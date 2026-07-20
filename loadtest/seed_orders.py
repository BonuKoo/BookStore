"""
k6 부하 테스트용 주문 시더 (Phase 6).

confirm API는 기존 주문(payment_event + payment_orders, NOT_STARTED)을 전제하므로,
로그인/장바구니/체크아웃 흐름을 우회해 DB에 직접 N건을 시드한다.
- 전용 상품(loadtest-isbn-001)을 충분한 재고로 upsert — 재고 차감 컨슈머가 DLQ로 빠지지 않게.
- ID는 hibernate 시퀀스 테이블(payment_event_seq/payment_order_seq)의 현재 값과 절대
  충돌하지 않도록 1천만 대역을 쓴다.
- 결과 orderId 목록을 orders.json으로 출력 → k6가 SharedArray로 읽는다.

사용: /c/Python313/python seed_orders.py [건수=1000]
"""
import json
import sys
import time

import pymysql

N = int(sys.argv[1]) if len(sys.argv) > 1 else 1000
ID_BASE = 10_000_000  # hibernate seq(수십만 대)와 충돌 방지용 오프셋
PRODUCT_ID = "loadtest-isbn-001"
SELLER_ID = 999
AMOUNT = 1000
BUYER = 1  # user_entity의 실존 행

ts = int(time.time())
run_tag = f"lt-{ts}"

conn = pymysql.connect(host="127.0.0.1", port=3306, user="root", password="1234",
                       database="core2_spa", autocommit=False)
cur = conn.cursor()

# 이전 실행 잔여물 정리 (payment_event.order_id UNIQUE 충돌 방지, lt- 프리픽스만).
# 이전 실행분을 한 번이라도 confirm 했다면 payment_order_history 가 payment_orders 를
# FK로 참조하므로, 자식(history)부터 지워야 부모(orders) 삭제가 가능하다.
cur.execute(
    """DELETE h FROM payment_order_history h
       JOIN payment_orders o ON h.payment_order_id = o.payment_order_id
       WHERE o.order_id LIKE 'lt-%'"""
)
cur.execute("DELETE FROM payment_orders WHERE order_id LIKE 'lt-%'")
cur.execute("DELETE FROM payment_event WHERE order_id LIKE 'lt-%'")

# 부하 전용 상품: 재고를 N*10으로 넉넉히 리셋
cur.execute(
    """INSERT INTO item (isbn, price, stock_quantity, title, seller_id, version)
       VALUES (%s, %s, %s, 'k6 loadtest book', %s, 0)
       ON DUPLICATE KEY UPDATE stock_quantity = VALUES(stock_quantity), price = VALUES(price)""",
    (PRODUCT_ID, AMOUNT, N * 10, SELLER_ID),
)

orders = []
event_rows = []
order_rows = []
for i in range(N):
    order_id = f"{run_tag}-{i:05d}"
    orders.append({"orderId": order_id, "amount": AMOUNT})
    event_rows.append((ID_BASE + i, BUYER, BUYER, 0, order_id, "loadtest",
                       "NOT_STARTED", 0))
    # threshold는 엔티티에서 primitive int라 NULL이면 confirm 시 JpaSystemException
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

out = __file__.rsplit("seed_orders.py", 1)[0] + "orders.json"
with open(out, "w", encoding="utf-8") as f:
    json.dump(orders, f)

print(f"seeded {N} orders, run_tag={run_tag}")
print(f"orders.json written: {out}")
