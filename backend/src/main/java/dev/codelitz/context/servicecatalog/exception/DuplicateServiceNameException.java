package dev.codelitz.context.servicecatalog.exception;

public class DuplicateServiceNameException extends RuntimeException {
    public DuplicateServiceNameException(String name) {
        super("A service named '%s' already exists".formatted(name));
    }
}
