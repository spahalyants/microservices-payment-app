package com.iprody.paymentserviceapp.async;

/**
 * Contract for sending messages for asynchronous processing.
 * Business logic must only depend on this interface,
 * never on a concrete broker implementation.
 *
 * @param <T> the type of message to send
 */

public interface AsyncSender<T extends Message> {

    /**
     * Sends a message for asynchronous processing.
     * Returns immediately without waiting for processing to complete.
     *
     * @param message the message to send
     */

    void send(T message);

}
