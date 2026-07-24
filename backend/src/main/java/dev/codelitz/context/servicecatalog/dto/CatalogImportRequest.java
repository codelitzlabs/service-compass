package dev.codelitz.context.servicecatalog.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CatalogImportRequest(
    @NotNull @Size(max = 100) List<@Valid TeamRequest> teams,
    @NotNull @Size(max = 500) List<@Valid ServiceRequest> services
) {}
