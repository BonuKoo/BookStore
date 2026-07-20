"""
오버셀링 재현용 시더 (포트폴리오 2-4 "잔여 리스크" 실증).

시나리오: 재고 S개짜리 단일 상품에 N건(N > S)의 확정 대기 주문을 만들어
전부 confirm → 결제는 N건 모두 성공하지만 재고 차감은 S건만 성공,
나머지 N-S건은 stock.deduction.queue.dlq에 적재된다.
그 N-S건이 곧 "환불 대상" — verify_oversell.py가 식별한다.

seed_orders.py와 컨벤션 동일하되 격리를 위해 분리:
- 프리픽스 ov- (lt-와 안 섞임), ID 대역 2천만 (lt-는 1천만)
- 전용 상품 oversell-isbn-001, 재고를 매 실행 S로 리셋

사용: /c/Python313/python seed_oversell.py [주문수 N=20] [재고 S=5]
"""
import json
import sys
import time

import pymysql

N = int(sys.argv[1]) if len(sys.argv) > 1 else 20
S = int(sys.argv[2]) if len(sys.argv) > 2 else 5
# N > S 면 오버셀링 재현, N <= S 면 대조군(재고 충분 → 전건 차감 성공·DLQ 0·환불 0 기대).
if N > S:
    print(f"[oversell] 주문 {N} > 재고 {S} → 환불 대상 {N - S}건 기대")
else:
    print(f"[control] 주문 {N} <= 재고 {S} → 전건 차감 성공·환불 0건 기대")

ID_BASE = 20_000_000  # lt-(1천만 대역)와 충돌 방지
PRODUCT_ID = "oversell-isbn-001"
SELLER_ID = 999
AMOUNT = 1000
BUYER = 1

ts = int(time.time())
run_tag = f"ov-{ts}"

conn = pymysql.connect(host="127.0.0.1", port=3306, user="root", password="1234",
                       database="core2_spa", autocommit=False)
cur = conn.cursor()

# 이전 ov- 실행 잔여물 정리. stock_deduction까지 지워야 하는 점이 seed_orders.py와
# 다르다 — 남아 있으면 멱등 가드(order_id UNIQUE)가 아니라 단순 잔여 행 누적이지만,
# verify의 "차감 성공 수 == S" 집계를 오염시킨다.
cur.execute(
    """DELETE h FROM payment_order_history h
       JOIN payment_orders o ON h.payment_order_id = o.payment_order_id
       WHERE o.order_id LIKE 'ov-%'"""
)
cur.execute("DELETE FROM payment_orders WHERE order_id LIKE 'ov-%'")
cur.execute("DELETE FROM payment_event WHERE order_id LIKE 'ov-%'")
cur.execute("DELETE FROM stock_deduction WHERE order_id LIKE 'ov-%'")

# 재고를 정확히 S로 리셋 — 여기가 seed_orders.py(넉넉히 N*10)와 정반대 포인트
cur.execute(
    """INSERT INTO item (isbn, price, stock_quantity, title, seller_id, version)
       VALUES (%s, %s, %s, 'oversell test book', %s, 0)
       ON DUPLICATE KEY UPDATE stock_quantity = VALUES(stock_quantity), price = VALUES(price)""",
    (PRODUCT_ID, AMOUNT, S, SELLER_ID),
)

orders = []
event_rows = []
order_rows = []
for i in range(N):
    order_id = f"{run_tag}-{i:05d}"
    orders.append({"orderId": order_id, "amount": AMOUNT})
    event_rows.append((ID_BASE + i, BUYER, BUYER, 0, order_id, "oversell-test",
                       "NOT_STARTED", 0))
    order_rows.append((ID_BASE + i, AMOUNT, 0, 0, 0, order_id, "NOT_STARTED",
                       PRODUCT_ID, SELLER_ID, ID_BASE + i, 1, 0))

cur.executemany(
    """INSERT INTO payment_event
       (payment_event_id, buyer, buyer_id, is_payment_done, order_id, order_name,
        payment_status, version, created_at)
       VALUES (%s, %s, %s, %s, %s, %s, %s, %s, NOW(6))""",
    event_rows,
)
cur.executemany(
    """INSERT INTO payment_orders
       (payment_order_id, amount, failed_count, is_ledger_updated, is_wallet_updated,
        order_id, payment_status, product_id, seller_id, payment_id, quantity, threshold, created_at)
       VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, NOW(6))""",
    order_rows,
)
conn.commit()
conn.close()

out = __file__.rsplit("seed_oversell.py", 1)[0] + "oversell_orders.json"
with open(out, "w", encoding="utf-8") as f:
    json.dump(orders, f)

print(f"seeded {N} orders vs stock {S} (expect {N - S} refund targets), run_tag={run_tag}")
print(f"oversell_orders.json written: {out}")
