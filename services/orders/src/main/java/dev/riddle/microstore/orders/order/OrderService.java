package dev.riddle.microstore.orders.order;

import dev.riddle.microstore.orders.inventory.InventoryClient;
import dev.riddle.microstore.orders.inventory.dto.InventoryItemResponse;
import dev.riddle.microstore.orders.order.dto.CreateOrderRequest;
import dev.riddle.microstore.orders.order.dto.OrderItemRequest;
import dev.riddle.microstore.orders.order.dto.OrderResponse;
import dev.riddle.microstore.orders.shared.error.InsufficientStockException;
import dev.riddle.microstore.orders.shared.error.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final InventoryClient inventoryClient;

    public OrderService(OrderRepository orderRepository, 
                       OrderMapper orderMapper,
                       InventoryClient inventoryClient) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.inventoryClient = inventoryClient;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating order for customer: {}", request.customerEmail());

        // Create order entity
        Order order = new Order();
        order.setCustomerName(request.customerName());
        order.setCustomerEmail(request.customerEmail());
        order.setStatus(OrderStatus.PENDING);

        // Process each order item
        for (OrderItemRequest itemRequest : request.items()) {
            processOrderItem(order, itemRequest);
        }

        // Recalculate total and save
        order.recalculateTotal();
        order.setStatus(OrderStatus.CONFIRMED);
        Order savedOrder = orderRepository.save(order);

        log.info("Order created successfully: {}", savedOrder.getId());
        return orderMapper.toResponse(savedOrder);
    }

    private void processOrderItem(Order order, OrderItemRequest itemRequest) {
        log.debug("Processing order item: {}", itemRequest.sku());

        // Fetch item details from inventory service
        InventoryItemResponse inventoryItem = inventoryClient.getItemBySku(itemRequest.sku());

        // Check stock availability
        if (!inventoryClient.checkStockAvailability(itemRequest.sku(), itemRequest.quantity())) {
            throw new InsufficientStockException(
                    "Insufficient stock for SKU: " + itemRequest.sku() +
                    ". Available: " + inventoryItem.quantity() +
                    ", Requested: " + itemRequest.quantity()
            );
        }

        // Create order item
        OrderItem orderItem = new OrderItem();
        orderItem.setSku(inventoryItem.sku());
        orderItem.setProductName(inventoryItem.name());
        orderItem.setQuantity(itemRequest.quantity());
        orderItem.setUnitPriceInCents(inventoryItem.priceInCents());
        orderItem.calculateSubtotal();

        order.addItem(orderItem);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
        return orderMapper.toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByCustomerEmail(String email) {
        return orderRepository.findByCustomerEmailOrderByCreatedAtDesc(email)
                .stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(orderMapper::toResponse)
                .toList();
    }
}
