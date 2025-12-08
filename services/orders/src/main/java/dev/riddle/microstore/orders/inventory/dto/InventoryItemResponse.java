package dev.riddle.microstore.orders.inventory.dto;

public record InventoryItemResponse(
        String sku,
        String name,
        String description,
        Integer priceInCents,
        Integer quantity
) {
}
