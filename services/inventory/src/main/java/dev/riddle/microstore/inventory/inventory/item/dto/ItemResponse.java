package dev.riddle.microstore.inventory.inventory.item.dto;

import java.time.Instant;
import java.util.UUID;

public record ItemResponse(
	UUID id,
	String sku,
	String name,
	String description,
	int quantity,
	int priceInCents,
	Instant createdAt,
	Instant updatedAt
) {}
