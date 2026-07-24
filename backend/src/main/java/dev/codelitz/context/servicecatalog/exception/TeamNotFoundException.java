package dev.codelitz.context.servicecatalog.exception;

public class TeamNotFoundException extends RuntimeException {
    public TeamNotFoundException(String name) {
        super("Team '%s' was not found".formatted(name));
    }
}
