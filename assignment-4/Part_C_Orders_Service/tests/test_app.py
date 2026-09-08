import pytest
from app import app, store


@pytest.fixture(autouse=True)
def reset_store():
    store.orders.clear()
    store.idempotency.clear()
    store.next_id = 1
    app.config["TESTING"] = True


@pytest.fixture
def client():
    return app.test_client()


def order_payload():
    return {
        "customerId": "CUST-101",
        "items": [{"name": "Burger", "quantity": 2}],
        "total": 240
    }


def test_create_succeeds_with_201_and_location(client):
    response = client.post(
        "/orders",
        json=order_payload(),
        headers={"Idempotency-Key": "key-001"}
    )

    assert response.status_code == 201
    assert "Location" in response.headers
    assert response.get_json()["status"] == "PENDING"


def test_idempotent_repeat_returns_original(client):
    headers = {"Idempotency-Key": "key-002"}

    first = client.post("/orders", json=order_payload(), headers=headers)
    second = client.post("/orders", json=order_payload(), headers=headers)

    assert first.status_code == 201
    assert second.status_code == 201
    assert second.get_json()["id"] == first.get_json()["id"]
    assert len(store.orders) == 1


def test_malformed_body_returns_400(client):
    response = client.post(
        "/orders",
        json={"customerId": "CUST-101"},
    )

    assert response.status_code == 400
    body = response.get_json()
    assert {"type", "title", "status", "detail"} <= set(body.keys())


def test_unknown_id_returns_404(client):
    response = client.get("/orders/999")

    assert response.status_code == 404
    body = response.get_json()
    assert body["status"] == 404
