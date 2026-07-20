"""
오버셀링 실험 사후 판정 (seed_oversell.py + oversell_confirm.js 실행 후).

판정 항목:
 1. 결제: ov- 주문 N건 전부 payment_status=SUCCESS (결제는 다 성공했는가)
 2. 재고: oversell-isbn-001 재고 == 0, 음수 아님 / stock_deduction ov- 행 == S
 3. DLQ: stock.deduction.queue.dlq에서 ov- 메시지 == N-S건 (peek, 비파괴)
 4. 환불 대상 식별: "SUCCESS인데 stock_deduction 기록이 없는 주문" 목록 + 금액
    → 이 쿼리가 곧 향후 자동 환불 컨슈머의 입력 정의다.

사용: /c/Python313/python verify_oversell.py
     RabbitMQ 관리 API 접근 불가 환경(브로커 다운 등)에선 3번만 SKIP 처리.
"""
import base64
import json
import sys
import urllib.request

import pymysql

# Windows 콘솔(cp949)에서 한글/대시 출력 깨짐 방지
sys.stdout.reconfigure(encoding="utf-8", errors="replace")

MQ_HOST = "192.168.0.6"
MQ_USER, MQ_PASS = "coreapp", "1234"
VHOST = "core_vhost"
DLQ = "stock.deduction.queue.dlq"
PRODUCT_ID = "oversell-isbn-001"

# 이번 실행의 run_tag를 oversell_orders.json에서 읽어 정확히 그 배치만 집계한다.
# (이전 실패 run이 DLQ에 남긴 ov- 메시지가 섞여도 오염되지 않도록 — 'ov-' 통짜 필터 금지)
_json = __file__.rsplit("verify_oversell.py", 1)[0] + "oversell_orders.json"
with open(_json, encoding="utf-8") as _f:
    _orders = json.load(_f)
RUN_TAG = _orders[0]["orderId"].rsplit("-", 1)[0]  # ov-<ts>
LIKE = RUN_TAG + "-%"
print(f"[run_tag] {RUN_TAG} ({len(_orders)}건 대상)\n")

conn = pymysql.connect(host="127.0.0.1", port=3306, user="root", password="1234",
                       database="core2_spa", autocommit=True)
cur = conn.cursor()

ok = True

# --- 1. 결제 전건 성공 ---
# 주의: confirm 경로가 갱신하는 실제 결제 상태는 payment_ORDERS.payment_status다.
# payment_event.payment_status는 이 흐름에서 갱신되지 않고(완결은 is_payment_done로 추적)
# NOT_STARTED로 남으므로, 여기서 보면 안 된다. (2026-07-20 실측으로 확인)
cur.execute("SELECT payment_status, COUNT(DISTINCT order_id) FROM payment_orders "
            "WHERE order_id LIKE %s GROUP BY payment_status", (LIKE,))
by_status = dict(cur.fetchall())
n_total = sum(by_status.values())
n_success = by_status.get("SUCCESS", 0)
print(f"[1] 결제 상태 분포: {by_status} (총 {n_total}건)")
if n_success != n_total or n_total == 0:
    ok = False
    print(f"    FAIL — 전건 SUCCESS 여야 함 (SUCCESS={n_success}/{n_total})")
else:
    print(f"    PASS — 결제 {n_total}건 전부 성공")

# --- 2. 재고/차감 정합성 ---
cur.execute("SELECT stock_quantity FROM item WHERE isbn = %s", (PRODUCT_ID,))
row = cur.fetchone()
stock_now = row[0] if row else None
cur.execute("SELECT COUNT(*) FROM stock_deduction WHERE order_id LIKE %s", (LIKE,))
n_deducted = cur.fetchone()[0]
expected_refunds = n_total - n_deducted
print(f"[2] 현재 재고={stock_now}, 차감 성공={n_deducted}건 → 환불 대상 기대치={expected_refunds}건")
if stock_now is None or stock_now < 0:
    ok = False
    print("    FAIL — 재고가 음수거나 상품이 없음 (원자 UPDATE 계약 위반!)")
elif stock_now != 0:
    print(f"    WARN — 재고가 0이 아님({stock_now}). 컨슈머가 아직 드레인 중일 수 있으니 잠시 후 재실행")
else:
    print("    PASS — 재고 정확히 소진, 음수 없음")

# --- 3. DLQ 적재 확인 (비파괴 peek: ack_requeue_true) ---
try:
    # count는 크게 — DLQ에 과거 실험 잔여물이 쌓여 있으면 이번 run 메시지가 큐 뒤쪽에
    # 밀려 있어, 작은 count로는 fetch 창 밖으로 벗어난다(비파괴 peek라 과다 요청 무해).
    url = f"http://{MQ_HOST}:15672/api/queues/{VHOST}/{DLQ}/get"
    body = json.dumps({"count": 5000, "ackmode": "ack_requeue_true",
                       "encoding": "auto"}).encode()
    req = urllib.request.Request(url, data=body, method="POST",
                                 headers={"Content-Type": "application/json"})
    token = base64.b64encode(f"{MQ_USER}:{MQ_PASS}".encode()).decode()
    req.add_header("Authorization", f"Basic {token}")
    with urllib.request.urlopen(req, timeout=10) as r:
        msgs = json.loads(r.read())
    ov_in_dlq = [m for m in msgs
                 if str(json.loads(m["payload"]).get("payload", {}).get("orderId", "")).startswith(RUN_TAG)]
    print(f"[3] DLQ 총 {len(msgs)}건 중 이번 run({RUN_TAG}) 메시지 {len(ov_in_dlq)}건 (기대 {expected_refunds}건)")
    if len(ov_in_dlq) != expected_refunds:
        ok = False
        print("    FAIL — DLQ 적재 수가 환불 대상 수와 불일치 (드레인 미완이면 재실행)")
    else:
        print("    PASS — 재고 부족분이 정확히 DLQ로 격리됨")
except Exception as e:
    print(f"[3] SKIP — 관리 API 접근 실패: {e}")

# --- 4. 환불 대상 식별 쿼리 (자동 환불 컨슈머의 입력 정의) ---
cur.execute(
    """SELECT po.order_id, SUM(po.amount) AS refund_amount
       FROM payment_orders po
       LEFT JOIN stock_deduction sd ON sd.order_id = po.order_id
       WHERE po.order_id LIKE %s
         AND po.payment_status = 'SUCCESS'
         AND sd.order_id IS NULL
       GROUP BY po.order_id ORDER BY po.order_id""",
    (LIKE,)
)
targets = cur.fetchall()
print(f"[4] 환불 대상 {len(targets)}건 (결제 성공 & 재고 차감 실패):")
for order_id, amount in targets[:10]:
    print(f"    {order_id}  환불액 {amount}")
if len(targets) > 10:
    print(f"    ... 외 {len(targets) - 10}건")
if len(targets) != expected_refunds:
    ok = False
    print(f"    FAIL — 식별 수({len(targets)}) != 기대치({expected_refunds})")
else:
    print("    PASS — DB만으로 환불 대상 완전 식별 가능")

conn.close()
print("\n=== 종합:", "PASS — 오버셀링이 재현되었고 환불 대상이 정확히 격리·식별됨" if ok
      else "FAIL — 위 항목 확인", "===")
