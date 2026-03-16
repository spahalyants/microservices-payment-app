package com.iprody.paymentserviceapp.async;

import java.time.Instant;
import java.util.UUID;

/**
 * Base contract for all messages exchanged through the async messaging system.
 * Every message must carry a unique identifier and the time it occurred.
 */

public interface Message {

    /**
     * Returns the unique identifier of this message.
     *
     * @return UUID of the message
     */

    UUID getMessageId();

    /**
     * Returns the moment in time when this message was created.
     *
     * @return the occurrence timestamp
     */

    Instant getOccurredAt();

}
