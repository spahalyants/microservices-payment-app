package com.iprody.paymentserviceapp.async;

/**
 * Contract for listening to incoming messages from the async messaging system.
 * Implementations are responsible for transport-level concerns
 * such as validation and error handling before delegating
 * to a {@link MessageHandler}.
 *
 * @param <T> the type of message to receive
 */

public interface AsyncListener<T extends Message> {

    /**
     * Called for each incoming message received from the broker.
     *
     * @param message the incoming message to process
     */

    void onMessage(T message);

}
