package com.iprody.xpaymentadapterapp.api;

import com.iprody.xpayment.app.api.client.DefaultApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

@Service
class XPaymentProviderGatewayImpl implements XPaymentProviderGateway {

    private static final Logger log = LoggerFactory.getLogger(XPaymentProviderGatewayImpl.class);

    private final DefaultApi defaultApi;
    private final ChargeMapper chargeMapper;

    public XPaymentProviderGatewayImpl(DefaultApi defaultApi, ChargeMapper chargeMapper) {
        this.defaultApi = defaultApi;
        this.chargeMapper = chargeMapper;
    }

    @Override
    public CreateChargeResponseDto createCharge(CreateChargeRequestDto createChargeRequestDto)
        throws RestClientException {
        try {
            return chargeMapper.toCreateChargeResponseDto(
                    defaultApi.createCharge(
                            chargeMapper.toCreateChargeRequest(createChargeRequestDto)
                    )
            );
        } catch (Exception e) {
            throw toRestClientException("POST /charges failed", e);
        }
    }

    @Override
    public CreateChargeResponseDto retrieveCharge(UUID id) throws RestClientException {
        try {
            return chargeMapper.toCreateChargeResponseDto(defaultApi.retrieveCharge(id));
        } catch (Exception e) {
            throw toRestClientException("GET /charges/{id} failed (id=" + id + ")", e);
        }
    }

    private RestClientException toRestClientException(String prefix, Exception e) {
        final String msg = String.format("%s: body: %s", prefix, e.getMessage());
        log.error(msg, e);
        return new RestClientException(msg, e);
    }
}
