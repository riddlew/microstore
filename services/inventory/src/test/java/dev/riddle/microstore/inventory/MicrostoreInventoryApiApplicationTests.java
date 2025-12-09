package dev.riddle.microstore.inventory;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import dev.riddle.microstore.inventory.testutil.JwtTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@SpringBootTest(properties = {
	"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration"
})
@Testcontainers
class MicrostoreInventoryApiApplicationTests {

	@RegisterExtension
	static WireMockExtension wireMock = WireMockExtension.newInstance()
		.options(wireMockConfig().port(8888))
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
		// Also wire JWT settings to the WireMock issuer (for safety if security loads)
		registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", JwtTestUtil::getTestIssuer);
		registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
			() -> JwtTestUtil.getTestIssuer() + "/.well-known/jwks.json");
	}

	@TestConfiguration
	static class JwtTestConfig {
		@Bean
		JwtDecoder jwtDecoder() {
			return NimbusJwtDecoder.withJwkSetUri(JwtTestUtil.getTestIssuer() + "/.well-known/jwks.json").build();
		}
	}

	@BeforeEach
	void setUp() {
		// Mock JWKS endpoint in case OAuth2 config is still loaded
		wireMock.stubFor(get(urlEqualTo("/.well-known/jwks.json"))
			.willReturn(aResponse()
				.withHeader("Content-Type", "application/json")
				.withBody(JwtTestUtil.getJwksJson())));
	}

	@Test
	void contextLoads() {
	}

}
