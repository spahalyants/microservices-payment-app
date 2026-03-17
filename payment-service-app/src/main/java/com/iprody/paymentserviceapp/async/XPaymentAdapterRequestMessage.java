package com.iprody.paymentserviceapp.async;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Outgoing message representing a request to process a payment transaction
 * through the X Payment Adapter.
 * <p>
 * Carries the payment identifier, amount, currency, and the moment
 * the request was created. The {@code messageId} uniquely identifies
 * this request message itself, while {@code paymentGuid} references
 * the corresponding payment record in the Payment Service.
 */

public class XPaymentAdapterRequestMessage implements Message {

    /**
     * Unique identifier of this request message.
     */
    private UUID messageId;

    /**
     * Unique identifier of the payment record in the Payment Service.
     */
    private UUID paymentGuid;

    /**
     * Total amount of the payment.
     */
    private BigDecimal amount;

    /**
     * ISO 4217 currency code (e.g. "USD", "EUR").
     */
    private String currency;

    /**
     * The moment this request was created.
     */
    private Instant occurredAt;

    @Override
    public UUID getMessageId() {
        return messageId;
    }

    public void setMessageId(UUID messageId) {
        this.messageId = messageId;
    }

    public UUID getPaymentGuid() {
        return paymentGuid;
    }

    public void setPaymentGuid(UUID paymentGuid) {
        this.paymentGuid = paymentGuid;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }
}
