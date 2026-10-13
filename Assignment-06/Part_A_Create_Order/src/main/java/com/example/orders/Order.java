package com.example.orders;

public record Order(Long id, String item, int quantity, String status) {}
