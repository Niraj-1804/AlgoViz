from flask import Flask, request, jsonify, url_for
from models import Order
from store import OrderStore
from errors import problem

app = Flask(__name__)
store = OrderStore()

ALLOWED_STATUSES = {"PENDING", "CONFIRMED", "CANCELLED"}


def validate(data):
    """Validate a create-order request before accessing its fields."""
    if not isinstance(data, dict):
        return False, "Request body must be a JSON object."

    required = {"customerId", "items", "total"}
    missing = required - set(data.keys())
    if missing:
        return False, f"Missing required field(s): {', '.join(sorted(missing))}."

    if not isinstance(data["customerId"], str) or not data["customerId"].strip():
        return False, "customerId must be a non-empty string."

    if not isinstance(data["items"], list) or not data["items"]:
        return False, "items must be a non-empty list."

    if not isinstance(data["total"], (int, float)) or isinstance(data["total"], bool):
        return False, "total must be a number."

    if data["total"] < 0:
        return False, "total cannot be negative."

    for item in data["items"]:
        if not isinstance(item, dict):
            return False, "Each item must be an object."
        if "name" not in item or "quantity" not in item:
            return False, "Each item must contain name and quantity."
        if not isinstance(item["quantity"], int) or item["quantity"] <= 0:
            return False, "Item quantity must be a positive integer."

    return True, ""


@app.post("/orders")
def create_order():
    data = request.get_json(silent=True)
    valid, detail = validate(data)
    if not valid:
        return problem(400, "Malformed request", detail)

    key = request.headers.get("Idempotency-Key")
    if key:
        existing = store.get_idempotent_order(key)
        if existing:
            response = jsonify(existing.as_json())
            response.status_code = 201
            response.headers["Location"] = url_for("get_order", order_id=existing.order_id)
            return response

    order_id = store.next_id
    order = Order(
        order_id=order_id,
        customer_id=data["customerId"],
        items=data["items"],
        total=float(data["total"]),
        internal_id=f"internal-{order_id}",
    )
    store.create(order)

    if key:
        store.save_idempotency(key, order_id)

    response = jsonify(order.as_json())
    response.status_code = 201
    response.headers["Location"] = url_for("get_order", order_id=order_id)
    return response


@app.get("/orders/<int:order_id>")
def get_order(order_id):
    order = store.get(order_id)
    if not order:
        return problem(404, "Order not found", f"No order exists with id {order_id}.")
    return jsonify(order.as_json()), 200


@app.get("/orders")
def list_orders():
    status = request.args.get("status")
    if status and status not in ALLOWED_STATUSES:
        return problem(400, "Invalid status", f"Unsupported status: {status}.")
    orders = store.list_by_status(status)
    return jsonify([order.as_json() for order in orders]), 200


@app.post("/orders/<int:order_id>/cancellation")
def cancel_order(order_id):
    order = store.get(order_id)
    if not order:
        return problem(404, "Order not found", f"No order exists with id {order_id}.")

    if order.status == "CANCELLED":
        return problem(409, "State conflict", "The order is already cancelled.")

    if order.status == "CONFIRMED":
        return problem(409, "State conflict", "A confirmed order cannot be cancelled.")

    order.status = "CANCELLED"
    return jsonify(order.as_json()), 200


if __name__ == "__main__":
    app.run(debug=True)
