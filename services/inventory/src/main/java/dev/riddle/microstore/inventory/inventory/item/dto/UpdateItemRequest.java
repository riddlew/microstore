package dev.riddle.microstore.inventory.inventory.item.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateItemRequest(
	@Nullable
	@Size(min = 1)
	String name,

	@Nullable
	@Size(min = 1)
	String description,

	@Nullable
	@Min(0)
	Integer quantity,

	@Nullable
	@Min(0)
	Integer priceInCents
) {}
