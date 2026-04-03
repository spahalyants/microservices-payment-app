package com.iprody.xpaymentadapterapp.checkstate;

import com.iprody.xpaymentadapterapp.checkstate.handler.PaymentStatusCheckHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PaymentStateCheckListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentStateCheckListener.class);

    private final RabbitTemplate rabbitTemplate;
    private final String exchangeName;
    private final String routingKey;
    private final String dlxExchangeName;
    private final String dlxRoutingKey;
    private final int maxAttempts;
    private final long intervalMs;
    private final PaymentStatusCheckHandler paymentStatusCheckHandler;

    public PaymentStateCheckListener(
        RabbitTemplate rabbitTemplate,
        @Value("${app.rabbitmq.exchange-name}") String exchangeName,
        @Value("${app.rabbitmq.queue-name}") String routingKey,
        @Value("${app.rabbitmq.dlx-exchange-name}") String dlxExchangeName,
        @Value("${app.rabbitmq.dlx-routing-key}") String dlxRoutingKey,
        @Value("${app.rabbitmq.max-attempts}") int maxAttempts,
        @Value("${app.rabbitmq.interval-ms}") long intervalMs,
        PaymentStatusCheckHandler paymentStatusCheckHandler
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
        this.dlxExchangeName = dlxExchangeName;
        this.dlxRoutingKey = dlxRoutingKey;
        this.maxAttempts = maxAttempts;
        this.intervalMs = intervalMs;
        this.paymentStatusCheckHandler = paymentStatusCheckHandler;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue-name}")
    public void handle(PaymentCheckStateMessage message, Message raw) {
        final MessageProperties props = raw.getMessageProperties();
        final Object retryHeader = props.getHeaders().getOrDefault("x-retry-count", 0);
        final int retryCount = retryHeader instanceof Integer i ? i : 0;
        log.info("Status check received: chargeGuid={}, attempt={}/{}",
            message.getChargeGuid(), retryCount, maxAttempts);

        final boolean terminal = paymentStatusCheckHandler.handle(message.getChargeGuid());

        if (terminal) {
            return;
        }

        if (retryCount < maxAttempts) {
            final PaymentCheckStateMessage retryMessage = new PaymentCheckStateMessage(
                message.getChargeGuid(),
                message.getPaymentGuid(),
                message.getAmount(),
                message.getCurrency()
            );
            rabbitTemplate.convertAndSend(
                exchangeName,
                routingKey,
                retryMessage,
                m -> {
                    m.getMessageProperties().setHeader("x-delay", intervalMs);
                    m.getMessageProperties().setHeader("x-retry-count", retryCount + 1);
                    return m;
                }
            );
            log.info("Rescheduled status check: chargeGuid={}, next attempt={}",
                message.getChargeGuid(), retryCount + 1);
        } else {
            rabbitTemplate.convertAndSend(
                dlxExchangeName,
                dlxRoutingKey,
                message,
                m -> {
                    m.getMessageProperties().setHeader("x-retry-count", retryCount);
                    m.getMessageProperties().setHeader("x-final-status", "TIMEOUT");
                    m.getMessageProperties().setHeader("x-original-queue", routingKey);
                    return m;
                }
            );
            log.warn("Max attempts reached for chargeGuid={}, routing to DLX",
                message.getChargeGuid());
        }
    }
}
