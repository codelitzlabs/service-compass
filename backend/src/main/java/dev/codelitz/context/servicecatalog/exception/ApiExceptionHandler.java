package dev.codelitz.context.servicecatalog.exception;

import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(ServiceNotFoundException.class)
    ProblemDetail notFound(ServiceNotFoundException exception) { return problem(HttpStatus.NOT_FOUND, exception.getMessage()); }
    @ExceptionHandler(DuplicateServiceNameException.class)
    ProblemDetail conflict(DuplicateServiceNameException exception) { return problem(HttpStatus.CONFLICT, exception.getMessage()); }
    @ExceptionHandler(DuplicateTeamNameException.class)
    ProblemDetail teamConflict(DuplicateTeamNameException exception) { return problem(HttpStatus.CONFLICT, exception.getMessage()); }
    @ExceptionHandler(TeamHasServicesException.class)
    ProblemDetail teamHasServices(TeamHasServicesException exception) { return problem(HttpStatus.CONFLICT, exception.getMessage()); }
    @ExceptionHandler(TeamNotFoundForDeletionException.class)
    ProblemDetail teamNotFound(TeamNotFoundForDeletionException exception) { return problem(HttpStatus.NOT_FOUND, exception.getMessage()); }
    @ExceptionHandler(TeamNotFoundException.class)
    ProblemDetail invalidTeam(TeamNotFoundException exception) { return problem(HttpStatus.BAD_REQUEST, exception.getMessage()); }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail invalid(MethodArgumentNotValidException exception) {
        var detail = problem(HttpStatus.BAD_REQUEST, "The request contains invalid fields");
        detail.setProperty("errors", exception.getBindingResult().getFieldErrors().stream().collect(
            java.util.stream.Collectors.toMap(e -> e.getField(), e -> e.getDefaultMessage(), (a, b) -> a)));
        return detail;
    }
    @ExceptionHandler(ConstraintViolationException.class)
    ProblemDetail invalidUpload(ConstraintViolationException exception) {
        var detail = problem(HttpStatus.BAD_REQUEST, "The uploaded catalog contains invalid fields");
        detail.setProperty("errors", exception.getConstraintViolations().stream().collect(
            java.util.stream.Collectors.toMap(
                violation -> violation.getPropertyPath().toString(),
                violation -> violation.getMessage(),
                (a, b) -> a)));
        return detail;
    }
    private ProblemDetail problem(HttpStatus status, String message) {
        var detail = ProblemDetail.forStatusAndDetail(status, message); detail.setType(URI.create("about:blank")); return detail;
    }
}
