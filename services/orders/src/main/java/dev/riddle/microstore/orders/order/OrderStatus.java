package dev.riddle.microstore.orders.order;

public enum OrderStatus {
    PENDING,      // Order created, awaiting stock confirmation
    CONFIRMED,    // Stock reserved, order confirmed
    REJECTED,     // Insufficient stock or other error
    CANCELLED     // Order cancelled by user/admin
}

