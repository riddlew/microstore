package dev.riddle.microstore.orders.order;

import dev.riddle.microstore.orders.order.dto.OrderItemResponse;
import dev.riddle.microstore.orders.order.dto.OrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "items", source = "items")
    OrderResponse toResponse(Order order);

    OrderItemResponse toItemResponse(OrderItem orderItem);
}

