package dev.riddle.microstore.inventory.inventory.item;

import org.springframework.data.jpa.domain.Specification;

public final class ItemSpecifications {
	private ItemSpecifications() {}

	public static Specification<InventoryItem> nameContains(String name) {
		return (root, query, builder) -> {
			if (name == null || name.isBlank())
				return builder.conjunction();

			return builder.like(builder.lower(root.get("name")), "%" + name.toLowerCase()+ "%");
		};
	}

	public static Specification<InventoryItem> skuEquals(String sku) {
		return (root, query, builder) -> {
			if (sku == null || sku.isBlank())
				return builder.conjunction();

			return builder.equal(root.get("sku"), sku);
		};
	}

	public static Specification<InventoryItem> qtyGte(Integer min) {
		return (root, query, builder) -> {
			if (min == null)
				return builder.conjunction();

			return builder.greaterThanOrEqualTo(root.get("quantity"), min);
		};
	}

	public static Specification<InventoryItem> qtyLte(Integer max) {
		return (root, query, builder) -> {
			if (max == null)
				return builder.conjunction();

			return builder.lessThanOrEqualTo(root.get("quantity"), max);
		};
	}

	public static Specification<InventoryItem> priceGte(Integer min) {
		return (root, query, builder) -> {
			if (min == null)
				return builder.conjunction();

			return builder.greaterThanOrEqualTo(root.get("priceInCents"), min);
		};
	}

	public static Specification<InventoryItem> priceLte(Integer max) {
		return (root, query, builder) -> {
			if (max == null)
				return builder.conjunction();

			return builder.lessThanOrEqualTo(root.get("priceInCents"), max);
		};
	}
}
