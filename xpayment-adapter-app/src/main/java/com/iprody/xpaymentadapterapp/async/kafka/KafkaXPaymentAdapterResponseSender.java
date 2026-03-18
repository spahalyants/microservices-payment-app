package com.iprody.xpaymentadapterapp.async.kafka;

import com.iprody.xpaymentadapterapp.async.AsyncSender;
import com.iprody.xpaymentadapterapp.async.XPaymentAdapterResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaXPaymentAdapterResponseSender implements AsyncSender<XPaymentAdapterResponseMessage> {

    private static final Logger log = LoggerFactory.getLogger(KafkaXPaymentAdapterResponseSender.class);

    private final KafkaTemplate<String, Object> template;
    private final String topic;

    public KafkaXPaymentAdapterResponseSender(
        KafkaTemplate<String, Object> template,
        @Value("${app.kafka.topics.x-payment-adapter.response}") String topic
    ) {
        this.template = template;
        this.topic = topic;
    }

    @Override
    public void send(XPaymentAdapterResponseMessage msg) {
        final String key = msg.getPaymentGuid().toString();
        log.info("Sending XPayment Adapter response: paymentGuid={}, status={}, amount={}, currency={} -> topic={}",
            msg.getPaymentGuid(), msg.getStatus(), msg.getAmount(), msg.getCurrency(), topic);
        template.send(topic, key, msg);
    }
}
