package dev.codelitz.context.servicecatalog.exception;

import java.util.UUID;

public class ServiceNotFoundException extends RuntimeException {
    public ServiceNotFoundException(UUID id) {
        super("Service %s was not found".formatted(id));
    }
}
