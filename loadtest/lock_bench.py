"""
재고 차감 동시성 전략 4종 실측 벤치마크.

같은 행(전용 bench 아이템)에 대해 "재고 1 차감"을 N회 수행하는 동일 워크로드를
4가지 전략으로 실행하고 총 소요시간·처리량·재시도·정합성을 비교한다.

  1. pessimistic  — SELECT ... FOR UPDATE 후 UPDATE (JPA PESSIMISTIC_WRITE 상당)
  2. optimistic   — version 컬럼 CAS + 재시도 루프 (JPA @Version 상당)
  3. atomic       — UPDATE ... SET stock=stock-1 WHERE stock>=1 (조건부 원자 UPDATE,
                    분산 메시지 파이프라인의 StockDeductionService가 실제로 쓰는 전략)
  4. queue-serial — 단일 소비자가 큐에서 순차 처리 (MQ 단일 컨슈머 모델: 경합 자체를 제거)

1~3은 동시 스레드(기본 40)로 경합을 만들고, 4는 같은 200건을 한 스레드가 드레인한다.
각 전략 사이에 재고를 초기화하며, 최종 재고 == 초기 - 성공수 정합성을 항상 검증한다.

사용: /c/Python313/python lock_bench.py [ops=200] [threads=40] [rounds=3]
결과: 콘솔 표 + lock_bench_results.json
"""
import json
import os
import queue
import random
import statistics
import sys
import threading
import time

import pymysql

DB = dict(host="127.0.0.1", port=3306, user="root", password="1234",
          database="core2_spa", charset="utf8mb4")
ISBN = "lock-bench-001"
OPS = int(sys.argv[1]) if len(sys.argv) > 1 else 200
THREADS = int(sys.argv[2]) if len(sys.argv) > 2 else 40
ROUNDS = int(sys.argv[3]) if len(sys.argv) > 3 else 3
MAX_RETRY = 50  # 낙관적 락 재시도 상한 (JPA 재시도 파사드 상당)
HERE = os.path.dirname(os.path.abspath(__file__))


def conn_new(autocommit):
    return pymysql.connect(**DB, autocommit=autocommit)


def reset_stock(initial):
    c = conn_new(True)
    cur = c.cursor()
    cur.execute(
        """INSERT INTO item (isbn, price, stock_quantity, title, seller_id, version)
           VALUES (%s, 1000, %s, 'lock bench item', 999, 0)
           ON DUPLICATE KEY UPDATE stock_quantity=%s, version=0""",
        (ISBN, initial, initial),
    )
    c.close()


def read_stock():
    c = conn_new(True)
    cur = c.cursor()
    cur.execute("SELECT stock_quantity FROM item WHERE isbn=%s", (ISBN,))
    v = cur.fetchone()[0]
    c.close()
    return v


# ---- 전략별 "재고 1 차감" 구현 (각 스레드는 자기 커넥션을 재사용) ----

def op_pessimistic(conn, stats):
    cur = conn.cursor()
    cur.execute("BEGIN")
    cur.execute("SELECT stock_quantity FROM item WHERE isbn=%s FOR UPDATE", (ISBN,))
    stock = cur.fetchone()[0]
    if stock >= 1:
        cur.execute("UPDATE item SET stock_quantity=stock_quantity-1 WHERE isbn=%s", (ISBN,))
        conn.commit()
        return True
    conn.rollback()
    stats["underflow"] += 1
    return False


def op_optimistic(conn, stats):
    cur = conn.cursor()
    for attempt in range(MAX_RETRY):
        cur.execute("SELECT stock_quantity, version FROM item WHERE isbn=%s", (ISBN,))
        stock, ver = cur.fetchone()
        if stock < 1:
            stats["underflow"] += 1
            return False
        cur.execute(
            "UPDATE item SET stock_quantity=%s, version=version+1 WHERE isbn=%s AND version=%s",
            (stock - 1, ISBN, ver),
        )
        if cur.rowcount == 1:
            stats["retries"] += attempt
            return True
        # 버전 충돌 → 짧은 지터 후 재시도 (재시도 파사드의 백오프 상당)
        time.sleep(random.uniform(0.001, 0.005))
    stats["retries"] += MAX_RETRY
    stats["retry_exhausted"] += 1
    return False


def op_atomic(conn, stats):
    cur = conn.cursor()
    cur.execute(
        "UPDATE item SET stock_quantity=stock_quantity-1 WHERE isbn=%s AND stock_quantity>=1",
        (ISBN,),
    )
    if cur.rowcount == 1:
        return True
    stats["underflow"] += 1
    return False


# ---- 실행 하니스 ----

def run_concurrent(op_fn, autocommit):
    """OPS건을 THREADS개 스레드가 나눠 실행. per-op 지연과 성공수를 수집."""
    stats = {"success": 0, "underflow": 0, "retries": 0, "retry_exhausted": 0, "errors": 0}
    latencies = []
    lock = threading.Lock()
    task_q = queue.Queue()
    for _ in range(OPS):
        task_q.put(1)

    def worker():
        conn = conn_new(autocommit)
        while True:
            try:
                task_q.get_nowait()
            except queue.Empty:
                break
            t0 = time.perf_counter()
            try:
                ok = op_fn(conn, local)
            except Exception:
                ok = False
                local["errors"] += 1
                try:
                    conn.rollback()
                except Exception:
                    pass
            dt = (time.perf_counter() - t0) * 1000
            with lock:
                latencies.append(dt)
                if ok:
                    stats["success"] += 1
        conn.close()

    threads = []
    locals_ = []
    for _ in range(THREADS):
        local = {"underflow": 0, "retries": 0, "retry_exhausted": 0, "errors": 0}
        locals_.append(local)
        t = threading.Thread(target=worker)
        threads.append(t)

    t_start = time.perf_counter()
    for t in threads:
        t.start()
    for t in threads:
        t.join()
    total_ms = (time.perf_counter() - t_start) * 1000

    for local in locals_:
        for k in ("underflow", "retries", "retry_exhausted", "errors"):
            stats[k] += local[k]
    return total_ms, latencies, stats


def run_queue_serial():
    """MQ 단일 컨슈머 모델: 같은 200건을 한 스레드가 원자 UPDATE로 순차 드레인."""
    stats = {"success": 0, "underflow": 0, "retries": 0, "retry_exhausted": 0, "errors": 0}
    latencies = []
    conn = conn_new(True)
    t_start = time.perf_counter()
    for _ in range(OPS):
        t0 = time.perf_counter()
        if op_atomic(conn, stats):
            stats["success"] += 1
        latencies.append((time.perf_counter() - t0) * 1000)
    total_ms = (time.perf_counter() - t_start) * 1000
    conn.close()
    return total_ms, latencies, stats


def pct(latencies, p):
    if not latencies:
        return 0.0
    s = sorted(latencies)
    return s[min(len(s) - 1, int(len(s) * p))]


def run_strategy(name, runner):
    rounds = []
    for r in range(ROUNDS):
        reset_stock(OPS)  # 초기 재고 = 요청 수 → 전부 성공하면 최종 0
        total_ms, lat, stats = runner()
        final = read_stock()
        consistent = (final == OPS - stats["success"])
        rounds.append({
            "total_ms": round(total_ms, 1),
            "ops_per_s": round(stats["success"] / (total_ms / 1000), 1) if total_ms else 0,
            "mean_ms": round(statistics.mean(lat), 2),
            "p95_ms": round(pct(lat, 0.95), 2),
            "success": stats["success"],
            "underflow": stats["underflow"],
            "retries": stats["retries"],
            "retry_exhausted": stats["retry_exhausted"],
            "errors": stats["errors"],
            "final_stock": final,
            "consistent": consistent,
        })
        print(f"  [{name} r{r+1}] total={total_ms:7.1f}ms  p95={rounds[-1]['p95_ms']:6.2f}ms  "
              f"success={stats['success']} retries={stats['retries']} "
              f"errors={stats['errors']} final={final} {'OK' if consistent else '!!정합성깨짐!!'}")
    mid = sorted(rounds, key=lambda x: x["total_ms"])[len(rounds) // 2]
    return {"rounds": rounds, "median": mid}


def main():
    print(f"ops={OPS}, threads={THREADS}, rounds={ROUNDS}, item={ISBN}\n")
    results = {}
    results["pessimistic"] = run_strategy(
        "pessimistic", lambda: run_concurrent(op_pessimistic, autocommit=False))
    results["optimistic"] = run_strategy(
        "optimistic", lambda: run_concurrent(op_optimistic, autocommit=True))
    results["atomic"] = run_strategy(
        "atomic(MQ컨슈머 전략)", lambda: run_concurrent(op_atomic, autocommit=True))
    results["queue_serial"] = run_strategy(
        "queue-serial(단일컨슈머)", lambda: run_queue_serial())

    out = os.path.join(HERE, "lock_bench_results.json")
    with open(out, "w", encoding="utf-8") as f:
        json.dump({"ops": OPS, "threads": THREADS, "rounds": ROUNDS, "results": results},
                  f, ensure_ascii=False, indent=1)
    print(f"\nwrote {out}")

    print(f"\n{'전략':24s} {'total(ms)':>10s} {'ops/s':>8s} {'p95(ms)':>8s} {'재시도':>6s} {'정합성':>6s}")
    for name, r in results.items():
        m = r["median"]
        print(f"{name:24s} {m['total_ms']:>10.1f} {m['ops_per_s']:>8.1f} "
              f"{m['p95_ms']:>8.2f} {m['retries']:>6d} {'OK' if m['consistent'] else 'FAIL':>6s}")


if __name__ == "__main__":
    main()
