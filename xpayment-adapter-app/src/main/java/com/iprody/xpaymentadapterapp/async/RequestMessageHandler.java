package com.iprody.xpaymentadapterapp.async;

import com.iprody.xpaymentadapterapp.api.CreateChargeRequestDto;
import com.iprody.xpaymentadapterapp.api.CreateChargeResponseDto;
import com.iprody.xpaymentadapterapp.api.XPaymentProviderGateway;
import com.iprody.xpaymentadapterapp.checkstate.PaymentStateCheckRegistrar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.util.UUID;

@Component
public class RequestMessageHandler implements MessageHandler<XPaymentAdapterRequestMessage> {

    private static final Logger log = LoggerFactory.getLogger(RequestMessageHandler.class);

    private final XPaymentProviderGateway xPaymentProviderGateway;
    private final AsyncSender<XPaymentAdapterResponseMessage> asyncSender;
    private final PaymentStateCheckRegistrar paymentStateCheckRegistrar;

    public RequestMessageHandler(
        XPaymentProviderGateway xPaymentProviderGateway,
        AsyncSender<XPaymentAdapterResponseMessage> asyncSender,
        PaymentStateCheckRegistrar paymentStateCheckRegistrar
    ) {
        this.xPaymentProviderGateway = xPaymentProviderGateway;
        this.asyncSender = asyncSender;
        this.paymentStateCheckRegistrar = paymentStateCheckRegistrar;
    }

    @Override
    public void handle(XPaymentAdapterRequestMessage message) {
        log.info("Payment request received: paymentGuid={}, amount={}, currency={}",
            message.getPaymentGuid(), message.getAmount(), message.getCurrency());

        final CreateChargeRequestDto requestDto = new CreateChargeRequestDto();
        requestDto.setAmount(message.getAmount());
        requestDto.setCurrency(message.getCurrency());
        requestDto.setOrder(message.getPaymentGuid());

        try {
            final CreateChargeResponseDto responseDto =
                xPaymentProviderGateway.createCharge(requestDto);
            log.info("Payment forwarded to X Payment Provider: paymentGuid={}, status={}",
                message.getPaymentGuid(), responseDto.getStatus());

            final XPaymentAdapterResponseMessage responseMessage =
                new XPaymentAdapterResponseMessage();
            responseMessage.setMessageId(UUID.randomUUID());
            responseMessage.setPaymentGuid(responseDto.getOrder());
            responseMessage.setTransactionRefId(responseDto.getId());
            responseMessage.setAmount(responseDto.getAmount());
            responseMessage.setCurrency(responseDto.getCurrency());
            responseMessage.setStatus(XPaymentAdapterStatus
                .valueOf(responseDto.getStatus().toUpperCase()));
            responseMessage.setOccurredAt(Instant.now());
            asyncSender.send(responseMessage);

            paymentStateCheckRegistrar.register(
                responseDto.getId(),
                responseDto.getOrder(),
                responseDto.getAmount(),
                responseDto.getCurrency()
            );

        } catch (RestClientException ex) {
            log.error("Failed to forward payment to X Payment Provider: paymentGuid={}",
                message.getPaymentGuid(), ex);

            final XPaymentAdapterResponseMessage responseMessage =
                new XPaymentAdapterResponseMessage();
            responseMessage.setMessageId(UUID.randomUUID());
            responseMessage.setPaymentGuid(message.getPaymentGuid());
            responseMessage.setAmount(message.getAmount());
            responseMessage.setCurrency(message.getCurrency());
            responseMessage.setStatus(XPaymentAdapterStatus.CANCELED);
            responseMessage.setOccurredAt(Instant.now());
            asyncSender.send(responseMessage);
        }
    }
}
