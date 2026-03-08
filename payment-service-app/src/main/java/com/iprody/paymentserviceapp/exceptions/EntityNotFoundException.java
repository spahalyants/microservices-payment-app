package com.iprody.paymentserviceapp.exceptions;

import java.util.UUID;


public class EntityNotFoundException extends RuntimeException {

    private final UUID entityId;

    public EntityNotFoundException(String message, UUID entityId) {
        super(message);
        this.entityId = entityId;
    }

    public UUID getEntityId() {
        return entityId;
    }
}
