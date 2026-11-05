# Part A — Create Order
Java 17 + Spring Boot 3. Run with `mvn spring-boot:run`.

## cURL (Linux/macOS/Git Bash)
Create: `curl -i -X POST http://localhost:8080/orders -H "Content-Type: application/json" -d '{"item":"Laptop","quantity":2}'`
List: `curl -i http://localhost:8080/orders`
Get one: `curl -i http://localhost:8080/orders/1`
Missing order: `curl -i http://localhost:8080/orders/999`

Note: This uses in-memory storage; data resets when the app restarts.
