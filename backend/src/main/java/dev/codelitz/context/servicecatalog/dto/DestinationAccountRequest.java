package dev.codelitz.context.servicecatalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DestinationAccountRequest(
    @NotBlank @Size(max = 80) String label,
    @NotBlank @Size(max = 120) String identifier,
    @Size(max = 40) String authenticationMethod
) {}
