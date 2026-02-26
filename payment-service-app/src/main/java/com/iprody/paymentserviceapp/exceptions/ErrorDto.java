package com.iprody.paymentserviceapp.exceptions;

import java.time.Instant;
import java.util.UUID;

public class ErrorDto {

    private final UUID id;
    private final String operation;
    private final String errorMessage;
    private final Instant timestamp;

    public ErrorDto(UUID id, Operation operation, String errorMessage) {
        this.id = id;
        this.operation = operation != null ? operation.getValue() : null;
        this.errorMessage = errorMessage;
        this.timestamp = Instant.now();
    }

    public ErrorDto(Operation operation, String errorMessage) {
        this(null, operation, errorMessage);
    }

    public UUID getId() {
        return id;
    }

    public String getOperation() {
        return operation;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}