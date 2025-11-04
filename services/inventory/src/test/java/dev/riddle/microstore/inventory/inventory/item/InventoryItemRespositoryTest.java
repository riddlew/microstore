package dev.riddle.microstore.inventory.inventory.item;

import dev.riddle.microstore.inventory.testutil.ItemTestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class InventoryItemRespositoryTest {

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
	private InventoryItemRepository inventoryItemRepository;

	@Test
	void shouldSaveAndFindBySku() {
		InventoryItem inventoryItem = ItemTestData.inventoryItem();
		InventoryItem savedInventoryItem = inventoryItemRepository.save(inventoryItem);

		assertThat(savedInventoryItem.getId()).isNotNull();

		Optional<InventoryItem> foundInventoryItem = inventoryItemRepository.findBySku(savedInventoryItem.getSku());
		assertThat(foundInventoryItem).isPresent();
		assertThat(foundInventoryItem.get().getSku()).isEqualTo(inventoryItem.getSku());
		assertThat(foundInventoryItem.get().getName()).isEqualTo(inventoryItem.getName());
		assertThat(foundInventoryItem.get().getDescription()).isEqualTo(inventoryItem.getDescription());
		assertThat(foundInventoryItem.get().getPriceInCents()).isEqualTo(inventoryItem.getPriceInCents());
	}

	@Test
	void findBySku_whenNotExists_shouldReturnEmpty() {
		Optional<InventoryItem> foundItem = inventoryItemRepository.findBySku("DOESNT-EXIST");

		assertThat(foundItem).isEmpty();
	}
}
