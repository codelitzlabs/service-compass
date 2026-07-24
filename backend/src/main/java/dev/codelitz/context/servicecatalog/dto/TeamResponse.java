package dev.codelitz.context.servicecatalog.dto;

import java.util.Set;
import java.util.UUID;

public record TeamResponse(UUID id, String name, String description, Set<String> owners) {}
