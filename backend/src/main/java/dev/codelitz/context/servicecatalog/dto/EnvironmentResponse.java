package dev.codelitz.context.servicecatalog.dto;

import java.util.UUID;

public record EnvironmentResponse(UUID id, String name, String color) {}
