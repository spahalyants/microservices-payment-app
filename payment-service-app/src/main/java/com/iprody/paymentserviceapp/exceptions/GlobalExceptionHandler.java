package com.iprody.paymentserviceapp.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorMessageDto handleEntityNotFound(EntityNotFoundException ex,
                                                HttpServletRequest request) {
        return new ErrorMessageDto(
                ex.getMessage(),
                Instant.now(),
                request.getMethod() + " " + request.getRequestURI(),
                ex.getEntityId()
        );
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorMessageDto handleAccessDenied(AuthorizationDeniedException ex,
                                              HttpServletRequest request) {
        return new ErrorMessageDto(
                ex.getMessage(),
                Instant.now(),
                request.getMethod() + " " + request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorMessageDto handleOther(Exception ex,
                                       HttpServletRequest request) {
        return new ErrorMessageDto(
                ex.getMessage(),
                Instant.now(),
                request.getMethod() + " " + request.getRequestURI(),
                null
        );
    }
}
