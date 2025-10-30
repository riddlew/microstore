package dev.riddle.microstore.inventory.inventory.item;

public record ItemSpecificationFilter(
	String name,
	String sku,
	Integer minQuantity,
	Integer maxQuantity,
	Integer minPrice,
	Integer maxPrice
) {}
