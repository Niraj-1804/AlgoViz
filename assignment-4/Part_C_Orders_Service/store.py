class OrderStore:
    """Simple in-process storage for the service."""

    def __init__(self):
        self.orders = {}
        self.idempotency = {}
        self.next_id = 1

    def create(self, order):
        self.orders[order.order_id] = order
        self.next_id += 1

    def get(self, order_id):
        return self.orders.get(order_id)

    def list_by_status(self, status=None):
        orders = list(self.orders.values())
        if status:
            orders = [o for o in orders if o.status == status]
        return orders

    def save_idempotency(self, key, order_id):
        self.idempotency[key] = order_id

    def get_idempotent_order(self, key):
        order_id = self.idempotency.get(key)
        return self.get(order_id) if order_id is not None else None
