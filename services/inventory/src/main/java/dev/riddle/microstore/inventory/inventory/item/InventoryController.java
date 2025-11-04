package dev.riddle.microstore.inventory.inventory.item;

import dev.riddle.microstore.inventory.inventory.item.dto.CreateItemRequest;
import dev.riddle.microstore.inventory.inventory.item.dto.ItemResponse;
import dev.riddle.microstore.inventory.inventory.item.dto.UpdateItemRequest;
import jakarta.servlet.ServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

	private final InventoryService inventoryService;

	@GetMapping
	public Page<ItemResponse> getItems(
		ItemSpecificationFilter filter,
		@PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable
	) {
		return inventoryService.getItemsByQuery(filter, pageable);
	}

	@GetMapping("/{sku}")
	public ItemResponse getItemById(@PathVariable("sku") String sku) {
		return inventoryService.getItemBySku(sku);
	}

	@PostMapping
	@ResponseStatus(org.springframework.http.HttpStatus.CREATED)
	public ItemResponse createItem(@RequestBody @Valid CreateItemRequest request) {
		return inventoryService.createItem(request);
	}

	@PatchMapping("{sku}")
	public ItemResponse updateItem(
		@PathVariable("sku") String sku,
		@RequestBody @Valid UpdateItemRequest request
	) {
		return inventoryService.updateItem(sku, request);
	}

	@DeleteMapping("{sku}")
	@ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
	public void deleteItem(@PathVariable("sku") String sku) {
		inventoryService.deleteItem(sku);
	}
}
