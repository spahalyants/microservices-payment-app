package com.iprody.xpaymentadapterapp.async.kafka;

import com.iprody.xpaymentadapterapp.async.AsyncListener;
import com.iprody.xpaymentadapterapp.async.MessageHandler;
import com.iprody.xpaymentadapterapp.async.XPaymentAdapterRequestMessage;
import com.iprody.xpaymentadapterapp.async.dlt.DeadLetterQueueProducer;
import com.iprody.xpaymentadapterapp.async.validation.MessageValidationException;
import com.iprody.xpaymentadapterapp.async.validation.RequestMessageValidator;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class KafkaXPaymentAdapterRequestListenerAdapter implements AsyncListener<XPaymentAdapterRequestMessage> {

    private static final Logger log = LoggerFactory.getLogger(KafkaXPaymentAdapterRequestListenerAdapter.class);

    private final MessageHandler<XPaymentAdapterRequestMessage> handler;
    private final RequestMessageValidator validator;
    private final DeadLetterQueueProducer deadLetterQueueProducer;

    public KafkaXPaymentAdapterRequestListenerAdapter(
        MessageHandler<XPaymentAdapterRequestMessage> handler,
        RequestMessageValidator validator,
        DeadLetterQueueProducer deadLetterQueueProducer
    ) {
        this.handler = handler;
        this.validator = validator;
        this.deadLetterQueueProducer = deadLetterQueueProducer;
    }

    @Override
    public void onMessage(XPaymentAdapterRequestMessage message) {
        handler.handle(message);
    }

    @KafkaListener(
        topics = "${app.kafka.topics.x-payment-adapter.request}",
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(
        XPaymentAdapterRequestMessage message,
        ConsumerRecord<String, XPaymentAdapterRequestMessage> record,
        Acknowledgment ack
    ) {
        log.info("Received XPayment Adapter request: paymentGuid={}, partition={}, offset={}",
            message.getPaymentGuid(), record.partition(), record.offset());
        try {
            validator.validate(message);
            onMessage(message);
            ack.acknowledge();
        } catch (MessageValidationException e) {
            log.warn("Message failed validation, routing to DLT: paymentGuid={}, reason={}",
                message.getPaymentGuid(), e.getMessage());
            deadLetterQueueProducer.send(message, e.getMessage());
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error handling XPayment Adapter request for paymentGuid={}",
                message.getPaymentGuid(), e);
            throw e;
        }
    }
}
