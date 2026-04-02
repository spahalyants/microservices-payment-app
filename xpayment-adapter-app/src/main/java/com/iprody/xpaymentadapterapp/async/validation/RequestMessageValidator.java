package com.iprody.xpaymentadapterapp.async.validation;

import com.iprody.xpaymentadapterapp.async.XPaymentAdapterRequestMessage;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Component
public class RequestMessageValidator {

    public void validate(XPaymentAdapterRequestMessage message) {
        validateAmount(message);
        validateCurrency(message);
        validateAmountScale(message);
    }

    private void validateAmount(XPaymentAdapterRequestMessage message) {
        if (message.getAmount() == null) {
            throw new MessageValidationException(
                    "Invalid message paymentGuid=" + message.getPaymentGuid()
                            + ": amount must not be null"
            );
        }
        if (message.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new MessageValidationException(
                    "Invalid message paymentGuid=" + message.getPaymentGuid()
                            + ": amount must not be negative, got " + message.getAmount()
            );
        }
    }

    private void validateCurrency(XPaymentAdapterRequestMessage message) {
        if (!StringUtils.hasText(message.getCurrency())) {
            throw new MessageValidationException(
                    "Invalid message paymentGuid=" + message.getPaymentGuid()
                            + ": currency must not be null or blank"
            );
        }
    }

    private void validateAmountScale(XPaymentAdapterRequestMessage message) {
        final int actualScale = message.getAmount().stripTrailingZeros().scale();
        final int allowedScale = CurrencyScale.scaleFor(message.getCurrency());

        if (actualScale > allowedScale) {
            throw new MessageValidationException(
                    "Invalid message paymentGuid=" + message.getPaymentGuid()
                            + ": currency " + message.getCurrency()
                            + " allows at most " + allowedScale + " decimal place(s)"
                            + " but amount " + message.getAmount() + " has " + actualScale
            );
        }
    }
}
