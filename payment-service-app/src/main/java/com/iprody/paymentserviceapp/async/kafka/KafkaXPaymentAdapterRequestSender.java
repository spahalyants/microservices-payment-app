package com.iprody.paymentserviceapp.async.kafka;

import com.iprody.paymentserviceapp.async.AsyncSender;
import com.iprody.paymentserviceapp.async.XPaymentAdapterRequestMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaXPaymentAdapterRequestSender
        implements AsyncSender<XPaymentAdapterRequestMessage> {

    private static final Logger log =
            LoggerFactory.getLogger(KafkaXPaymentAdapterRequestSender.class);

    private final KafkaTemplate<String, Object> template;
    private final String topic;

    @Autowired
    public KafkaXPaymentAdapterRequestSender(
            KafkaTemplate<String, Object> template,
            @Value("${app.kafka.topics.xpayment-adapter.request:xpayment-adapter.requests}") String topic
    ) {
        this.template = template;
        this.topic = topic;
    }

    @Override
    public void send(XPaymentAdapterRequestMessage msg) {
        String key = msg.getPaymentGuid().toString();
        log.info("Sending XPayment Adapter request: guid={}, amount={}, currency={} -> topic={}",
                msg.getPaymentGuid(), msg.getAmount(), msg.getCurrency(), topic);
        template.send(topic, key, msg);
    }
}
