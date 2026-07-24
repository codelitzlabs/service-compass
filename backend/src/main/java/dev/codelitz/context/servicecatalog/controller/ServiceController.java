package dev.codelitz.context.servicecatalog.controller;

import dev.codelitz.context.servicecatalog.dto.EnvironmentResponse;
import dev.codelitz.context.servicecatalog.dto.ServiceRequest;
import dev.codelitz.context.servicecatalog.dto.ServiceResponse;
import dev.codelitz.context.servicecatalog.service.ServiceCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/services")
class ServiceController {
    private final ServiceCatalogService service;
    ServiceController(ServiceCatalogService service) { this.service = service; }

    @GetMapping @Operation(summary = "Search and list services")
    Page<ServiceResponse> findAll(@RequestParam(required = false) String query, @ParameterObject Pageable pageable) {
        return service.findAll(query, pageable);
    }
    @GetMapping("/{id}") ServiceResponse findById(@PathVariable UUID id) { return service.findById(id); }
    @PostMapping
    @Operation(
        summary = "Create a service",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
            schema = @Schema(implementation = ServiceRequest.class),
            examples = @ExampleObject(name = "Service with platform destinations", value = """
                {
                  "name": "inventory-api",
                  "description": "Tracks product availability for order processing",
                  "owners": ["Platform on-call", "Developer experience"],
                  "teams": ["Platform"],
                  "lifecycle": "production",
                  "repositoryUrl": "https://github.com/example/inventory-api",
                  "tags": ["java", "spring-boot", "inventory"],
                  "destinations": [
                    {
                      "name": "Grafana",
                      "label": "grafana",
                      "links": [
                        {"url": "https://grafana.example.com/d/inventory-api", "environment": "Production"},
                        {"url": "https://grafana.example.com/d/inventory-api-dev", "environment": "Development"}
                      ]
                    },
                    {
                      "name": "Argo CD application",
                      "label": "argocd",
                      "links": [{"url": "https://deploy.example.com/applications/inventory-api", "environment": "Production"}]
                    },
                    {
                      "name": "Confluence runbook",
                      "label": "confluence",
                      "links": [{"url": "https://docs.example.com/inventory-api/runbook"}]
                    },
                    {
                      "name": "Sentry project",
                      "label": "sentry",
                      "links": [{"url": "https://errors.example.com/projects/inventory-api", "environment": "Production"}]
                    },
                    {
                      "name": "Swagger UI",
                      "label": "swagger",
                      "links": [{"url": "https://inventory.example.com/api/docs", "environment": "Production"}]
                    }
                  ]
                }
                """)
        ))
    )
    ResponseEntity<ServiceResponse> create(@Valid @RequestBody ServiceRequest request) {
        var created = service.create(request);
        return ResponseEntity.created(URI.create("/api/services/" + created.id())).body(created);
    }
    @PutMapping("/{id}") ServiceResponse update(@PathVariable UUID id, @Valid @RequestBody ServiceRequest request) { return service.update(id, request); }
    @DeleteMapping("/{id}") ResponseEntity<Void> delete(@PathVariable UUID id) { service.delete(id); return ResponseEntity.noContent().build(); }

    @GetMapping("/environments") @Operation(summary = "List available environments")
    List<EnvironmentResponse> environments() { return service.findEnvironments(); }
}
