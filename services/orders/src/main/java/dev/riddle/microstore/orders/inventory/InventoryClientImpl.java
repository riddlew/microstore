package dev.riddle.microstore.orders.inventory;

import dev.riddle.microstore.orders.inventory.dto.InventoryItemResponse;
import dev.riddle.microstore.orders.shared.error.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class InventoryClientImpl implements InventoryClient {

    private static final Logger log = LoggerFactory.getLogger(InventoryClientImpl.class);
    
    private final RestClient restClient;
    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private static final String CLIENT_REGISTRATION_ID = "orders-service";

    public InventoryClientImpl(
            @Value("${inventory.service.url}") String inventoryServiceUrl,
            OAuth2AuthorizedClientManager authorizedClientManager) {
        
        this.authorizedClientManager = authorizedClientManager;
        
        this.restClient = RestClient.builder()
                .baseUrl(inventoryServiceUrl)
                .requestInterceptor((request, body, execution) -> {
                    // Get OAuth2 access token
                    var authorizeRequest = OAuth2AuthorizeRequest
                            .withClientRegistrationId(CLIENT_REGISTRATION_ID)
                            .principal("orders-service")
                            .build();
                    
                    var authorizedClient = authorizedClientManager.authorize(authorizeRequest);
                    
                    if (authorizedClient != null && authorizedClient.getAccessToken() != null) {
                        request.getHeaders().setBearerAuth(authorizedClient.getAccessToken().getTokenValue());
                    }
                    
                    return execution.execute(request, body);
                })
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, (request, response) -> {
                    if (response.getStatusCode().value() == 404) {
                        throw new NotFoundException("Inventory item not found");
                    }
                })
                .build();
    }

    @Override
    public InventoryItemResponse getItemBySku(String sku) {
        log.debug("Fetching inventory item: {}", sku);
        
        return restClient.get()
                .uri("/api/inventory/{sku}", sku)
                .retrieve()
                .body(InventoryItemResponse.class);
    }

    @Override
    public boolean checkStockAvailability(String sku, int quantity) {
        log.debug("Checking stock availability for SKU: {}, quantity: {}", sku, quantity);
        
        InventoryItemResponse item = getItemBySku(sku);
        boolean available = item.quantity() >= quantity;
        
        log.debug("Stock check result for {}: available={}, current stock={}, requested={}",
                sku, available, item.quantity(), quantity);
        
        return available;
    }
}

