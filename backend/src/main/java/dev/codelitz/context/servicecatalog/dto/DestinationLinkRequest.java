package dev.codelitz.context.servicecatalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
import java.util.List;

public record DestinationLinkRequest(
    @NotBlank @Size(max = 500) @Pattern(regexp = "https?://.+", message = "must be an HTTP or HTTPS URL") String url,
    @Size(max = 80) String environment,
    @Size(max = 40) String authenticationMethod,
    @Size(max = 120) String accountIdentifier,
    @Size(max = 500) String accessNotes,
    @Size(max = 500) @Pattern(regexp = "^$|https?://.+", message = "must be an HTTP or HTTPS URL") String accessUrl,
    @Size(max = 20) List<@Valid DestinationAccountRequest> accounts
) {}
