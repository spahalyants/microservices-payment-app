package com.iprody.paymentserviceapp.async;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * In-memory implementation of {@link AsyncSender} that simulates asynchronous
 * interaction with the X Payment Adapter.
 * <p>
 * When a request message is received, this broker immediately returns to the
 * caller and schedules a background task to run after 30 seconds. The task
 * produces a single {@link XPaymentAdapterResponseMessage}:
 * <ul>
 *   <li>If the payment amount is divisible by 2 → status {@code SUCCEEDED}</li>
 *   <li>Otherwise → status {@code CANCELED}</li>
 * </ul>
 * The result is delivered by calling {@link AsyncListener#onMessage(Message)}
 * on the configured listener.
 * <p>
 * This class will be replaced by a real broker adapter in a future lesson.
 * All consuming code depends only on {@link AsyncSender} and will require
 * no changes when that swap happens.
 */

@Service
public class InMemoryXPaymentAdapterMessageBroker
        implements AsyncSender<XPaymentAdapterRequestMessage> {

    private static final Logger log =
            LoggerFactory.getLogger(InMemoryXPaymentAdapterMessageBroker.class);

    private static final long PROCESSING_DELAY_SECONDS = 30L;

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(2);

    private final AsyncListener<XPaymentAdapterResponseMessage> resultListener;

    public InMemoryXPaymentAdapterMessageBroker(
            AsyncListener<XPaymentAdapterResponseMessage> resultListener) {
        this.resultListener = resultListener;
    }

    @Override
    public void send(XPaymentAdapterRequestMessage request) {
        log.info("Broker received request: messageId={}, paymentGuid={}, amount={} {}",
                request.getMessageId(), request.getPaymentGuid(),
                request.getAmount(), request.getCurrency());

        scheduler.schedule(
                () -> process(request),
                PROCESSING_DELAY_SECONDS,
                TimeUnit.SECONDS
        );

        log.debug("Processing scheduled in {} seconds for paymentGuid={}",
                PROCESSING_DELAY_SECONDS, request.getPaymentGuid());
    }

    private void process(XPaymentAdapterRequestMessage request) {
        UUID transactionRefId = UUID.randomUUID();
        boolean succeeded = request.getAmount()
                .remainder(BigDecimal.TWO)
                .compareTo(BigDecimal.ZERO) == 0;

        XPaymentAdapterStatus status =
                succeeded ? XPaymentAdapterStatus.SUCCEEDED : XPaymentAdapterStatus.CANCELED;

        log.info("Processing complete: paymentGuid={}, transactionRefId={}, status={}",
                request.getPaymentGuid(), transactionRefId, status);

        XPaymentAdapterResponseMessage response = new XPaymentAdapterResponseMessage();
        response.setMessageId(UUID.randomUUID());
        response.setPaymentGuid(request.getPaymentGuid());
        response.setAmount(request.getAmount());
        response.setCurrency(request.getCurrency());
        response.setTransactionRefId(transactionRefId);
        response.setStatus(status);
        response.setOccurredAt(Instant.now());

        resultListener.onMessage(response);
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down InMemoryXPaymentAdapterMessageBroker scheduler");
        scheduler.shutdownNow();
    }
}
