# Part D — State Conflict (409)
Start from the standalone Part A project and apply these changes.

## 1. Create OrderConflictException.java
```java
package com.example.orders;
public class OrderConflictException extends RuntimeException {
    public OrderConflictException(String message) { super(message); }
}
```

## 2. Add to OrderService.java
```java
public synchronized Order cancel(long id) {
    Order old = findById(id);
    if (!old.status().equals("CREATED")) {
        throw new OrderConflictException("Only CREATED orders can be cancelled");
    }
    Order updated = new Order(old.id(), old.item(), old.quantity(), "CANCELLED");
    orders.put(id, updated);
    return updated;
}
```

## 3. Add to GlobalExceptionHandler.java
```java
@ExceptionHandler(OrderConflictException.class)
public ResponseEntity<Map<String, Object>> conflict(OrderConflictException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
        "status", 409, "error", "Conflict", "message", ex.getMessage()));
}
```

## 4. Add to OrderController.java
```java
@PostMapping("/{id}/cancel")
public Order cancel(@PathVariable long id) {
    return service.cancel(id);
}
```

## cURL
Create an order first, then:
`curl -i -X POST http://localhost:8080/orders/1/cancel`
Repeat the cancellation; expected status is `409 Conflict`.
