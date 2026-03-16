package com.iprody.paymentserviceapp.async;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Incoming message representing the result of processing a payment transaction
 * through the X Payment Adapter.
 * <p>
 * Carries the original payment identifier, the transaction reference assigned
 * by X Payment, the final status, and the moment the result was produced.
 * The {@code messageId} uniquely identifies this response message itself.
 */

public class XPaymentAdapterResponseMessage implements Message {

    /**
     * Unique identifier of this response message.
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
     * Transaction reference identifier assigned by the X Payment Adapter.
     */
    private UUID transactionRefId;

    /**
     * Final status of the payment transaction in the X Payment Adapter.
     */
    private XPaymentAdapterStatus status;

    /**
     * The moment this response was produced.
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

    public UUID getTransactionRefId() {
        return transactionRefId;
    }

    public void setTransactionRefId(UUID transactionRefId) {
        this.transactionRefId = transactionRefId;
    }

    public XPaymentAdapterStatus getStatus() {
        return status;
    }

    public void setStatus(XPaymentAdapterStatus status) {
        this.status = status;
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }
}
