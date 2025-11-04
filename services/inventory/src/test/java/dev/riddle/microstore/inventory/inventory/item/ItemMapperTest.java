package dev.riddle.microstore.inventory.inventory.item;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.mapstruct.factory.Mappers;

import dev.riddle.microstore.inventory.inventory.item.dto.CreateItemRequest;
import dev.riddle.microstore.inventory.inventory.item.dto.ItemResponse;
import dev.riddle.microstore.inventory.testutil.ItemTestData;

public class ItemMapperTest {
	
	private final ItemMapper mapper = Mappers.getMapper(ItemMapper.class);

	@Test
	void shouldMapCreateRequestToEntity() {
		CreateItemRequest request = ItemTestData.createRequest();
		InventoryItem entity = mapper.toEntity(request);
		assertThat(entity.getSku()).isEqualTo(request.sku());
		assertThat(entity.getName()).isEqualTo(request.name());
		assertThat(entity.getDescription()).isEqualTo(request.description());
		assertThat(entity.getPriceInCents()).isEqualTo(request.priceInCents());
		assertThat(entity.getQuantity()).isEqualTo(request.quantity());
		assertThat(entity.getId()).isNull();
	}

	@Test
	void shouldMapEntityToResponse() {
		InventoryItem entity = ItemTestData.inventoryItem();
		ItemResponse response = mapper.toResponse(entity);
		assertThat(response.sku()).isEqualTo(entity.getSku());
		assertThat(response.name()).isEqualTo(entity.getName());
		assertThat(response.description()).isEqualTo(entity.getDescription());
		assertThat(response.priceInCents()).isEqualTo(entity.getPriceInCents());
		assertThat(response.quantity()).isEqualTo(entity.getQuantity());
		assertThat(response.createdAt()).isEqualTo(entity.getCreatedAt());
		assertThat(response.updatedAt()).isEqualTo(entity.getUpdatedAt());
	}
}
