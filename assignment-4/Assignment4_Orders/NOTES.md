# CS543 — Web Services — Assignment 4 Notes

## Team

**Team ID:** 17  
**Members:**
1. Anurag Tiwari — 20251651028
2. Mohd Rashid — 20251651057
3. Saurabh Singh — 20251651089
4. Niraj Kumar Gupta — 20251651066

## Selected service

**Orders** — the service is modeled as REST resources. Payments is used only as the outbound dependency required by Part D.

## A4 — Resource table

| Method | URL | What it does | Success code | Failure codes |
|---|---|---|---|---|
| POST | `/orders` | Creates an order and confirms payment through the Payments service | 201 Created + Location | 400, 503 |
| GET | `/orders/{orderId}` | Reads one order | 200 OK | 404 |
| GET | `/orders?status=CONFIRMED` | Lists orders, optionally filtered by status | 200 OK | 400 |
| PATCH | `/orders/{orderId}/cancellation` | Changes a confirmed order to cancelled | 200 OK | 404, 409 |

### A5 — Hard choice justification

The least comfortable SOAP-to-REST mapping was `cancelOrder(orderId)` because the SOAP operation is written as an action while REST URLs should represent durable resources. I mapped it to the state-changing sub-resource `/orders/{orderId}/cancellation`. I rejected `/cancelOrder` because the verb would remain in the URL, and I rejected `DELETE /orders/{orderId}` because cancellation is a business state transition rather than removal of the stored order.

## D3 — Fallback reasoning

If the Payments service is unreachable after the retry budget is exhausted, Orders fails the payment-dependent create operation and returns a consistent dependency-unavailable problem. It does not create an order that appears successfully paid without confirmed payment.

Degrading by accepting the order as paid would be unsafe because it could create an unpaid order or leave payment state ambiguous. This follows the Assignment 3 boundary where payment faults such as `PAYMENT_GATEWAY_UNAVAILABLE` are treated as explicit domain errors.

## Answers to the five questions

### 1. WSDL vs OpenAPI line count

The Assignment 3 `partner.wsdl` contains **93 lines**. This Assignment 4 `openapi.yaml` contains **105 lines**, so the OpenAPI file is **12 lines longer** in this version.

The difference is not a measure of which contract is more powerful. The WSDL contains SOAP-specific machinery such as XML Schema types, WSDL messages, a port type, a SOAP binding, SOAP action, and a service/port address. The OpenAPI document instead concentrates on HTTP resources, methods, parameters, request/response schemas, and status codes.

Two things the WSDL declared that this OpenAPI file does not need are:
- a **SOAP binding/transport and `soapAction`**;
- separate **WSDL message/port definitions** for the SOAP operation.

### 2. SOAP Fault replacement

One fault from Assignment 3 was:

> `<faultstring>Card Declined</faultstring>`

with the detail code `card_declined` and the message that the issuing bank declined the transaction.

In REST, this can become **HTTP 422 Unprocessable Content** with the common problem shape, for example:

```json
{
  "type": "https://campuseats.example.com/problems/payment-rejected",
  "title": "Payment Rejected",
  "status": 422,
  "detail": "The issuing bank declined this transaction."
}
```

Returning this failure inside a `200 OK` response is a problem because the network and HTTP-aware infrastructure would see a successful request. Monitoring, clients, proxies, caches, and retry logic commonly use the HTTP status code to distinguish success from failure. A real 4xx status therefore makes the failure visible at the protocol level instead of hiding it inside a successful response body.

### 3. UDDI: publish, find, bind

The three UDDI ideas do not disappear equally. **Publish** still exists conceptually because the service contract is published as `openapi.yaml`. **Find** and **bind** no longer require a UDDI registry in this assignment. API documentation/service discovery plus the configured service URL takes over finding the contract and endpoint, while the HTTP client binds to the resource URL at runtime.

In this implementation, `PAYMENTS_SERVICE_URL` supplies the Payments endpoint at runtime instead of hard-coding a partner address into the Orders code.

### 4. XML Schema validation vs OpenAPI validation

The specific function that carries the request-validation responsibility in the implementation is **`validate()` in `app.py`**. It is called before the request body fields are used.

Without this function, a request such as `{ "customerId": "C1", "item": "Thali", "quantity": 0, "amount": 120 }` could reach the application even though a valid order must have a positive quantity. Missing fields or wrong data types could also cause incorrect processing or an internal error instead of a controlled `400` response.

### 5. Where SOAP would still be preferred

I would still choose the SOAP stack at the **external PayFast payment integration boundary** if that partner continued to require the Assignment 3 SOAP contract. The guarantee being purchased is a formally defined **WSDL/XSD message and fault contract**, so both sides have an explicit, machine-readable agreement about the operation, request/response structure, and typed payment fault.

REST is preferable for the internal CampusEats Orders API here because HTTP resources and status codes express the service boundary more directly. SOAP is justified at the partner boundary when the partner contract itself requires those SOAP/WSDL guarantees.

## Validation and tests

The included `validator_output.txt` records the contract check as **0 errors**, and `pytest_output.txt` records **4 passed** tests.

The curl transcript demonstrates:
- successful create with `201 Created` and `Location`;
- repeated create with the same `Idempotency-Key` returning the original order;
- malformed JSON returning `400`;
- missing order returning `404`;
- cancellation followed by a second cancellation returning `409`.
