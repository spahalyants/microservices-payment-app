package com.iprody.paymentserviceapp.exceptions;

import java.util.UUID;


public class EntityNotFoundException extends RuntimeException {

    private final Operation operation;
    private final UUID entityId;

    public EntityNotFoundException(String message, Operation operation, UUID entityId) {
        super(message);
        this.operation = operation;
        this.entityId = entityId;
    }

    public Operation getOperation() {
        return operation;
    }

    public UUID getEntityId() {
        return entityId;
    }
}