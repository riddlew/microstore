package dev.riddle.microstore.inventory.inventory.item.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateItemRequest(
	@Size(min = 8, max = 12)
	String sku,

	@NotBlank
	String name,

	String description,

	@Min(0)
	int quantity,

	@Min(0)
	int priceInCents
) {}
