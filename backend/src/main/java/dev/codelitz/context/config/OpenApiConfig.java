package dev.codelitz.context.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI contextOpenApi() {
        return new OpenAPI().info(new Info().title("Service Compass API").version("v1").description("Engineering service context in one place."));
    }
}
