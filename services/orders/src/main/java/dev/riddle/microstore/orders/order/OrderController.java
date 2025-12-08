package dev.riddle.microstore.orders.order;

import dev.riddle.microstore.orders.order.dto.CreateOrderRequest;
import dev.riddle.microstore.orders.order.dto.OrderResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_inventory.write')")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAuthority('SCOPE_inventory.read')")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID orderId) {
        OrderResponse response = orderService.getOrderById(orderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_inventory.read')")
    public ResponseEntity<List<OrderResponse>> getAllOrders(
            @RequestParam(required = false) String customerEmail) {
        
        List<OrderResponse> orders = customerEmail != null
                ? orderService.getOrdersByCustomerEmail(customerEmail)
                : orderService.getAllOrders();
        
        return ResponseEntity.ok(orders);
    }
}

