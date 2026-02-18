package com.iprody.paymentserviceapp.persistence;

import com.iprody.paymentserviceapp.persistence.model.Payment;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class PaymentFilterFactory {

    public static Specification<Payment> fromFilter(PaymentFilter filter) {

        Specification<Payment> spec = Specification.unrestricted();

        if (StringUtils.hasText(filter.currency())) {
            spec = spec.and(PaymentSpecifications.hasCurrency(filter.currency()));
        }

        // amount: both, only min, or only max

        if (filter.minAmount() != null && filter.maxAmount() != null) {
            spec = spec.and(PaymentSpecifications.amountBetween(
                    filter.minAmount(), filter.maxAmount()));
        } else if (filter.minAmount() != null) {
            spec = spec.and(PaymentSpecifications.amountGreaterOrEqual(filter.minAmount()));
        } else if (filter.maxAmount() != null) {
            spec = spec.and(PaymentSpecifications.amountLessOrEqual(filter.maxAmount()));
        }

        // dates: both, only after, or only before

        if (filter.createdAfter() != null && filter.createdBefore() != null) {
            spec = spec.and(PaymentSpecifications.createdBetween(
                    filter.createdAfter(), filter.createdBefore()));
        } else if (filter.createdAfter() != null) {
            spec = spec.and(PaymentSpecifications.createdAfter(filter.createdAfter()));
        } else if (filter.createdBefore() != null) {
            spec = spec.and(PaymentSpecifications.createdBefore(filter.createdBefore()));
        }

        if (filter.status() != null) {
            spec = spec.and(PaymentSpecifications.hasStatus(filter.status()));
        }

        return spec;
    }
}

// PaymentFilterFactory — the orchestrator
// This is where the magic of dynamic composition happens.