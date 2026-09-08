from dataclasses import dataclass
from datetime import datetime, timezone

@dataclass
class Order:
    order_id: str
    customer_id: str
    item: str
    quantity: int
    amount: float
    status: str = "PENDING_PAYMENT"
    created_at: str = ""
    internal_id: str = ""

    def as_json(self):
        return {
            "id": self.order_id,
            "customerId": self.customer_id,
            "item": self.item,
            "quantity": self.quantity,
            "amount": self.amount,
            "status": self.status,
            "createdAt": self.created_at,
        }
