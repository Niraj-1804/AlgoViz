# Assignment 4 - Part C: Orders Service

A small Flask REST service implementing the Part C requirements.

## Files

- `models.py` - stored Order record and public `as_json()` representation
- `store.py` - in-process dictionary storage
- `errors.py` - common `problem()` error shape
- `app.py` - REST endpoints and validation
- `tests/test_app.py` - four required pytest tests

## Endpoints

- `POST /orders`
- `GET /orders/{id}`
- `GET /orders?status=PENDING`
- `POST /orders/{id}/cancellation`

## Run

```bash
pip install -r requirements.txt
python app.py
```

## Test

```bash
pytest -q
```

The create endpoint accepts an `Idempotency-Key` header and returns the original order when the same key is repeated.
