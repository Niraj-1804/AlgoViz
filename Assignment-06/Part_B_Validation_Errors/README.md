# Part B — Validation and Error Responses
Use the same source files and `pom.xml` as Part A. The complete, standalone project is in the Part A folder; this folder provides the Part B-specific code snippets and cURL tests.

## Validation request DTO
`CreateOrderRequest.java`:
```java
package com.example.orders;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
public record CreateOrderRequest(
    @NotBlank(message = "Item is required") String item,
    @Min(value = 1, message = "Quantity must be at least 1") int quantity
) {}
```

## GlobalExceptionHandler.java
See the complete file in Part A. It returns 400 for validation/malformed JSON, and 404 for a missing order.

## cURL tests
Invalid quantity:
`curl -i -X POST http://localhost:8080/orders -H "Content-Type: application/json" -d '{"item":"Laptop","quantity":0}'`

Blank item:
`curl -i -X POST http://localhost:8080/orders -H "Content-Type: application/json" -d '{"item":"","quantity":1}'`

Malformed JSON:
`curl -i -X POST http://localhost:8080/orders -H "Content-Type: application/json" -d '{"item":"Laptop","quantity":}'`

Missing order:
`curl -i http://localhost:8080/orders/999`
