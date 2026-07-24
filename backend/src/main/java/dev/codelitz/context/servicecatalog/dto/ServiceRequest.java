package dev.codelitz.context.servicecatalog.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Set;

public record ServiceRequest(
    @NotBlank @Size(max = 120) String name,
    @NotBlank @Size(max = 500) String description,
    @NotEmpty @Size(max = 10) Set<@NotBlank @Size(max = 120) String> owners,
    @NotEmpty @Size(max = 10) Set<@NotBlank @Size(max = 120) String> teams,
    @NotBlank @Pattern(regexp = "experimental|production|deprecated") String lifecycle,
    @NotBlank @Size(max = 500) @Pattern(regexp = "https?://.+", message = "must be an HTTP or HTTPS URL") String repositoryUrl,
    @Size(max = 20) Set<@NotBlank @Size(max = 40) String> tags,
    @Size(max = 50) List<@Valid DestinationRequest> destinations
) {}
