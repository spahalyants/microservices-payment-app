package com.iprody.paymentserviceapp.persistence;

import com.iprody.paymentserviceapp.persistence.model.Payment;
import com.iprody.paymentserviceapp.persistence.model.PaymentStatus;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;

public final class PaymentSpecifications {

    public static Specification<Payment> hasCurrency(String currency) {
        return (root, query, cb)
                -> cb.equal(root.get("currency"), currency);
    }

    // individual bounds — for when user provides only min OR only max

    public static Specification<Payment> amountGreaterOrEqual(BigDecimal min) {
        return (root, query, cb)
                -> cb.greaterThanOrEqualTo(root.get("amount"), min);
    }

    public static Specification<Payment> amountLessOrEqual(BigDecimal max) {
        return (root, query, cb)
                -> cb.lessThanOrEqualTo(root.get("amount"), max);
    }

    public static Specification<Payment> amountBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb)
                -> cb.between(root.get("amount"), min, max);
    }

    public static Specification<Payment> createdAfter(Instant after) {
        return (root, query, cb)
                -> cb.greaterThanOrEqualTo(root.get("createdAt"), after);
    }

    public static Specification<Payment> createdBefore(Instant before) {
        return (root, query, cb)
                -> cb.lessThanOrEqualTo(root.get("createdAt"), before);
    }

    public static Specification<Payment> createdBetween(Instant after, Instant before) {
        return (root, query, cb)
                -> cb.between(root.get("createdAt"), after, before);
    }

    public static Specification<Payment> hasStatus(PaymentStatus status) {
        return (root, query, cb)
                -> cb.equal(root.get("status"), status);
    }
}

/*

PaymentSpecifications — the individual filter building blocks.

Each method returns a Specification<Payment>, which is a functional interface
with one method: toPredicate(Root, CriteriaQuery, CriteriaBuilder)

Think of each specification as one SQL WHERE condition expressed in Java.

 */


