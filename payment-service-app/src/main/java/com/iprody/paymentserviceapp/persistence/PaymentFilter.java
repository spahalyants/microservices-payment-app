package com.iprody.paymentserviceapp.persistence;

import com.iprody.paymentserviceapp.persistence.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentFilter(
        String currency,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        Instant createdAfter,
        Instant createdBefore,
        PaymentStatus status
) {}

// This is the object that comes in from the HTTP request.
// PaymentFilter (Record) — the DTO for search criteria.