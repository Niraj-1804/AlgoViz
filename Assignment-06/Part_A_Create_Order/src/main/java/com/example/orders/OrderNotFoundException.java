package com.example.orders;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(long id) {
        super("Order " + id + " not found");
    }
}
