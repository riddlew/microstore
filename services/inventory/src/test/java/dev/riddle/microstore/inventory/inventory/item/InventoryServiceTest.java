package dev.riddle.microstore.inventory.inventory.item;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.riddle.microstore.inventory.inventory.item.dto.CreateItemRequest;
import dev.riddle.microstore.inventory.inventory.item.dto.ItemResponse;
import dev.riddle.microstore.inventory.inventory.item.dto.UpdateItemRequest;
import dev.riddle.microstore.inventory.shared.error.NotFoundException;
import dev.riddle.microstore.inventory.testutil.ItemTestData;

@ExtendWith(MockitoExtension.class)
public class InventoryServiceTest {

	@Mock
	private InventoryItemRepository repository;

	@Mock
	private ItemMapper mapper;

	@InjectMocks
	private InventoryService service;

	@Test
	void createItem_shouldSaveAndReturnResponse() {
		CreateItemRequest request = ItemTestData.createRequest();
		InventoryItem entity = ItemTestData.inventoryItem();
		ItemResponse response = new ItemResponse(
				null,
				entity.getSku(),
				entity.getName(),
				entity.getDescription(),
				entity.getPriceInCents(),
				entity.getQuantity(),
				entity.getCreatedAt(),
				entity.getUpdatedAt());

		when(mapper.toEntity(request))
			.thenReturn(entity);
		when(repository.save(any(InventoryItem.class)))
			.thenReturn(entity);
		when(mapper.toResponse(entity))
			.thenReturn(response);

		ItemResponse result = service.createItem(request);

		assertThat(result).isEqualTo(response);
		verify(repository).save(entity);
	}

	@Test
	void getItemBySku_whenNotFound_shouldThrowNotFoundException() {
		String sku = "NON-EXISTENT";
		when(repository.findBySku(sku)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.getItemBySku(sku))
				.isInstanceOf(NotFoundException.class)
				.hasMessageContaining(sku);
	}

	@Test
	void getItemBySku_whenFound_shouldReturnResponse() {
		InventoryItem entity = ItemTestData.inventoryItem();
		ItemResponse response = new ItemResponse(
				null,
				entity.getSku(),
				entity.getName(),
				entity.getDescription(),
				entity.getPriceInCents(),
				entity.getQuantity(),
				entity.getCreatedAt(),
				entity.getUpdatedAt());

		when(repository.findBySku(entity.getSku()))
			.thenReturn(Optional.of(entity));
		when(mapper.toResponse(entity))
			.thenReturn(response);

		ItemResponse result = service.getItemBySku(entity.getSku());

		assertThat(result).isEqualTo(response);
	}

	@Test
	void updateItem_shouldUpdateOnlyProvidedFields() {
		String sku = "TEST-SKU-001";
		InventoryItem existing = ItemTestData.inventoryItem();
		UpdateItemRequest updateRequest = ItemTestData.updateRequest();

		when(repository.findBySku(sku))
			.thenReturn(Optional.of(existing));
		when(repository.save(any(InventoryItem.class)))
			.thenReturn(existing);
		when(mapper.toResponse(any(InventoryItem.class)))
			.thenReturn(
				new ItemResponse(null,
					sku,
					updateRequest.name(),
					updateRequest.description(),
					updateRequest.priceInCents(),
					updateRequest.quantity(),
					existing.getCreatedAt(),
					existing.getUpdatedAt()));

		ItemResponse result = service.updateItem(sku, updateRequest);

		assertThat(result.name())
			.isEqualTo(updateRequest.name());
		verify(repository).save(existing);
	}

	@Test
	void deleteItem_whenNotFound_shouldThrowNotFoundException() {
		String sku = "NON-EXISTENT";
		when(repository.existsBySku(sku))
			.thenReturn(false);

		assertThatThrownBy(() -> service
			.deleteItem(sku))
			.isInstanceOf(NotFoundException.class);
	}

	@Test
	void deleteItem_whenFound_shouldDelete() {
		InventoryItem entity = ItemTestData.inventoryItem();
		when(repository.existsBySku(entity.getSku()))
			.thenReturn(true);

		service.deleteItem(entity.getSku());

		verify(repository).deleteBySku(entity.getSku());
	}
}
