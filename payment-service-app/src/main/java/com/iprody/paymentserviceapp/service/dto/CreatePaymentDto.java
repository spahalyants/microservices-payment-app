package com.iprody.paymentserviceapp.service.dto;

import com.iprody.paymentserviceapp.persistence.model.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePaymentDto(
        UUID inquiryRefId,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        String note
) {
}
