package com.iprody.xpaymentadapterapp.async;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class RequestMessageHandler implements MessageHandler<XPaymentAdapterRequestMessage> {

    private static final Logger log = LoggerFactory.getLogger(RequestMessageHandler.class);

    private static final long PROCESSING_DELAY_SECONDS = 30L;

    private final AsyncSender<XPaymentAdapterResponseMessage> sender;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public RequestMessageHandler(AsyncSender<XPaymentAdapterResponseMessage> sender) {
        this.sender = sender;
    }

    @Override
    public void handle(XPaymentAdapterRequestMessage message) {
        log.info("Scheduling payment processing: paymentGuid={}, amount={}, currency={}",
            message.getPaymentGuid(), message.getAmount(), message.getCurrency());
        scheduler.schedule(
            () -> processAndRespond(message),
            PROCESSING_DELAY_SECONDS,
            TimeUnit.SECONDS
        );
    }

    private void processAndRespond(XPaymentAdapterRequestMessage request) {
        final XPaymentAdapterStatus status = resolveStatus(request.getAmount());
        log.info("Processing complete: paymentGuid={}, resolvedStatus={}",
            request.getPaymentGuid(), status);
        final XPaymentAdapterResponseMessage response = new XPaymentAdapterResponseMessage();
        response.setMessageId(UUID.randomUUID());
        response.setPaymentGuid(request.getPaymentGuid());
        response.setAmount(request.getAmount());
        response.setCurrency(request.getCurrency());
        response.setStatus(status);
        response.setTransactionRefId(UUID.randomUUID());
        response.setOccurredAt(Instant.now());
        sender.send(response);
    }

    private XPaymentAdapterStatus resolveStatus(BigDecimal amount) {
        final boolean divisibleByTwo =
            amount.remainder(BigDecimal.valueOf(2)).compareTo(BigDecimal.ZERO) == 0;
        return divisibleByTwo ? XPaymentAdapterStatus.SUCCEEDED : XPaymentAdapterStatus.CANCELED;
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down RequestMessageHandler scheduler");
        scheduler.shutdown();
    }
}
