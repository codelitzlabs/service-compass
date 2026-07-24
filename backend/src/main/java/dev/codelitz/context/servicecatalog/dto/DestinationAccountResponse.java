package dev.codelitz.context.servicecatalog.dto;

import java.util.UUID;

public record DestinationAccountResponse(UUID id, String label, String identifier, String authenticationMethod) {}
