package dev.riddle.microstore.orders.inventory;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import dev.riddle.microstore.orders.inventory.dto.InventoryItemResponse;
import dev.riddle.microstore.orders.shared.error.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WireMockTest
class InventoryClientTest {

    private InventoryClient inventoryClient;
    private String baseUrl;

    @BeforeEach
    void setUp(WireMockRuntimeInfo wmRuntimeInfo) {
        baseUrl = wmRuntimeInfo.getHttpBaseUrl();

        // Mock OAuth2 setup (no actual token exchange in tests)
        ClientRegistration registration = ClientRegistration
                .withRegistrationId("orders-service")
                .clientId("orders-service")
                .clientSecret("secret")
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .tokenUri("http://localhost:9000/oauth2/token")
                .build();

        InMemoryClientRegistrationRepository registrationRepository =
                new InMemoryClientRegistrationRepository(registration);
        InMemoryOAuth2AuthorizedClientService authorizedClientService =
                new InMemoryOAuth2AuthorizedClientService(registrationRepository);
        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                        registrationRepository, authorizedClientService);

        inventoryClient = new InventoryClientImpl(baseUrl, authorizedClientManager);
    }

    @Test
    void getItemBySku_whenItemExists_shouldReturnItem() {
        // Given
        String sku = "TEST-SKU";
        stubFor(get(urlEqualTo("/api/inventory/" + sku))
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

        // When
        InventoryItemResponse result = inventoryClient.getItemBySku(sku);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.sku()).isEqualTo("TEST-SKU");
        assertThat(result.name()).isEqualTo("Test Product");
        assertThat(result.priceInCents()).isEqualTo(1999);
        assertThat(result.quantity()).isEqualTo(100);

        verify(getRequestedFor(urlEqualTo("/api/inventory/" + sku)));
    }

    @Test
    void getItemBySku_whenItemNotFound_shouldThrowException() {
        // Given
        String sku = "NON-EXISTENT";
        stubFor(get(urlEqualTo("/api/inventory/" + sku))
                .willReturn(aResponse()
                        .withStatus(404)));

        // When/Then
        assertThatThrownBy(() -> inventoryClient.getItemBySku(sku))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void checkStockAvailability_withSufficientStock_shouldReturnTrue() {
        // Given
        String sku = "TEST-SKU";
        stubFor(get(urlEqualTo("/api/inventory/" + sku))
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

        // When
        boolean result = inventoryClient.checkStockAvailability(sku, 50);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void checkStockAvailability_withInsufficientStock_shouldReturnFalse() {
        // Given
        String sku = "TEST-SKU";
        stubFor(get(urlEqualTo("/api/inventory/" + sku))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                    "sku": "TEST-SKU",
                                    "name": "Test Product",
                                    "description": "Test Description",
                                    "priceInCents": 1999,
                                    "quantity": 10
                                }
                                """)));

        // When
        boolean result = inventoryClient.checkStockAvailability(sku, 50);

        // Then
        assertThat(result).isFalse();
    }
}
