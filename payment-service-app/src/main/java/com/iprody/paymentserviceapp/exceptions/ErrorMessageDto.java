package com.iprody.paymentserviceapp.exceptions;

import java.time.Instant;
import java.util.UUID;

public record ErrorMessageDto(
        String error,
        Instant timestamp,
        String operation,
        UUID entityId
) {
}
