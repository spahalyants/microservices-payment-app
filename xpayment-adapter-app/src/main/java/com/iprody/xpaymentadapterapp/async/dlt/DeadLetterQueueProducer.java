package com.iprody.xpaymentadapterapp.async.dlt;

import com.iprody.xpaymentadapterapp.async.XPaymentAdapterRequestMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class DeadLetterQueueProducer {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterQueueProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String dltTopic;

    public DeadLetterQueueProducer(
        KafkaTemplate<String, Object> kafkaTemplate,
        @Value("${app.kafka.topics.x-payment-adapter.dlt}") String dltTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.dltTopic = dltTopic;
    }

    public void send(XPaymentAdapterRequestMessage message, String reason) {
        final String key = message.getPaymentGuid() != null
            ? message.getPaymentGuid().toString()
            : "unknown";
        log.warn("Routing invalid message to DLT: paymentGuid={}, reason={}, topic={}",
            key, reason, dltTopic);
        kafkaTemplate.send(dltTopic, key, message);
    }
}
