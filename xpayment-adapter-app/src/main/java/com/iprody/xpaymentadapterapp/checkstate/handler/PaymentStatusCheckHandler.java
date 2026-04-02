package com.iprody.xpaymentadapterapp.checkstate.handler;

import java.util.UUID;

public interface PaymentStatusCheckHandler {

    /**
     * Checks the payment status in X Payment Provider by the given ID.
     * If the status is non-terminal, returns false.
     * Otherwise, sends an async notification to Payment Service
     * about the changed payment status and returns true.
     *
     * @param chargeGuid UUID of the charge to check
     * @return true if the payment is completed and no further checks are needed, false otherwise
     */
    boolean handle(UUID chargeGuid);
}
