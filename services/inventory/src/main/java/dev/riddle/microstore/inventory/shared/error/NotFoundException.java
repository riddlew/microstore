package dev.riddle.microstore.inventory.shared.error;

public class NotFoundException extends RuntimeException {
	public NotFoundException(String resource, String sku) {
		super(resource + " not found with SKU " + sku);
	}
}
