package com.iprody.paymentserviceapp.services;

import com.iprody.paymentserviceapp.mapper.PaymentMapper;
import com.iprody.paymentserviceapp.persistence.PaymentFilter;
import com.iprody.paymentserviceapp.persistence.PaymentRepository;
import com.iprody.paymentserviceapp.persistence.model.Payment;
import com.iprody.paymentserviceapp.persistence.model.PaymentStatus;
import com.iprody.paymentserviceapp.service.PaymentServiceImpl;
import com.iprody.paymentserviceapp.service.dto.PaymentDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Captor
    private ArgumentCaptor<Specification<Payment>> specCaptor;


    @Test
    void shouldFindPaymentByGuid() {
        // given
        UUID guid = UUID.randomUUID();
        Payment entity = paymentEntity(guid, new BigDecimal("10.00"), "USD", PaymentStatus.APPROVED);
        PaymentDto dto = paymentDto(entity);

        when(paymentRepository.findById(guid)).thenReturn(java.util.Optional.of(entity));
        when(paymentMapper.toDto(entity)).thenReturn(dto);

        // when
        PaymentDto result = paymentService.findByGuid(guid);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getGuid()).isEqualTo(guid);
        assertThat(result.getAmount()).isEqualTo(new BigDecimal("10.00"));
        assertThat(result.getCurrency()).isEqualTo("USD");
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.APPROVED);

        verify(paymentRepository).findById(guid);
        verify(paymentMapper).toDto(entity);
        verifyNoMoreInteractions(paymentRepository, paymentMapper);
    }

    @Test
    void shouldThrowWhenPaymentNotFoundByGuid() {
        // given
        UUID guid = UUID.randomUUID();
        when(paymentRepository.findById(guid)).thenReturn(java.util.Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> paymentService.findByGuid(guid));

        // then
        assertThat(thrown)
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Payment not found: " + guid);

        verify(paymentRepository).findById(guid);
        verifyNoMoreInteractions(paymentRepository);
        verifyNoInteractions(paymentMapper);
    }


    @Test
    void shouldReturnAllPaymentsMappedToDto() {
        // given
        Payment p1 = paymentEntity(UUID.randomUUID(),
                     new BigDecimal("10.00"),
                     "USD",
                     PaymentStatus.APPROVED);

        Payment p2 = paymentEntity(UUID.randomUUID(),
                     new BigDecimal("20.00"),
                     "EUR",
                     PaymentStatus.PENDING);

        PaymentDto d1 = paymentDto(p1);
        PaymentDto d2 = paymentDto(p2);

        when(paymentRepository.findAll()).thenReturn(List.of(p1, p2));
        when(paymentMapper.toDto(p1)).thenReturn(d1);
        when(paymentMapper.toDto(p2)).thenReturn(d2);

        // when
        List<PaymentDto> result = paymentService.findAll();

        // then
        assertThat(result).containsExactly(d1, d2);
        verify(paymentRepository).findAll();
        verify(paymentMapper).toDto(p1);
        verify(paymentMapper).toDto(p2);
        verifyNoMoreInteractions(paymentRepository, paymentMapper);
    }

    @Test
    void shouldSearchUsingSpecificationBuiltFromFilter_andMapPageToDto() {
        // given
        PaymentFilter filter = new PaymentFilter(
                "USD",
                new BigDecimal("10.00"),
                new BigDecimal("50.00"),
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-02-01T00:00:00Z"),
                PaymentStatus.PENDING
        );

        Pageable pageable = PageRequest.of(0, 25, Sort.by(Sort.Direction.DESC, "amount"));

        Payment entity = paymentEntity(UUID.randomUUID(), new BigDecimal("12.34"), "USD", PaymentStatus.PENDING);
        PaymentDto dto = paymentDto(entity);

        Page<Payment> entityPage = new PageImpl<>(List.of(entity), pageable, 1);

        when(paymentRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(entityPage);
        when(paymentMapper.toDto(entity)).thenReturn(dto);

        // when
        Page<PaymentDto> result = paymentService.search(filter, pageable);

        // then
        assertThat(result.getContent()).containsExactly(dto);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getPageable().getPageSize()).isEqualTo(25);
        assertThat(result.getSort()).isEqualTo(Sort.by(Sort.Direction.DESC, "amount"));

        verify(paymentRepository).findAll(specCaptor.capture(), eq(pageable));
        assertThat(specCaptor.getValue()).isNotNull(); // spec реально построен
        verify(paymentMapper).toDto(entity);
        verifyNoMoreInteractions(paymentRepository, paymentMapper);
    }

    // helpers
    private static Payment paymentEntity(UUID guid, BigDecimal amount, String currency, PaymentStatus status) {
        Payment p = new Payment();
        p.setGuid(guid);
        p.setInquiryRefId(UUID.randomUUID());
        p.setTransactionRefId(UUID.randomUUID());
        p.setAmount(amount);
        p.setCurrency(currency);
        p.setStatus(status);
        p.setNote("note");
        p.setCreatedAt(Instant.parse("2026-01-10T10:00:00Z"));
        p.setUpdatedAt(Instant.parse("2026-01-10T10:05:00Z"));
        return p;
    }

    private static PaymentDto paymentDto(Payment entity) {
        PaymentDto dto = new PaymentDto();
        dto.setGuid(entity.getGuid());
        dto.setInquiryRefId(entity.getInquiryRefId());
        dto.setAmount(entity.getAmount());
        dto.setCurrency(entity.getCurrency());
        dto.setTransactionRefId(entity.getTransactionRefId());
        dto.setStatus(entity.getStatus());
        dto.setNote(entity.getNote());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
