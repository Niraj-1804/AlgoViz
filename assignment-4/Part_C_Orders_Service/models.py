from dataclasses import dataclass, field
from datetime import datetime
from typing import List, Dict, Any


@dataclass
class Order:
    order_id: int
    customer_id: str
    items: List[Dict[str, Any]]
    total: float
    status: str = "PENDING"
    internal_id: str = field(default="", repr=False)
    created_at: str = field(default_factory=lambda: datetime.utcnow().isoformat())

    def as_json(self):
        """Public representation. Internal id is deliberately not exposed."""
        return {
            "id": self.order_id,
            "customerId": self.customer_id,
            "items": self.items,
            "total": self.total,
            "status": self.status,
            "createdAt": self.created_at,
        }
