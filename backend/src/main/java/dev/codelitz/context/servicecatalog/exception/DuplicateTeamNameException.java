package dev.codelitz.context.servicecatalog.exception;

public class DuplicateTeamNameException extends RuntimeException {
    public DuplicateTeamNameException(String name) {
        super("A team named '%s' already exists".formatted(name));
    }
}
