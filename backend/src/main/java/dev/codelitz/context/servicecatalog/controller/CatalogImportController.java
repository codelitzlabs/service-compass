package dev.codelitz.context.servicecatalog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import dev.codelitz.context.servicecatalog.dto.CatalogImportRequest;
import dev.codelitz.context.servicecatalog.dto.CatalogImportResponse;
import dev.codelitz.context.servicecatalog.dto.DestinationLinkRequest;
import dev.codelitz.context.servicecatalog.dto.DestinationAccountRequest;
import dev.codelitz.context.servicecatalog.dto.DestinationRequest;
import dev.codelitz.context.servicecatalog.dto.ServiceRequest;
import dev.codelitz.context.servicecatalog.dto.TeamRequest;
import dev.codelitz.context.servicecatalog.service.CatalogImportService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.io.IOException;
import java.time.LocalDate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/import")
class CatalogImportController {
    private final CatalogImportService service;
    private final Validator validator;
    private final ObjectMapper mapper = JsonMapper.builder().findAndAddModules().build();

    CatalogImportController(CatalogImportService service, Validator validator) {
        this.service = service;
        this.validator = validator;
    }

    @GetMapping("/template")
    @Operation(summary = "Download a catalog import template")
    CatalogImportRequest template() {
        return new CatalogImportRequest(
            java.util.List.of(new TeamRequest("Platform", "Shared infrastructure and developer experience", java.util.Set.of("Platform on-call", "Ada Lovelace"))),
            java.util.List.of(new ServiceRequest(
                "orders-api",
                "Handles order intake and exposes order status APIs",
                java.util.Set.of("Orders on-call", "orders@example.com"),
                java.util.Set.of("Platform"),
                "production",
                "https://github.com/example/orders-api",
                java.util.Set.of("java", "critical"),
                java.util.List.of(
                    new DestinationRequest("Grafana", "grafana", java.util.List.of(
                        new DestinationLinkRequest("https://grafana.example.com/d/orders-api", "Production", "SSO", null, "Sign in with the company identity provider. VPN is required outside the office.", "https://access.example.com/grafana", java.util.List.of(
                            new DestinationAccountRequest("Administrator", "admin@example.com", "SSO"),
                            new DestinationAccountRequest("Marketing", "marketing@example.com", "Sign in"))),
                        new DestinationLinkRequest("https://grafana.example.com/d/orders-api-staging", "Staging", "SSO", null, null, null, java.util.List.of()))),
                    new DestinationRequest("Argo CD", "argocd", java.util.List.of(
                        new DestinationLinkRequest("https://argocd.example.com/applications/orders-api", "Production", "SSO", null, null, null, java.util.List.of(new DestinationAccountRequest("User", "Company email", "SSO"))),
                        new DestinationLinkRequest("https://argocd.example.com/applications/orders-api-staging", "Staging", "SSO", null, null, null, java.util.List.of(new DestinationAccountRequest("User", "Company email", "SSO"))))),
                    new DestinationRequest("Runbook", "confluence", java.util.List.of(
                        new DestinationLinkRequest("https://confluence.example.com/display/ORDERS/Runbook", null, "SSO", null, null, null, java.util.List.of(new DestinationAccountRequest("User", "Company email", "SSO")))))
                )
            ))
        );
    }

    @GetMapping("/export")
    @Operation(summary = "Download the current catalog as reusable JSON")
    ResponseEntity<CatalogImportRequest> export() {
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"catalog-export-" + LocalDate.now() + ".json\"")
            .body(service.exportCatalog());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a filled catalog import template")
    CatalogImportResponse upload(@RequestPart("file") MultipartFile file) throws IOException {
        var request = mapper.readValue(file.getInputStream(), CatalogImportRequest.class);
        var violations = validator.validate(request);
        if (!violations.isEmpty()) throw new ConstraintViolationException(violations);
        return service.importCatalog(request);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Import a filled catalog template JSON body")
    CatalogImportResponse uploadJson(@Valid @RequestBody CatalogImportRequest request) {
        return service.importCatalog(request);
    }
}
