package dev.riddle.microstore.inventory.inventory.item;

import dev.riddle.microstore.inventory.inventory.item.dto.CreateItemRequest;
import dev.riddle.microstore.inventory.inventory.item.dto.ItemResponse;
import dev.riddle.microstore.inventory.inventory.item.dto.UpdateItemRequest;
import dev.riddle.microstore.inventory.shared.error.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static dev.riddle.microstore.inventory.inventory.item.ItemSpecifications.*;

@Service
@RequiredArgsConstructor
public class InventoryService {
	private final InventoryItemRepository inventoryItemRepository;
	private final ItemMapper mapper;

	@Transactional
	public ItemResponse createItem(CreateItemRequest request) {
		InventoryItem item = mapper.toEntity(request);
		return mapper.toResponse(inventoryItemRepository.save(item));
	}

	@Transactional(readOnly = true)
	public Page<ItemResponse> getItemsByQuery(
		ItemSpecificationFilter filter,
		@PageableDefault(size = 20, sort = "name") Pageable pageable
	) {
		Specification<InventoryItem> spec = Specification
			.allOf(
				nameContains(filter.name()),
				skuEquals(filter.sku()),
				qtyGte(filter.minQuantity()),
				qtyLte(filter.maxPrice()),
				priceGte(filter.minPrice()),
				priceLte(filter.maxPrice())
			);

		return inventoryItemRepository
			.findAll(spec, pageable)
			.map(mapper::toResponse);
	}

	@Transactional(readOnly = true)
	public ItemResponse getItemBySku(String sku) {
		InventoryItem item = inventoryItemRepository
			.findBySku(sku)
			.orElseThrow(() -> new NotFoundException("InventoryItem", sku));

		return mapper.toResponse(item);
	}

	@Transactional
	public ItemResponse updateItem(String sku, UpdateItemRequest request) {
		InventoryItem item = inventoryItemRepository
			.findBySku(sku)
			.orElseThrow(() -> new NotFoundException("InventoryItem", sku));

		mapper.update(item, request);
		return mapper.toResponse(inventoryItemRepository.save(item));
	}

	@Transactional
	public void deleteItem(String sku) {
		if (!inventoryItemRepository.existsBySku(sku)) {
			throw new NotFoundException("InventoryItem", sku);
		}

		inventoryItemRepository.deleteBySku(sku);
	}
}
