package com.iprody.xpaymentadapterapp.checkstate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PaymentStateCheckRegistrarImpl implements PaymentStateCheckRegistrar {

    private static final Logger log = LoggerFactory.getLogger(PaymentStateCheckRegistrarImpl.class);

    private final RabbitTemplate rabbitTemplate;
    private final String exchangeName;
    private final String routingKey;
    private final long intervalMs;

    public PaymentStateCheckRegistrarImpl(
        RabbitTemplate rabbitTemplate,
        @Value("${app.rabbitmq.exchange-name}") String exchangeName,
        @Value("${app.rabbitmq.queue-name}") String routingKey,
        @Value("${app.rabbitmq.interval-ms}") long intervalMs
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
        this.intervalMs = intervalMs;
    }

    @Override
    public void register(
        UUID chargeGuid,
        UUID paymentGuid,
        BigDecimal amount,
        String currency
    ) {
        final PaymentCheckStateMessage message = new PaymentCheckStateMessage(
            chargeGuid,
            paymentGuid,
            amount,
            currency
        );
        log.info("Registering payment for status check: chargeGuid={}, paymentGuid={}",
            chargeGuid, paymentGuid);
        rabbitTemplate.convertAndSend(
            exchangeName,
            routingKey,
            message,
            m -> {
                m.getMessageProperties().setHeader("x-delay", intervalMs);
                m.getMessageProperties().setHeader("x-retry-count", 1);
                return m;
            }
        );
    }
}
