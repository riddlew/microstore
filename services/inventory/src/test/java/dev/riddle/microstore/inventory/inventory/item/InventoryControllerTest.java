package dev.riddle.microstore.inventory.inventory.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.riddle.microstore.inventory.inventory.item.dto.CreateItemRequest;
import dev.riddle.microstore.inventory.inventory.item.dto.ItemResponse;
import dev.riddle.microstore.inventory.shared.error.NotFoundException;
import dev.riddle.microstore.inventory.testutil.ItemTestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventoryController.class)
public class InventoryControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private InventoryService inventoryService;

	@Test
	void createItem_withValidRequest_shouldReturnCreatedItem() throws Exception {
		CreateItemRequest request = ItemTestData.createRequest();
		ItemResponse response = new ItemResponse(
			null,
			request.sku(),
			request.name(),
			request.description(),
			request.priceInCents(),
			request.quantity(),
			Instant.now(),
			Instant.now()
		);

		when(inventoryService.createItem(request))
			.thenReturn(response);

		mockMvc.perform(post("/api/inventory")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.sku").value(request.sku()));

	}

	@Test
	void createItem_withInvalidRequest_shouldReturnBadRequest() throws Exception {
		CreateItemRequest request = new CreateItemRequest(
			"", // Blank SKU = invalid request
			"Name",
			"Request",
			10000,
			1
		);

		mockMvc.perform(post("/api/inventory")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
			.andExpect(jsonPath("$.title").exists())
			.andExpect(jsonPath("$.status").exists());
	}

	@Test
	void getItemBySku_whenNotFound_shouldReturnNotFound() throws Exception {
		String sku = "NOT_IN_DATABASE_SKU";

		when(inventoryService.getItemBySku(sku)).thenThrow(new NotFoundException("inventoryControllerTest", sku));

		mockMvc.perform(get("/api/inventory/{sku}", sku))
			.andExpect(status().isNotFound())
			.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
			.andExpect(jsonPath("$.title").exists())
			.andExpect(jsonPath("$.status").exists())
			.andExpect(jsonPath("$.detail").value(containsString(sku)));
	}

	@Test
	void getItemsBySku_whenFound_shouldReturnFoundItems() throws Exception {
		String sku = "TEST-SKU-001";
		ItemResponse response = new ItemResponse(
			null,
			sku,
			"Test Item",
			"Test Description",
			1999,
			1,
			Instant.now(),
			Instant.now()
		);

		when(inventoryService.getItemBySku(sku)).thenReturn(response);

		mockMvc.perform(get("/api/inventory/{sku}", sku))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.sku").value(sku))
			.andExpect(jsonPath("$.name").value("Test Item"));
	}
}
