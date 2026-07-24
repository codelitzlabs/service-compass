package dev.codelitz.context.servicecatalog.exception;

import java.util.UUID;

public class TeamNotFoundForDeletionException extends RuntimeException {
    public TeamNotFoundForDeletionException(UUID id) {
        super("Team %s was not found".formatted(id));
    }
}
