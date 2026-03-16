package com.iprody.paymentserviceapp.async;

import com.iprody.paymentserviceapp.persistence.PaymentRepository;
import com.iprody.paymentserviceapp.persistence.model.PaymentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Handles incoming response messages from the X Payment Adapter.
 * <p>
 * Looks up the corresponding Payment record in the database and updates
 * its status and transaction reference based on the result received
 * from the X Payment Adapter.
 * <p>
 * This class contains pure business logic and has no knowledge of
 * how or from where the message was delivered.
 */

@Component
public class XPaymentAdapterResponseMessageHandler
        implements MessageHandler<XPaymentAdapterResponseMessage> {

    private static final Logger log =
            LoggerFactory.getLogger(XPaymentAdapterResponseMessageHandler.class);

    private final PaymentRepository paymentRepository;

    public XPaymentAdapterResponseMessageHandler(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public void handle(XPaymentAdapterResponseMessage message) {
        log.info("Handling XPayment response: paymentGuid={}, status={}, messageId={}",
                message.getPaymentGuid(), message.getStatus(), message.getMessageId());

        paymentRepository.findById(message.getPaymentGuid()).ifPresentOrElse(
                payment -> {
                    PaymentStatus newStatus = mapStatus(message.getStatus());
                    payment.setStatus(newStatus);
                    payment.setTransactionRefId(message.getTransactionRefId());
                    paymentRepository.save(payment);
                    log.info("Payment updated: guid={}, newStatus={}, transactionRefId={}",
                            payment.getGuid(), newStatus, message.getTransactionRefId());
                },
                () -> log.warn("Received response for unknown paymentGuid={}",
                        message.getPaymentGuid())
        );
    }

    private PaymentStatus mapStatus(XPaymentAdapterStatus adapterStatus) {
        return switch (adapterStatus) {
            case SUCCEEDED -> PaymentStatus.APPROVED;
            case CANCELED -> PaymentStatus.DECLINED;
            case PROCESSING -> PaymentStatus.PROCESSING;
        };
    }
}
