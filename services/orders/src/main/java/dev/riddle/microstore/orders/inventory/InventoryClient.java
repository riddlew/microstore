package dev.riddle.microstore.orders.inventory;

import dev.riddle.microstore.orders.inventory.dto.InventoryItemResponse;

/**
 * Client interface for communicating with the Inventory service.
 */
public interface InventoryClient {
    
    /**
     * Fetches inventory item by SKU.
     * 
     * @param sku the SKU to look up
     * @return the inventory item details
     * @throws dev.riddle.microstore.orders.shared.error.NotFoundException if item not found
     */
    InventoryItemResponse getItemBySku(String sku);

    /**
     * Checks if sufficient stock is available.
     * 
     * @param sku the SKU to check
     * @param quantity the desired quantity
     * @return true if sufficient stock is available
     */
    boolean checkStockAvailability(String sku, int quantity);
}

