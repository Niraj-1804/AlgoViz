# Part C — Idempotency-Key
This folder contains the Part C changes. Start from the standalone Part A project, then apply the code below.

## 1. Add to OrderService.java
Add this import:
`import java.util.HashMap;` (or keep `java.util.*;`)

Add this field inside `OrderService`:
```java
private final Map<String, Order> idempotencyKeys = new HashMap<>();
```

Replace/add this overload:
```java
public synchronized Order create(CreateOrderRequest request, String key) {
    if (key == null || key.isBlank()) {
        throw new IllegalArgumentException("Idempotency-Key header is required");
    }
    Order existing = idempotencyKeys.get(key);
    if (existing != null) return existing;

    Order created = create(request);
    idempotencyKeys.put(key, created);
    return created;
}
```

## 2. Replace the POST method in OrderController.java
```java
@PostMapping
public ResponseEntity<Order> create(
        @Valid @RequestBody CreateOrderRequest request,
        @RequestHeader("Idempotency-Key") String key) {
    Order order = service.create(request, key);
    return ResponseEntity.created(URI.create("/orders/" + order.id())).body(order);
}
```
This minimal version returns the same order on retry. For a production-ready implementation, distinguish replayed requests (usually 200 OK), persist keys in a database, and reject reuse of the same key with a different request body.

## cURL
First request:
`curl -i -X POST http://localhost:8080/orders -H "Content-Type: application/json" -H "Idempotency-Key: order-abc-123" -d '{"item":"Laptop","quantity":2}'`

Repeat the exact request to verify the same order ID is returned.
