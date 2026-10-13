package com.example.orders;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OrderService {
    private final Map<Long, Order> orders = new HashMap<>();
    private final AtomicLong ids = new AtomicLong();

    public synchronized Order create(CreateOrderRequest request) {
        long id = ids.incrementAndGet();
        Order order = new Order(id, request.item(), request.quantity(), "CREATED");
        orders.put(id, order);
        return order;
    }

    public synchronized List<Order> findAll() {
        return new ArrayList<>(orders.values());
    }

    public synchronized Order findById(long id) {
        Order order = orders.get(id);
        if (order == null) throw new OrderNotFoundException(id);
        return order;
    }
}
