package dev.riddle.microstore.inventory.testutil;

import dev.riddle.microstore.inventory.inventory.item.InventoryItem;
import dev.riddle.microstore.inventory.inventory.item.dto.CreateItemRequest;
import dev.riddle.microstore.inventory.inventory.item.dto.UpdateItemRequest;

public class ItemTestData {
	public static CreateItemRequest createRequest() {
		return new CreateItemRequest(
			"TEST-SKU-001",
			"Test Item",
			"Test description",
			100,
			1999
		);
	}

	public static CreateItemRequest createRequest(String sku) {
		return new CreateItemRequest(
			sku,
			"Test Item",
			"Test description",
			100,
			1999
		);
	}

	public static InventoryItem inventoryItem() {
		InventoryItem item = new InventoryItem();
		item.setSku("TEST-SKU-001");
		item.setName("Test Item");
		item.setDescription("Test description");
		item.setPriceInCents(1999);
		item.setQuantity(100);
		return item;
	}

	public static UpdateItemRequest updateRequest() {
		return new UpdateItemRequest(
			"Updated Name",
			"Updated description",
			100,
			1999
		);
	}
}
