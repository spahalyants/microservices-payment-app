package com.iprody.xpaymentadapterapp.async;

import java.time.Instant;
import java.util.UUID;

public interface Message {

    UUID getMessageId();

    Instant getOccurredAt();
}
