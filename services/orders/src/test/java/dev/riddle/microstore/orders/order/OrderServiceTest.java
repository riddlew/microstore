package dev.riddle.microstore.orders.order;

import dev.riddle.microstore.orders.inventory.InventoryClient;
import dev.riddle.microstore.orders.inventory.dto.InventoryItemResponse;
import dev.riddle.microstore.orders.order.dto.CreateOrderRequest;
import dev.riddle.microstore.orders.order.dto.OrderItemRequest;
import dev.riddle.microstore.orders.order.dto.OrderResponse;
import dev.riddle.microstore.orders.shared.error.InsufficientStockException;
import dev.riddle.microstore.orders.shared.error.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private InventoryClient inventoryClient;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrder_withSufficientStock_shouldSucceed() {
        // Given
        CreateOrderRequest request = new CreateOrderRequest(
                "John Doe",
                "john@example.com",
                List.of(new OrderItemRequest("TEST-SKU", 2))
        );

        InventoryItemResponse inventoryItem = new InventoryItemResponse(
                "TEST-SKU",
                "Test Product",
                "Description",
                1999, // $19.99 in cents
                10
        );

        when(inventoryClient.getItemBySku("TEST-SKU")).thenReturn(inventoryItem);
        when(inventoryClient.checkStockAvailability("TEST-SKU", 2)).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(UUID.randomUUID());
            return order;
        });

        OrderResponse expectedResponse = new OrderResponse(
                UUID.randomUUID(),
                "John Doe",
                "john@example.com",
                OrderStatus.CONFIRMED,
                3998, // $39.98 in cents
                List.of(),
                null,
                null
        );
        when(orderMapper.toResponse(any(Order.class))).thenReturn(expectedResponse);

        // When
        OrderResponse result = orderService.createOrder(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(OrderStatus.CONFIRMED);
        verify(orderRepository).save(any(Order.class));
        verify(inventoryClient).getItemBySku("TEST-SKU");
        verify(inventoryClient).checkStockAvailability("TEST-SKU", 2);
    }

    @Test
    void createOrder_withInsufficientStock_shouldThrowException() {
        // Given
        CreateOrderRequest request = new CreateOrderRequest(
                "John Doe",
                "john@example.com",
                List.of(new OrderItemRequest("TEST-SKU", 20))
        );

        InventoryItemResponse inventoryItem = new InventoryItemResponse(
                "TEST-SKU",
                "Test Product",
                "Description",
                1999,
                10
        );

        when(inventoryClient.getItemBySku("TEST-SKU")).thenReturn(inventoryItem);
        when(inventoryClient.checkStockAvailability("TEST-SKU", 20)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Insufficient stock");

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getOrderById_whenNotFound_shouldThrowException() {
        // Given
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> orderService.getOrderById(orderId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Order not found");
    }
}
