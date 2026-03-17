package com.iprody.paymentserviceapp.async;

/**
 * Contract for processing incoming messages that have already been
 * received and validated by an {@link AsyncListener}.
 * Implementations contain pure business logic and have no knowledge
 * of the underlying transport or broker technology.
 *
 * @param <T> the type of message to handle
 */

public interface MessageHandler<T extends Message> {

    /**
     * Handles the given message according to business rules.
     *
     * @param message the message to handle
     */

    void handle(T message);

}