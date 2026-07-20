"""
lock_bench.py 결과의 독립 검증 (3중 교차 증명).

A. 반증 가능성: 일부러 락 없는 read-then-write(naive) 전략을 같은 하니스에 넣는다.
   → lost update가 실제로 발생해 정합성 검증이 FAIL로 잡아내야 한다.
   (하니스가 망가진 전략을 통과시키면 하니스 자체가 무가치하다는 증명 시도)

B. MySQL 서버 카운터 교차 검증: SHOW GLOBAL STATUS의 Com_update(서버가 받은
   UPDATE 문 총수)를 각 전략 실행 직전/직후에 떠서, 스크립트가 주장하는
   "성공+충돌" 횟수와 대조한다. 이 카운터는 벤치 스크립트가 위조할 수 없다.

C. DB 불변식: 낙관적 락의 version 컬럼은 성공한 CAS에서만 +1 되므로
   실행 후 version == 성공 수 여야 한다 (리셋 시 version=0).

사용: /c/Python313/python verify_bench.py   (인자 없이 — lock_bench 기본값 200ops/40스레드 상속)
"""
import time

import pymysql

import lock_bench as lb  # 동일 하니스/전략 함수 재사용 (다른 구현이 아니라 '그 코드 그대로' 검증)


def mon_conn():
    return pymysql.connect(**lb.DB, autocommit=True)


def global_counter(cur, name):
    cur.execute(f"SHOW GLOBAL STATUS LIKE '{name}'")
    return int(cur.fetchone()[1])


def read_row():
    c = mon_conn()
    cur = c.cursor()
    cur.execute("SELECT stock_quantity, version FROM item WHERE isbn=%s", (lb.ISBN,))
    row = cur.fetchone()
    c.close()
    return row


# ---- A. 고의로 망가뜨린 전략: 락 없는 read-then-write (lost update 유발기) ----
def op_naive(conn, stats):
    cur = conn.cursor()
    cur.execute("SELECT stock_quantity FROM item WHERE isbn=%s", (lb.ISBN,))
    stock = cur.fetchone()[0]
    if stock < 1:
        stats["underflow"] += 1
        return False
    time.sleep(0.002)  # 읽기-쓰기 사이 경쟁 윈도우 (실제 앱의 처리 지연 상당)
    cur.execute("UPDATE item SET stock_quantity=%s WHERE isbn=%s", (stock - 1, lb.ISBN))
    return True


def run_with_counters(title, runner):
    mc = mon_conn()
    mcur = mc.cursor()
    upd_before = global_counter(mcur, "Com_update")
    lockwait_before = global_counter(mcur, "Innodb_row_lock_waits")

    total_ms, lat, stats = runner()

    upd_after = global_counter(mcur, "Com_update")
    lockwait_after = global_counter(mcur, "Innodb_row_lock_waits")
    mc.close()

    stock, version = read_row()
    consistent = (stock == lb.OPS - stats["success"])
    return {
        "title": title,
        "total_ms": round(total_ms, 1),
        "success": stats["success"],
        "collisions": stats["retries"],
        "mysql_updates": upd_after - upd_before,   # MySQL이 실제 수신한 UPDATE 문 수
        "mysql_lock_waits": lockwait_after - lockwait_before,  # InnoDB 행락 대기 발생 수
        "final_stock": stock,
        "final_version": version,
        "consistent": consistent,
        "underflow": stats["underflow"],
        "errors": stats["errors"],
    }


def show(r, expect_updates=None, note=""):
    print(f"\n=== {r['title']} ===")
    print(f"  스크립트 주장: 성공 {r['success']} / 충돌(재시도) {r['collisions']} / 에러 {r['errors']}")
    print(f"  MySQL 카운터 : UPDATE문 수신 {r['mysql_updates']}건, InnoDB 행락 대기 {r['mysql_lock_waits']}회")
    if expect_updates is not None:
        match = "일치 ✅" if r["mysql_updates"] == expect_updates else f"불일치 ❌ (기대 {expect_updates})"
        print(f"  교차검증     : 기대 UPDATE 수({expect_updates}) vs MySQL 실측({r['mysql_updates']}) → {match}")
    print(f"  DB 최종상태  : stock={r['final_stock']}, version={r['final_version']}")
    print(f"  정합성       : 최종재고({r['final_stock']}) == 200 - 성공({r['success']}) → "
          f"{'✅ 유지' if r['consistent'] else '❌ 깨짐 (lost update 발생)'}")
    if note:
        print(f"  해석         : {note}")


def main():
    print(f"검증 실행: ops={lb.OPS}, threads={lb.THREADS}, item={lb.ISBN}")

    # --- A. 반증 가능성: naive는 반드시 FAIL로 잡혀야 한다 ---
    lb.reset_stock(lb.OPS)
    naive = run_with_counters(
        "A. naive read-then-write (고의 결함 전략 — 하니스가 잡아내는지)",
        lambda: lb.run_concurrent(op_naive, autocommit=True))
    show(naive, expect_updates=naive["success"],
         note="정합성이 '깨짐'으로 나와야 정상 — 하니스의 검증이 실제로 결함을 탐지한다는 증명")

    # --- B/C. 낙관적 락: Com_update == 성공+충돌, version == 성공 ---
    lb.reset_stock(lb.OPS)
    opt = run_with_counters(
        "B. optimistic (카운터·version 교차검증)",
        lambda: lb.run_concurrent(lb.op_optimistic, autocommit=True))
    show(opt, expect_updates=opt["success"] + opt["collisions"],
         note=f"version({opt['final_version']}) == 성공({opt['success']}) → "
              f"{'✅ 불변식 성립' if opt['final_version'] == opt['success'] else '❌ 불변식 위반'}"
              " (version은 성공한 CAS에서만 +1, 스크립트가 위조 불가)")

    # --- B. 비관적 락: Com_update == 성공 수, 행락 대기가 실제 발생 ---
    lb.reset_stock(lb.OPS)
    pes = run_with_counters(
        "C. pessimistic (행락 대기 실재 검증)",
        lambda: lb.run_concurrent(lb.op_pessimistic, autocommit=False))
    show(pes, expect_updates=pes["success"],
         note="InnoDB 행락 대기 수가 0보다 커야 'FOR UPDATE 줄서기'가 실재했다는 뜻")

    # --- B. 원자 UPDATE: Com_update == 시도 수(성공+언더플로) ---
    lb.reset_stock(lb.OPS)
    atm = run_with_counters(
        "D. atomic (UPDATE 수 == 시도 수 검증)",
        lambda: lb.run_concurrent(lb.op_atomic, autocommit=True))
    show(atm, expect_updates=atm["success"] + atm["underflow"])

    print("\n---- 판정 요약 ----")
    print(f"A naive가 정합성 FAIL로 탐지됨      : {'✅' if not naive['consistent'] else '❌ (하니스 무가치!)'}")
    print(f"B optimistic UPDATE수 교차 일치     : {'✅' if opt['mysql_updates'] == opt['success']+opt['collisions'] else '❌'}")
    print(f"C optimistic version==성공 불변식   : {'✅' if opt['final_version'] == opt['success'] else '❌'}")
    print(f"D pessimistic 행락 대기 실재        : {'✅' if pes['mysql_lock_waits'] > 0 else '❌'}")
    print(f"E 정상 전략 3종 정합성 전부 유지    : "
          f"{'✅' if all(r['consistent'] for r in (opt, pes, atm)) else '❌'}")


if __name__ == "__main__":
    main()
