package dev.riddle.microstore.orders.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class OrderValidationIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("inventory.service.url", () -> "http://localhost:8888");
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldReturn400WhenOrderIdIsInvalidUUID() {
        // When - Request order with invalid UUID format
        ResponseEntity<ProblemDetail> response = restTemplate
                .withBasicAuth("orders-service", "secret")
                .getForEntity("/api/orders/not-a-valid-uuid", ProblemDetail.class);

        // Then - Should return 400 Bad Request
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Invalid Parameter");
        assertThat(response.getBody().getDetail()).contains("orderId");
    }

    @Test
    void shouldReturn404WhenOrderIdDoesNotExist() {
        // When - Request order with valid UUID that doesn't exist
        ResponseEntity<ProblemDetail> response = restTemplate
                .withBasicAuth("orders-service", "secret")
                .getForEntity("/api/orders/00000000-0000-0000-0000-000000000000", ProblemDetail.class);

        // Then - Should return 404 Not Found
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Not Found");
    }

    @Test
    void shouldReturn400WhenOrderIdHasInvalidCharacters() {
        // When - Request order with malformed UUID
        ResponseEntity<ProblemDetail> response = restTemplate
                .withBasicAuth("orders-service", "secret")
                .getForEntity("/api/orders/12345-invalid", ProblemDetail.class);

        // Then - Should return 400 Bad Request
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Invalid Parameter");
    }
}

