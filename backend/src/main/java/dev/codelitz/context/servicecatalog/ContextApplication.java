package dev.codelitz.context.servicecatalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(scanBasePackages = "dev.codelitz.context")
@ConfigurationPropertiesScan(basePackages = "dev.codelitz.context")
public class ContextApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContextApplication.class, args);
    }
}
