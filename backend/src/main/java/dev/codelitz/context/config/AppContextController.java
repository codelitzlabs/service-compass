package dev.codelitz.context.config;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.web.csrf.CsrfToken;

@RestController
class AppContextController {
    private final PortalProperties properties;

    AppContextController(PortalProperties properties) { this.properties = properties; }

    @GetMapping("/api/context")
    AppContextResponse context(CsrfToken csrfToken) {
        // Resolving the deferred token makes Spring send the XSRF-TOKEN cookie used by the SPA.
        if (csrfToken != null) csrfToken.getToken();
        return new AppContextResponse(properties.companyName());
    }
}

record AppContextResponse(String companyName) {}
