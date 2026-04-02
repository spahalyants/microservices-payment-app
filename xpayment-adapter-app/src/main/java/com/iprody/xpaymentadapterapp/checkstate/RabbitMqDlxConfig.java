package com.iprody.xpaymentadapterapp.checkstate;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqDlxConfig {

    @Bean
    DirectExchange deadLetterExchange() {
        return new DirectExchange("payments.dlx");
    }

    @Bean
    Queue deadLetterQueue() {
        return QueueBuilder.durable("payments.dead.queue").build();
    }

    @Bean
    Binding dlxBinding() {
        return BindingBuilder.bind(deadLetterQueue())
            .to(deadLetterExchange())
            .with("payments.dead");
    }
}
