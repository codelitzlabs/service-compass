package dev.codelitz.context.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "portal")
public record PortalProperties(String companyName) {
    public PortalProperties {
        if (companyName == null || companyName.isBlank()) companyName = "Codelitz Labs";
    }
}
