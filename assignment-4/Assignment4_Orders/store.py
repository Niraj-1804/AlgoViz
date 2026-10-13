class OrderStore:
    def __init__(self):
        self.orders = {}
        self.idempotency = {}

    def add(self, order, key=None):
        self.orders[order.order_id] = order
        if key:
            self.idempotency[key] = order.order_id

    def get(self, order_id):
        return self.orders.get(order_id)

    def find(self, status=None):
        values = list(self.orders.values())
        return [o for o in values if status is None or o.status == status]

    def by_key(self, key):
        oid = self.idempotency.get(key)
        return self.get(oid) if oid else None
