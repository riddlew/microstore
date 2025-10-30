package dev.riddle.microstore.inventory.inventory.item;

import dev.riddle.microstore.inventory.inventory.item.dto.CreateItemRequest;
import dev.riddle.microstore.inventory.inventory.item.dto.ItemResponse;
import dev.riddle.microstore.inventory.inventory.item.dto.UpdateItemRequest;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ItemMapper {
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	InventoryItem toEntity(CreateItemRequest request);

	ItemResponse toResponse(InventoryItem item);

	// Updates the existing entity with the values from the request
	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	void update(@MappingTarget InventoryItem target, UpdateItemRequest request);
}
