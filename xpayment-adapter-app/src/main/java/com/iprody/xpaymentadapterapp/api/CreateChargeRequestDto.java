package com.iprody.xpaymentadapterapp.api;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreateChargeRequestDto {

    private BigDecimal amount;
    private String currency;
    private UUID order;
}
