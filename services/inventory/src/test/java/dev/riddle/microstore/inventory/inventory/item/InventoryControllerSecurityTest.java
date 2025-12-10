package dev.riddle.microstore.inventory.inventory.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.riddle.microstore.inventory.inventory.item.dto.CreateItemRequest;
import dev.riddle.microstore.inventory.inventory.item.dto.ItemResponse;
import dev.riddle.microstore.inventory.testutil.ItemTestData;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;
import dev.riddle.microstore.inventory.config.ResourceServerConfig;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = InventoryController.class,
    excludeAutoConfiguration = {OAuth2ResourceServerAutoConfiguration.class}
)
@Import(ResourceServerConfig.class)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class InventoryControllerSecurityTest {

    private final MockMvc mockMvc;

    private final ObjectMapper objectMapper;

    @MockitoBean
    private InventoryService service;

    // Provide a JwtDecoder bean so SecurityFilterChain can initialize
    @MockitoBean
    private JwtDecoder jwtDecoder;

    InventoryControllerSecurityTest(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @Test
    void getItem_withoutAuth_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/inventory/TEST-SKU"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getItem_withReadScope_shouldReturn200() throws Exception {
        ItemResponse response = new ItemResponse(
                UUID.randomUUID(),
                "TEST-SKU",
                "Test Item",
                "Description",
                100,
                1999,
                Instant.now(),
                Instant.now()
        );

        when(service.getItemBySku("TEST-SKU")).thenReturn(response);

        mockMvc.perform(get("/api/inventory/TEST-SKU")
                        .with(jwt().authorities(() -> "SCOPE_inventory.read")))
                .andExpect(status().isOk());
    }

    @Test
    void createItem_withoutWriteScope_shouldReturn403() throws Exception {
        CreateItemRequest request = ItemTestData.createRequest();

        mockMvc.perform(post("/api/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(jwt().authorities(() -> "SCOPE_inventory.read")))
                .andExpect(status().isForbidden());
    }

    @Test
    void createItem_withWriteScope_shouldReturn201() throws Exception {
        CreateItemRequest request = ItemTestData.createRequest();
        ItemResponse response = new ItemResponse(
                UUID.randomUUID(),
                request.sku(),
                request.name(),
                request.description(),
                request.quantity(),
                request.priceInCents(),
                Instant.now(),
                Instant.now()
        );

        when(service.createItem(any(CreateItemRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(jwt().authorities(() -> "SCOPE_inventory.write")))
                .andExpect(status().isCreated());
    }
}

