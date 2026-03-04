package com.iprody.paymentserviceapp;

import com.iprody.paymentserviceapp.persistence.PaymentFilter;
import com.iprody.paymentserviceapp.persistence.PaymentFilterFactory;
import com.iprody.paymentserviceapp.persistence.PaymentRepository;
import com.iprody.paymentserviceapp.persistence.model.Payment;
import com.iprody.paymentserviceapp.persistence.model.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.liquibase.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@ActiveProfiles("test")
class PaymentFilterFactoryJpaTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @BeforeEach
    void seed() {
        paymentRepository.deleteAllInBatch();

        paymentRepository.save(payment("USD", "10.00",
                Instant.parse("2026-01-10T00:00:00Z"), PaymentStatus.PENDING));

        paymentRepository.save(payment("USD", "50.00",
                Instant.parse("2026-01-20T00:00:00Z"), PaymentStatus.APPROVED));

        paymentRepository.save(payment("EUR", "100.00",
                Instant.parse("2026-02-10T00:00:00Z"), PaymentStatus.PENDING));
    }

    // b) criteria filters
    static Stream<Arguments> filterCases() {
        return Stream.of(
                // currency
                Arguments.of(new PaymentFilter("USD", null, null, null, null, null), 2),

                // min only
                Arguments.of(new PaymentFilter(null, bd("50.00"), null, null, null, null), 2),

                // max only
                Arguments.of(new PaymentFilter(null, null, bd("50.00"), null, null, null), 2),

                // range amount
                Arguments.of(new PaymentFilter(null, bd("10.00"), bd("50.00"), null, null, null), 2),

                // createdAfter only (>=)
                Arguments.of(new PaymentFilter(null, null, null,
                        Instant.parse("2026-01-15T00:00:00Z"), null, null), 2),

                // createdBefore only (<=)
                Arguments.of(new PaymentFilter(null, null, null,
                        null, Instant.parse("2026-02-01T00:00:00Z"), null), 2),

                // created range (between inclusive)
                Arguments.of(new PaymentFilter(null, null, null,
                        Instant.parse("2026-01-01T00:00:00Z"),
                        Instant.parse("2026-01-31T23:59:59Z"),
                        null), 2),

                // status
                Arguments.of(new PaymentFilter(null, null, null, null, null, PaymentStatus.PENDING), 2),

                // combined currency + status
                Arguments.of(new PaymentFilter("USD", null, null, null, null, PaymentStatus.PENDING), 1)
        );
    }

    @ParameterizedTest
    @MethodSource("filterCases")
    void shouldFilterCorrectly(PaymentFilter filter, int expectedCount) {
        // given
        Specification<Payment> spec = PaymentFilterFactory.fromFilter(filter);
        Pageable pageable = PageRequest.of(0, 25);

        // when
        Page<Payment> page = paymentRepository.findAll(spec, pageable);

        // then
        assertThat(page.getTotalElements()).isEqualTo(expectedCount);
    }

    // c) sorting
    @Test
    void shouldSortByAmountAsc() {
        // given
        Specification<Payment> spec = PaymentFilterFactory.fromFilter(
                new PaymentFilter(null, null, null, null, null, null)
        );
        Pageable pageable = PageRequest.of(0, 25, Sort.by(Sort.Direction.ASC, "amount"));

        // when
        Page<Payment> page = paymentRepository.findAll(spec, pageable);

        // then
        List<BigDecimal> amounts = page.getContent().stream().map(Payment::getAmount).toList();
        assertThat(amounts).containsExactly(
                new BigDecimal("10.00"),
                new BigDecimal("50.00"),
                new BigDecimal("100.00")
        );
    }

    @Test
    void shouldSortByCreatedAtDesc() {
        // given
        Specification<Payment> spec = PaymentFilterFactory.fromFilter(
                new PaymentFilter(null, null, null, null, null, null)
        );
        Pageable pageable = PageRequest.of(0, 25, Sort.by(Sort.Direction.DESC, "createdAt"));

        // when
        Page<Payment> page = paymentRepository.findAll(spec, pageable);

        // then
        List<Instant> dates = page.getContent().stream().map(Payment::getCreatedAt).toList();
        assertThat(dates).containsExactly(
                Instant.parse("2026-02-10T00:00:00Z"),
                Instant.parse("2026-01-20T00:00:00Z"),
                Instant.parse("2026-01-10T00:00:00Z")
        );
    }

    // d) pagination defaults
    @Test
    void shouldReturnFirstPageWithDefaultSize25() {
        // given
        Specification<Payment> spec = PaymentFilterFactory.fromFilter(
                new PaymentFilter(null, null, null, null, null, null)
        );
        Pageable pageable = PageRequest.of(0, 25);

        // when
        Page<Payment> page = paymentRepository.findAll(spec, pageable);

        // then
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.getSize()).isEqualTo(25);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent()).hasSize(3);
    }

    // helpers
    private static Payment payment(String currency, String amount, Instant createdAt, PaymentStatus status) {
        Payment p = new Payment();
        p.setInquiryRefId(UUID.randomUUID());
        p.setTransactionRefId(UUID.randomUUID());
        p.setCurrency(currency);
        p.setAmount(new BigDecimal(amount));
        p.setStatus(status);
        p.setNote("note");
        p.setCreatedAt(createdAt);
        p.setUpdatedAt(createdAt);
        return p;
    }

    private static BigDecimal bd(String s) {
        return new BigDecimal(s);
    }
}
