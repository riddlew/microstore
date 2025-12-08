package dev.riddle.microstore.orders.order.dto;

import java.util.UUID;

public record OrderItemResponse(
        UUID id,
        String sku,
        String productName,
        Integer quantity,
        Integer unitPriceInCents,
        Integer subtotalInCents
) {
}
