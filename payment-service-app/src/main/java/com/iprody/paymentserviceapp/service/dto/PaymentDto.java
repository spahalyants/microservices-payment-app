package com.iprody.paymentserviceapp.service.dto;

import com.iprody.paymentserviceapp.persistence.model.PaymentStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class PaymentDto {
    private UUID guid;
    private UUID inquiryRefId;
    private BigDecimal amount;
    private String currency;
    private UUID transactionRefId;
    private PaymentStatus status;
    private String note;
    private Instant createdAt;
    private Instant updatedAt;
}