package dev.riddle.microstore.inventory.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.riddle.microstore.inventory.inventory.item.dto.CreateItemRequest;
import dev.riddle.microstore.inventory.inventory.item.dto.ItemResponse;
import dev.riddle.microstore.inventory.testutil.ItemTestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class InventoryApiIntegrationTest {

	@Container
	static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:16-alpine")
		.withDatabaseName("inventory-test")
		.withUsername("test")
		.withPassword("test");

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
		registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
		registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
	}

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void shouldCreateAndRetrieveItem() {

		CreateItemRequest createRequest = ItemTestData.createRequest("INTEG-001");

		ResponseEntity<ItemResponse> createResponse = restTemplate.postForEntity(
			"/api/inventory",
			createRequest,
			ItemResponse.class
		);

		assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(createResponse.getBody()).isNotNull();
		assertThat(createResponse.getBody().sku()).isEqualTo(createRequest.sku());

		ResponseEntity<ItemResponse> getResponse = restTemplate.getForEntity(
			"/api/inventory/" + createRequest.sku(),
			ItemResponse.class
		);

		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(getResponse.getBody()).isNotNull();
		assertThat(getResponse.getBody().sku()).isEqualTo(createRequest.sku());
		assertThat(getResponse.getBody().name()).isEqualTo(createRequest.name());

	}

	@Test
	void getItem_whenNotFound_shouldReturnNotFound() {
		ResponseEntity<String> response = restTemplate.getForEntity(
			"/api/inventory/DOES-NOT-EXIST",
			String.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getHeaders().getContentType().toString())
			.contains("application/problem+json");
	}

}
