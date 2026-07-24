package dev.codelitz.context.servicecatalog.dto;

import java.util.List;
import java.util.UUID;

public record DestinationResponse(UUID id, String name, String label, List<DestinationLinkResponse> links) {}
