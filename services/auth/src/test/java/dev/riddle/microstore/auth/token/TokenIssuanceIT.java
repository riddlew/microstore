package dev.riddle.microstore.auth.token;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
class TokenIssuanceIT {

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
    }

    private final MockMvc mockMvc;

    TokenIssuanceIT(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void shouldIssueTokenForClientCredentials() throws Exception {
        mockMvc.perform(post("/oauth2/token")
                        .param("grant_type", "client_credentials")
                        .param("scope", "inventory.read")
                        .with(httpBasic("orders-service", "secret")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").exists())
                .andExpect(jsonPath("$.token_type").value("Bearer"))
                .andExpect(jsonPath("$.expires_in").exists());
    }

    @Test
    void shouldIncludeScopeInAccessToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/oauth2/token")
                        .param("grant_type", "client_credentials")
                        .param("scope", "inventory.read inventory.write")
                        .with(httpBasic("orders-service", "secret")))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        String accessToken = JsonPath.parse(response).read("$.access_token");

        // Decode JWT payload (second part of JWT)
        String[] parts = accessToken.split("\\.");
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]));

        // Verify token claims
        assertThat(payload).contains("\"scope\":\"inventory.read inventory.write\"");
        assertThat(payload).contains("\"iss\":\"http://localhost:9000\"");
    }

    @Test
    void shouldRejectInvalidClientCredentials() throws Exception {
        mockMvc.perform(post("/oauth2/token")
                        .param("grant_type", "client_credentials")
                        .param("scope", "inventory.read")
                        .with(httpBasic("orders-service", "wrong-secret")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectUnknownClient() throws Exception {
        mockMvc.perform(post("/oauth2/token")
                        .param("grant_type", "client_credentials")
                        .param("scope", "inventory.read")
                        .with(httpBasic("unknown-client", "secret")))
                .andExpect(status().isUnauthorized());
    }
}

