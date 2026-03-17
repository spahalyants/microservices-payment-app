package com.iprody.paymentserviceapp.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Adapter that listens for response messages from the X Payment Adapter
 * and delegates them to the {@link MessageHandler} for business processing.
 * <p>
 * This class is responsible for transport-level concerns only.
 * Currently minimal because the in-memory broker requires no additional
 * transport logic. When a real broker such as Kafka replaces this
 * in-memory implementation, transport-specific logic will live here,
 * keeping the {@link MessageHandler} free of it.
 */

@Component
public class InMemoryXPaymentAdapterResultListenerAdapter
        implements AsyncListener<XPaymentAdapterResponseMessage> {

    private static final Logger log =
            LoggerFactory.getLogger(InMemoryXPaymentAdapterResultListenerAdapter.class);

    private final MessageHandler<XPaymentAdapterResponseMessage> handler;

    public InMemoryXPaymentAdapterResultListenerAdapter(
            MessageHandler<XPaymentAdapterResponseMessage> handler) {
        this.handler = handler;
    }

    @Override
    public void onMessage(XPaymentAdapterResponseMessage message) {
        log.debug("Received response message: messageId={}, paymentGuid={}, status={}",
                message.getMessageId(), message.getPaymentGuid(), message.getStatus());
        handler.handle(message);
    }
}
