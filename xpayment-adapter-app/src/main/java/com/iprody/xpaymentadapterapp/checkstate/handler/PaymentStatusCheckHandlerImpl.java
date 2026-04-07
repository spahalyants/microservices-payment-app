package com.iprody.xpaymentadapterapp.checkstate.handler;

import com.iprody.xpaymentadapterapp.api.CreateChargeResponseDto;
import com.iprody.xpaymentadapterapp.api.XPaymentProviderGateway;
import com.iprody.xpaymentadapterapp.async.AsyncSender;
import com.iprody.xpaymentadapterapp.async.XPaymentAdapterResponseMessage;
import com.iprody.xpaymentadapterapp.async.XPaymentAdapterStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
public class PaymentStatusCheckHandlerImpl implements PaymentStatusCheckHandler {

    private static final Logger log = LoggerFactory.getLogger(PaymentStatusCheckHandlerImpl.class);

    private static final Set<String> TERMINAL_STATUSES = Set.of("SUCCEEDED", "CANCELED");

    private final XPaymentProviderGateway xPaymentProviderGateway;
    private final AsyncSender<XPaymentAdapterResponseMessage> asyncSender;

    public PaymentStatusCheckHandlerImpl(
        XPaymentProviderGateway xPaymentProviderGateway,
        AsyncSender<XPaymentAdapterResponseMessage> asyncSender
    ) {
        this.xPaymentProviderGateway = xPaymentProviderGateway;
        this.asyncSender = asyncSender;
    }

    @Override
    public boolean handle(UUID chargeGuid) {
        try {
            final CreateChargeResponseDto response =
                xPaymentProviderGateway.retrieveCharge(chargeGuid);
            final String status = response.getStatus().toUpperCase();
            log.info("Status check for chargeGuid={}: status={}", chargeGuid, status);

            if (TERMINAL_STATUSES.contains(status)) {
                final XPaymentAdapterResponseMessage responseMessage =
                    new XPaymentAdapterResponseMessage();
                responseMessage.setMessageId(UUID.randomUUID());
                responseMessage.setPaymentGuid(response.getOrder());
                responseMessage.setTransactionRefId(response.getId());
                responseMessage.setAmount(response.getAmount());
                responseMessage.setCurrency(response.getCurrency());
                responseMessage.setStatus(XPaymentAdapterStatus.valueOf(status));
                responseMessage.setOccurredAt(Instant.now());
                asyncSender.send(responseMessage);
                log.info("Terminal status reached for chargeGuid={}: notifying Payment Service",
                    chargeGuid);
                return true;
            }

            return false;

        } catch (RestClientException ex) {
            log.error("Error checking status for chargeGuid={}", chargeGuid, ex);
            return false;
        }
    }
}
