package dev.riddle.microstore.inventory.inventory;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import dev.riddle.microstore.inventory.inventory.item.dto.CreateItemRequest;
import dev.riddle.microstore.inventory.inventory.item.dto.ItemResponse;
import dev.riddle.microstore.inventory.testutil.ItemTestData;
import dev.riddle.microstore.inventory.testutil.JwtTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestConstructor;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Testcontainers
public class InventoryApiIntegrationTest {

	@RegisterExtension
	static WireMockExtension wireMock = WireMockExtension.newInstance()
		.options(wireMockConfig().dynamicPort())
		.build();

	@Container
	static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:16-alpine")
		.withDatabaseName("inventory-test")
		.withUsername("test")
		.withPassword("test");

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		// Use Testcontainers-provided JDBC URL (mapped host/port, via TESTCONTAINERS_HOST_OVERRIDE).
		registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
		registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
		registry.add("spring.datasource.password", postgreSQLContainer::getPassword);

		// Wire the resource server to the WireMock JWKS/issuer (dynamic port).
		registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
			() -> wireMock.getRuntimeInfo().getHttpBaseUrl());
		registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
			() -> wireMock.getRuntimeInfo().getHttpBaseUrl() + "/.well-known/jwks.json");
	}

	@BeforeEach
	void setUp() {
		var baseUrl = wireMock.getRuntimeInfo().getHttpBaseUrl();

		// Mock JWKS endpoint
		wireMock.stubFor(get(urlEqualTo("/.well-known/jwks.json"))
			.willReturn(aResponse()
				.withHeader("Content-Type", "application/json")
				.withBody(JwtTestUtil.getJwksJson())));

		// Mock OpenID Configuration
		wireMock.stubFor(get(urlEqualTo("/.well-known/openid-configuration"))
			.willReturn(aResponse()
				.withHeader("Content-Type", "application/json")
				.withBody(String.format("""
					{
						"issuer": "%s",
						"jwks_uri": "%s/.well-known/jwks.json"
					}
					""", baseUrl, baseUrl))));
	}

	private final TestRestTemplate restTemplate;

	InventoryApiIntegrationTest(TestRestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	private String getAuthToken(List<String> scopes) {
		return JwtTestUtil.generateTestJwt("test-client", scopes);
	}

	private HttpHeaders createAuthHeaders(String token) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(token);
		return headers;
	}

	@TestConfiguration
	static class JwtTestConfig {
		@Bean
		JwtDecoder jwtDecoder() {
			String jwkSetUri = wireMock.getRuntimeInfo().getHttpBaseUrl() + "/.well-known/jwks.json";
			return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
		}
	}

	@Test
	void shouldCreateAndRetrieveItem() {
		String token = getAuthToken(List.of("inventory.read", "inventory.write"));
		HttpHeaders headers = createAuthHeaders(token);

		CreateItemRequest createRequest = ItemTestData.createRequest("INTEG-001");

		// First attempt without auth should fail
		ResponseEntity<ItemResponse> createResponseWithoutAuth = restTemplate.postForEntity(
			"/api/inventory",
			createRequest,
			ItemResponse.class
		);
		assertThat(createResponseWithoutAuth.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

		// Now with auth token
		HttpEntity<CreateItemRequest> createRequestEntity = new HttpEntity<>(createRequest, headers);
		ResponseEntity<ItemResponse> createResponse = restTemplate.postForEntity(
			"/api/inventory",
			createRequestEntity,
			ItemResponse.class
		);

		assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(createResponse.getBody()).isNotNull();
		assertThat(createResponse.getBody().sku()).isEqualTo(createRequest.sku());

		HttpEntity<Void> getRequestEntity = new HttpEntity<>(headers);
		ResponseEntity<ItemResponse> getResponse = restTemplate.exchange(
			"/api/inventory/" + createRequest.sku(),
			HttpMethod.GET,
			getRequestEntity,
			ItemResponse.class
		);

		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(getResponse.getBody()).isNotNull();
		assertThat(getResponse.getBody().sku()).isEqualTo(createRequest.sku());
		assertThat(getResponse.getBody().name()).isEqualTo(createRequest.name());
	}

	@Test
	void getItem_whenNotFound_shouldReturnNotFound() {
		String token = getAuthToken(List.of("inventory.read"));
		HttpHeaders headers = createAuthHeaders(token);
		HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

		ResponseEntity<String> response = restTemplate.exchange(
			"/api/inventory/DOES-NOT-EXIST",
			HttpMethod.GET,
			requestEntity,
			String.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getHeaders().getContentType().toString())
			.contains("application/problem+json");
	}

	@Test
	void getItem_withoutAuth_shouldReturnUnauthorized() {
		ResponseEntity<String> response = restTemplate.getForEntity(
			"/api/inventory/TEST-SKU",
			String.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

}
