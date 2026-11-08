package com.example.hefest.controller;

import com.example.hefest.service.EventService;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.web.servlet.ModelAndView;

import java.util.NoSuchElementException;

@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class GlobalExceptionHandler {
    @ExceptionHandler(NoSuchElementException.class)
    public ModelAndView notFound(NoSuchElementException exception) { return page(HttpStatus.NOT_FOUND, "Страницата или записът не съществува."); }

    @ExceptionHandler(EventService.ConcurrentEditException.class)
    public ModelAndView conflict(EventService.ConcurrentEditException exception) { return page(HttpStatus.CONFLICT, exception.getMessage()); }

    @ExceptionHandler(IllegalArgumentException.class)
    public ModelAndView badRequest(IllegalArgumentException exception) { return page(HttpStatus.BAD_REQUEST, exception.getMessage()); }

    @ExceptionHandler({MethodArgumentNotValidException.class, TypeMismatchException.class})
    public ModelAndView invalidRequest(Exception exception) { return page(HttpStatus.BAD_REQUEST, "Невалидни данни в заявката."); }

    @ExceptionHandler(Exception.class)
    public ModelAndView serverError(Exception exception) { return page(HttpStatus.INTERNAL_SERVER_ERROR, "Възникна неочаквана грешка."); }

    private ModelAndView page(HttpStatus status, String message) {
        ModelAndView view = new ModelAndView("error");
        view.setStatus(status);
        view.addObject("status", status.value());
        view.addObject("message", message);
        return view;
    }
}
