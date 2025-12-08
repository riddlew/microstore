package dev.riddle.microstore.orders.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StockReservationRequest(
        @NotBlank
        String sku,

        @NotNull
        @Min(1)
        Integer quantity
) {
}

