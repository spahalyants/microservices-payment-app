package com.iprody.paymentserviceapp.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;


import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorMessageDto handleEntityNotFound(EntityNotFoundException ex) {
        return new ErrorMessageDto(ex.getMessage(), Instant.now());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorMessageDto handleOther(Exception ex) {
        return new ErrorMessageDto(ex.getMessage(), Instant.now());
    }
}


