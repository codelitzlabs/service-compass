package dev.codelitz.context.servicecatalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record TeamRequest(
    @NotBlank @Size(max = 120) String name,
    @Size(max = 300) String description,
    @NotEmpty @Size(max = 30) Set<@NotBlank @Size(max = 120) String> owners
) {}
