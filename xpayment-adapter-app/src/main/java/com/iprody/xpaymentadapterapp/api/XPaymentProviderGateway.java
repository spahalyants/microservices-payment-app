package com.iprody.xpaymentadapterapp.api;

import org.springframework.web.client.RestClientException;

import java.util.UUID;

public interface XPaymentProviderGateway {

    CreateChargeResponseDto createCharge(CreateChargeRequestDto createChargeRequestDto)
        throws RestClientException;

    CreateChargeResponseDto retrieveCharge(UUID id) throws RestClientException;
}
