package dev.riddle.microstore.orders.integration;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import dev.riddle.microstore.orders.order.dto.CreateOrderRequest;
import dev.riddle.microstore.orders.order.dto.OrderItemRequest;
import dev.riddle.microstore.orders.order.dto.OrderResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@WireMockTest(httpPort = 8888)
class OrderCreationIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Use Testcontainers-provided JDBC URL (mapped host/port, via TESTCONTAINERS_HOST_OVERRIDE).
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("inventory.service.url", () -> "http://localhost:8888");
    }

    private final TestRestTemplate restTemplate;

    OrderCreationIT(TestRestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Test
    void shouldCreateOrderWhenStockIsAvailable() {
        // Given - Mock inventory service response
        stubFor(get(urlEqualTo("/api/inventory/TEST-SKU"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "sku": "TEST-SKU",
                                    "name": "Test Product",
                                    "description": "Test Description",
                                    "priceInCents": 1999,
                                    "quantity": 100
                                }
                                """)));

        CreateOrderRequest request = new CreateOrderRequest(
                "John Doe",
                "john@example.com",
                List.of(new OrderItemRequest("TEST-SKU", 2))
        );

        // When
        ResponseEntity<OrderResponse> response = restTemplate
                .withBasicAuth("orders-service", "secret")
                .postForEntity("/api/orders", request, OrderResponse.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().customerName()).isEqualTo("John Doe");
        assertThat(response.getBody().items()).hasSize(1);
    }
}
