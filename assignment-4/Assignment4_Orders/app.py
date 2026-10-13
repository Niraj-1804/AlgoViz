import json, os, random, time, uuid
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.parse import urlparse, parse_qs
from urllib.request import Request, urlopen
from urllib.error import HTTPError, URLError
from models import Order
from store import OrderStore
from errors import problem, send_json

STORE = OrderStore()
PAYMENTS_URL = os.getenv("PAYMENTS_SERVICE_URL", "http://localhost:8090")
TIMEOUT = 5
MAX_ATTEMPTS = 3


def validate(data):
    if not isinstance(data, dict): return "Body must be a JSON object."
    required = ["customerId", "item", "quantity", "amount"]
    missing = [x for x in required if x not in data]
    if missing: return "Missing required field(s): " + ", ".join(missing)
    if not isinstance(data["customerId"], str) or not data["customerId"]: return "customerId must be a non-empty string."
    if not isinstance(data["item"], str) or not data["item"]: return "item must be a non-empty string."
    if not isinstance(data["quantity"], int) or isinstance(data["quantity"], bool) or data["quantity"] <= 0: return "quantity must be a positive integer."
    if not isinstance(data["amount"], (int, float)) or isinstance(data["amount"], bool) or data["amount"] <= 0: return "amount must be a positive number."
    return None


def call_payments(order, idem_key):
    base = PAYMENTS_URL.rstrip("/")
    payload = json.dumps({"orderId": order.order_id, "amount": order.amount, "currency": "INR"}).encode()
    for attempt in range(1, MAX_ATTEMPTS + 1):
        req = Request(base + "/payments", data=payload, method="POST",
                      headers={"Content-Type": "application/json", "Idempotency-Key": idem_key})
        try:
            with urlopen(req, timeout=TIMEOUT) as r:
                if 200 <= r.status < 300:
                    return True, None
                if 400 <= r.status < 500:
                    return False, (r.status, "Payment service rejected the request")
                # 5xx: retry if budget remains
                if attempt == MAX_ATTEMPTS: return False, (503, "Payment service unavailable")
        except HTTPError as e:
            if 400 <= e.code < 500:
                return False, (e.code, "Payment service rejected the request")
            if attempt == MAX_ATTEMPTS: return False, (503, "Payment service unavailable")
        except (URLError, TimeoutError, ConnectionError):
            if attempt == MAX_ATTEMPTS: return False, (503, "Payment service unavailable")
        delay = 0.5 * (2 ** (attempt - 1)) + random.uniform(0, 0.2)
        time.sleep(delay)
    return False, (503, "Payment service unavailable")

class Handler(BaseHTTPRequestHandler):
    def log_message(self, fmt, *args): pass

    def read_json(self):
        try:
            n = int(self.headers.get("Content-Length", "0"))
            return json.loads(self.rfile.read(n))
        except Exception:
            return None

    def do_GET(self):
        p = urlparse(self.path)
        if p.path.startswith("/orders/"):
            oid = p.path.split("/")[2]
            o = STORE.get(oid)
            if not o: return send_json(self, 404, problem(404, "Not Found", "Order not found."))
            return send_json(self, 200, o.as_json())
        if p.path == "/orders":
            status = parse_qs(p.query).get("status", [None])[0]
            if status is not None and status not in {"PENDING_PAYMENT", "CONFIRMED", "CANCELLED"}:
                return send_json(self, 400, problem(400, "Bad Request", "Invalid status filter."))
            return send_json(self, 200, {"items": [o.as_json() for o in STORE.find(status)]})
        return send_json(self, 404, problem(404, "Not Found", "Resource not found."))

    def do_POST(self):
        p = urlparse(self.path)
        if p.path == "/orders":
            data = self.read_json()
            if data is None: return send_json(self, 400, problem(400, "Bad Request", "Malformed JSON body."))
            err = validate(data)
            if err: return send_json(self, 400, problem(400, "Bad Request", err))
            key = self.headers.get("Idempotency-Key")
            if key:
                old = STORE.by_key(key)
                if old: return send_json(self, 201, old.as_json(), {"Location": "/orders/" + old.order_id})
            oid = "ORD-" + uuid.uuid4().hex[:10].upper()
            order = Order(oid, data["customerId"], data["item"], data["quantity"], float(data["amount"]), created_at=datetime_now(), internal_id=uuid.uuid4().hex)
            if not key: key = uuid.uuid4().hex
            ok, failure = call_payments(order, key)
            if not ok:
                status, detail = failure
                if status != 503: status = 422
                return send_json(self, status, problem(status, "Payment Dependency Unavailable" if status == 503 else "Payment Rejected", detail))
            order.status = "CONFIRMED"
            STORE.add(order, key)
            return send_json(self, 201, order.as_json(), {"Location": "/orders/" + order.order_id})
        return send_json(self, 404, problem(404, "Not Found", "Resource not found."))

    def do_PATCH(self):
        p = urlparse(self.path)
        parts = p.path.strip("/").split("/")
        if len(parts) == 3 and parts[0] == "orders" and parts[2] == "cancellation":
            o = STORE.get(parts[1])
            if not o: return send_json(self, 404, problem(404, "Not Found", "Order not found."))
            if o.status == "CANCELLED": return send_json(self, 409, problem(409, "Conflict", "Order is already cancelled."))
            if o.status != "CONFIRMED": return send_json(self, 409, problem(409, "Conflict", "Only confirmed orders can be cancelled."))
            o.status = "CANCELLED"
            return send_json(self, 200, o.as_json())
        return send_json(self, 404, problem(404, "Not Found", "Resource not found."))

def datetime_now():
    from datetime import datetime, timezone
    return datetime.now(timezone.utc).isoformat()

def main():
    port = int(os.getenv("PORT", "8080"))
    print(f"Orders service listening on http://localhost:{port}")
    ThreadingHTTPServer(("0.0.0.0", port), Handler).serve_forever()

if __name__ == "__main__": main()
