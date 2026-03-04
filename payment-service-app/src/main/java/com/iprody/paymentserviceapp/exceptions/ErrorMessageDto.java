package com.iprody.paymentserviceapp.exceptions;

import java.time.Instant;

public record ErrorMessageDto(
        String message,
        Instant timestamp
) {
}
