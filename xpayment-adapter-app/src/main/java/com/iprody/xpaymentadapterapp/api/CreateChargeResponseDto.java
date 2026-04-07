package com.iprody.xpaymentadapterapp.api;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreateChargeResponseDto {

    private UUID id;
    private BigDecimal amount;
    private String currency;
    private UUID order;
    private String status;
}
