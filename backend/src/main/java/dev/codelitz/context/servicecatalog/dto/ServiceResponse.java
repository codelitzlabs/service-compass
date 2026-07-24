package dev.codelitz.context.servicecatalog.dto;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record ServiceResponse(
    UUID id,
    String name,
    String description,
    Set<String> owners,
    Set<TeamSummary> teams,
    String lifecycle,
    String repositoryUrl,
    Set<String> tags,
    List<DestinationResponse> destinations,
    Instant createdAt,
    Instant updatedAt
) {}
