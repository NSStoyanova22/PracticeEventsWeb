package com.example.hefest.controller;

import com.example.hefest.service.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestControllerAdvice(basePackageClasses = EventRestController.class)
public class ApiExceptionHandler {
    @ExceptionHandler(NoSuchElementException.class)
    public ProblemDetail notFound() { return problem(HttpStatus.NOT_FOUND, "Ресурсът не съществува."); }

    @ExceptionHandler(EventService.ConcurrentEditException.class)
    public ProblemDetail conflict(EventService.ConcurrentEditException exception) { return problem(HttpStatus.CONFLICT, exception.getMessage()); }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validation(MethodArgumentNotValidException exception) {
        ProblemDetail detail = problem(HttpStatus.BAD_REQUEST, "Невалидни данни.");
        detail.setProperty("errors", exception.getBindingResult().getFieldErrors().stream().collect(Collectors.toMap(
                error -> error.getField(), error -> error.getDefaultMessage(), (first, ignored) -> first)));
        return detail;
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ProblemDetail badRequest(RuntimeException exception) { return problem(HttpStatus.BAD_REQUEST, exception.getMessage()); }

    private ProblemDetail problem(HttpStatus status, String message) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(status, message);
        detail.setTitle(status.getReasonPhrase());
        return detail;
    }
}
