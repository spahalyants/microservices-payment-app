package com.iprody.paymentserviceapp.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorDto handleEntityNotFound(EntityNotFoundException ex) {
        return new ErrorDto(
                ex.getEntityId(),
                ex.getOperation(),
                ex.getMessage()
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorDto handleOther(Exception ex) {
        return new ErrorDto(
                null,
                ex.getMessage()
        );
    }
}
