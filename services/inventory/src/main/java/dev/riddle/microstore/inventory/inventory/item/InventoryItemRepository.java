package dev.riddle.microstore.inventory.inventory.item;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface InventoryItemRepository extends
	JpaRepository<InventoryItem, UUID>,
	JpaSpecificationExecutor<InventoryItem>
{
	Optional<InventoryItem> findBySku(String sku);
	boolean existsBySku(String sku);
	void deleteBySku(String sku);
}
