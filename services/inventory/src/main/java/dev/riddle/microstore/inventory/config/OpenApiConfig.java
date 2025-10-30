package dev.riddle.microstore.inventory.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	GroupedOpenApi inventoryApi() {
		return GroupedOpenApi
			.builder()
			.group("inventory")
			.pathsToMatch("/api/**")
			.build();
	}

	@Bean
	OpenAPI apiInfo() {
		return new OpenAPI()
			.info(new Info()
				.title("Inventory Api")
				.version("v1"));
	}
}
