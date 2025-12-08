package dev.riddle.microstore.orders.order.dto;

import dev.riddle.microstore.orders.order.OrderStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String customerName,
        String customerEmail,
        OrderStatus status,
        Integer totalAmountInCents,
        List<OrderItemResponse> items,
        Instant createdAt,
        Instant updatedAt
) {
}
