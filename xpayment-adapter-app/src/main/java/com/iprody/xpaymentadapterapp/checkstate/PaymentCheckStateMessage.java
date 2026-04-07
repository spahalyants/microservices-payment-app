package com.iprody.xpaymentadapterapp.checkstate;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class PaymentCheckStateMessage {

    private UUID chargeGuid;
    private UUID paymentGuid;
    private BigDecimal amount;
    private String currency;

    public PaymentCheckStateMessage(
        UUID chargeGuid,
        UUID paymentGuid,
        BigDecimal amount,
        String currency
    ) {
        this.chargeGuid = chargeGuid;
        this.paymentGuid = paymentGuid;
        this.amount = amount;
        this.currency = currency;
    }
}
