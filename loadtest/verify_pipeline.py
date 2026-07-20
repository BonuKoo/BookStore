"""
k6 부하 후 MQ 파이프라인 완결 검증기 (Phase 6).

k6는 confirm API의 HTTP 지표만 본다. 이 스크립트는 그 뒤의 비동기 경로를 측정한다:
- 1초 간격으로 DB를 폴링해 is_payment_done=1 도달 건수와 워커 산출물
  (stock_deduction / wallet_transactions / ledger_transactions)을 추적
- 전건 완결까지의 드레인 시간(마지막 confirm 이후 파이프라인이 따라잡는 데 걸린 시간)
- RabbitMQ 관리 API로 큐 적체/DLQ 유입 감시 (통계 5초 지연 유의)
- 종료 시 정합성 검증: 모든 카운트 == N, 재고 감소량 == N, DLQ 증가 없음

사용: /c/Python313/python verify_pipeline.py [타임아웃초=300]
      (k6 실행 직후 바로 실행 — orders.json의 run_tag를 자동 인식)
"""
import base64
import json
import sys
import time
import urllib.request

import pymysql

TIMEOUT_S = int(sys.argv[1]) if len(sys.argv) > 1 else 300
MGMT = "http://192.168.0.6:15672/api"
AUTH = base64.b64encode(b"coreapp:1234").decode()
PRODUCT_ID = "loadtest-isbn-001"

here = __file__.rsplit("verify_pipeline.py", 1)[0]
with open(here + "orders.json", encoding="utf-8") as f:
    orders = json.load(f)
N = len(orders)
prefix = orders[0]["orderId"].rsplit("-", 1)[0] + "-%"  # 'lt-<ts>-%'

QUEUES = ["payment.confirmed.queue", "stock.deduction.queue",
          "wallet.settlement.queue", "ledger.recording.queue",
          "settlement.wallet.completed.queue", "settlement.ledger.completed.queue"]


def mgmt_get(path):
    req = urllib.request.Request(f"{MGMT}/{path}")
    req.add_header("Authorization", f"Basic {AUTH}")
    return json.load(urllib.request.urlopen(req, timeout=5))


def dlq_totals():
    total = 0
    for q in QUEUES:
        try:
            total += mgmt_get(f"queues/core_vhost/{q}.dlq")["messages"]
        except Exception:
            pass
    return total


def queue_depths():
    parts = []
    for q in QUEUES:
        try:
            d = mgmt_get(f"queues/core_vhost/{q}")
            if d["messages"]:
                parts.append(f"{q.split('.')[0]}={d['messages']}")
        except Exception:
            pass
    return " ".join(parts) if parts else "all-empty"


conn = pymysql.connect(host="127.0.0.1", port=3306, user="root", password="1234",
                       database="core2_spa", autocommit=True)
cur = conn.cursor()


def counts():
    cur.execute("SELECT COUNT(*) FROM payment_event WHERE order_id LIKE %s AND is_payment_done=1", (prefix,))
    done = cur.fetchone()[0]
    cur.execute("SELECT COUNT(*) FROM stock_deduction WHERE order_id LIKE %s", (prefix,))
    stock = cur.fetchone()[0]
    cur.execute("SELECT COUNT(*) FROM wallet_transactions WHERE order_id LIKE %s", (prefix,))
    wallet = cur.fetchone()[0]
    cur.execute("SELECT COUNT(*) FROM ledger_transactions WHERE order_id LIKE %s", (prefix,))
    ledger = cur.fetchone()[0]
    return done, stock, wallet, ledger


dlq_before = dlq_totals()
cur.execute("SELECT stock_quantity FROM item WHERE isbn=%s", (PRODUCT_ID,))
stock_before_row = cur.fetchone()
start = time.time()
print(f"N={N}, prefix={prefix}, dlq_before={dlq_before}, 폴링 시작...")

last = (-1, -1, -1, -1)
while True:
    done, stock, wallet, ledger = counts()
    elapsed = time.time() - start
    if (done, stock, wallet, ledger) != last:
        print(f"[{elapsed:6.1f}s] done={done}/{N} stock={stock} wallet={wallet} ledger={ledger} | q: {queue_depths()}")
        last = (done, stock, wallet, ledger)
    if done == N and stock == N and wallet == N and ledger == N:
        print(f"\n=== 전건 완결. 드레인 시간(폴링 시작 기준): {elapsed:.1f}s ===")
        break
    if elapsed > TIMEOUT_S:
        print(f"\n=== 타임아웃({TIMEOUT_S}s). 미완결 잔여 있음 ===")
        break
    time.sleep(1)

# 최종 정합성
done, stock, wallet, ledger = counts()
dlq_after = dlq_totals()
cur.execute("SELECT stock_quantity FROM item WHERE isbn=%s", (PRODUCT_ID,))
stock_after = cur.fetchone()[0]
stock_delta = (stock_before_row[0] - stock_after) if stock_before_row else None
cur.execute("SELECT COUNT(*) FROM ledger_entries le JOIN ledger_transactions lt ON le.transaction_id=lt.id WHERE lt.order_id LIKE %s", (prefix,))
entries = cur.fetchone()[0]

print("\n---- 최종 정합성 ----")
print(f"is_payment_done : {done}/{N} {'OK' if done == N else 'FAIL'}")
print(f"stock_deduction : {stock}/{N} {'OK' if stock == N else 'FAIL'}")
print(f"재고 실감소     : {stock_delta} (기대 {N}) {'OK' if stock_delta == N else 'CHECK'}")
print(f"wallet_tx       : {wallet}/{N} {'OK' if wallet == N else 'FAIL'}")
print(f"ledger_tx       : {ledger}/{N} {'OK' if ledger == N else 'FAIL'}")
print(f"ledger_entries  : {entries} (기대 {N*2}) {'OK' if entries == N*2 else 'CHECK'}")
print(f"DLQ 유입        : {dlq_after - dlq_before}건 {'OK' if dlq_after == dlq_before else 'FAIL'}")
conn.close()
