package dev.codelitz.context.servicecatalog.dto;

import java.util.UUID;
import java.util.List;

public record DestinationLinkResponse(
    UUID id,
    String url,
    EnvironmentResponse environment,
    String authenticationMethod,
    String accountIdentifier,
    String accessNotes,
    String accessUrl,
    List<DestinationAccountResponse> accounts
) {}
