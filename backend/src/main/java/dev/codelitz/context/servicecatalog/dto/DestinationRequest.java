package dev.codelitz.context.servicecatalog.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record DestinationRequest(
    @NotBlank @Size(max = 120) String name,
    @NotBlank @Size(max = 30) String label,
    @NotEmpty @Size(max = 20) List<@Valid DestinationLinkRequest> links
) {}
