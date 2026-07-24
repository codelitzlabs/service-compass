package dev.codelitz.context.servicecatalog.exception;

public class TeamHasServicesException extends RuntimeException {
    public TeamHasServicesException(String name) {
        super("Team '%s' cannot be deleted while it has services".formatted(name));
    }
}
