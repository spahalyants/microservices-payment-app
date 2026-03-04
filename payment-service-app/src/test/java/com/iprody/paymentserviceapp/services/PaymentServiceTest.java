package com.iprody.paymentserviceapp.services;

import com.iprody.paymentserviceapp.exceptions.EntityNotFoundException;
import com.iprody.paymentserviceapp.mapper.PaymentMapper;
import com.iprody.paymentserviceapp.persistence.PaymentFilter;
import com.iprody.paymentserviceapp.persistence.PaymentRepository;
import com.iprody.paymentserviceapp.persistence.model.Payment;
import com.iprody.paymentserviceapp.persistence.model.PaymentStatus;
import com.iprody.paymentserviceapp.service.PaymentServiceImpl;
import com.iprody.paymentserviceapp.service.dto.CreatePaymentDto;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Captor
    private ArgumentCaptor<Payment> paymentCaptor;

    // -------------------------------------------------------------------------
    // findByGuid
    // -------------------------------------------------------------------------

    @Test
    void shouldFindPaymentByGuid() {
        // given
        UUID guid = UUID.randomUUID();
        Payment entity = paymentEntity(guid, new BigDecimal("10.00"), "USD", PaymentStatus.APPROVED);
        PaymentDto dto = paymentDto(entity);

        when(paymentRepository.findById(guid)).thenReturn(Optional.of(entity));
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
        when(paymentRepository.findById(guid)).thenReturn(Optional.empty());

        // when
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> paymentService.findByGuid(guid)
        );

        // then
        assertEquals("Payment not found: " + guid, exception.getMessage());

        verify(paymentRepository).findById(guid);
        verifyNoMoreInteractions(paymentRepository);
        verifyNoInteractions(paymentMapper);
    }

    // -------------------------------------------------------------------------
    // findAll
    // -------------------------------------------------------------------------

    @Test
    void shouldReturnAllPaymentsMappedToDto() {
        // given
        Payment p1 = paymentEntity(UUID.randomUUID(), new BigDecimal("10.00"), "USD", PaymentStatus.APPROVED);
        Payment p2 = paymentEntity(UUID.randomUUID(), new BigDecimal("20.00"), "EUR", PaymentStatus.PENDING);
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

    // -------------------------------------------------------------------------
    // search
    // -------------------------------------------------------------------------

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
        assertThat(specCaptor.getValue()).isNotNull();
        verify(paymentMapper).toDto(entity);
        verifyNoMoreInteractions(paymentRepository, paymentMapper);
    }

    // -------------------------------------------------------------------------
    // create
    // -------------------------------------------------------------------------

    @Test
    void shouldCreatePaymentAndReturnDto() {
        // given
        CreatePaymentDto inputDto = new CreatePaymentDto(
                UUID.randomUUID(),
                new BigDecimal("42.50"),
                "USD",
                PaymentStatus.RECEIVED,
                "note"
        );
        Payment entity = paymentEntity(null, new BigDecimal("42.50"), "USD", PaymentStatus.RECEIVED);
        Payment savedEntity = paymentEntity(UUID.randomUUID(), new BigDecimal("42.50"), "USD", PaymentStatus.RECEIVED);
        PaymentDto savedDto = paymentDto(savedEntity);

        when(paymentMapper.toEntity(inputDto)).thenReturn(entity);
        when(paymentRepository.save(entity)).thenReturn(savedEntity);
        when(paymentMapper.toDto(savedEntity)).thenReturn(savedDto);

        // when
        PaymentDto result = paymentService.create(inputDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getGuid()).isEqualTo(savedEntity.getGuid());

        verify(paymentMapper).toEntity(inputDto);
        verify(paymentRepository).save(paymentCaptor.capture());
        assertThat(paymentCaptor.getValue().getGuid()).isNull();
        verify(paymentMapper).toDto(savedEntity);
        verifyNoMoreInteractions(paymentRepository, paymentMapper);
    }

    // -------------------------------------------------------------------------
    // update
    // -------------------------------------------------------------------------

    @Test
    void shouldUpdatePaymentAndReturnDto() {
        // given
        UUID guid = UUID.randomUUID();
        Payment existing = paymentEntity(guid, new BigDecimal("10.00"), "USD", PaymentStatus.PENDING);
        PaymentDto updateDto = paymentDto(
                paymentEntity(guid, new BigDecimal("99.99"), "EUR", PaymentStatus.APPROVED)
        );
        Payment savedEntity = paymentEntity(guid, new BigDecimal("99.99"), "EUR", PaymentStatus.APPROVED);
        PaymentDto savedDto = paymentDto(savedEntity);

        when(paymentRepository.findById(guid)).thenReturn(Optional.of(existing));
        when(paymentRepository.save(existing)).thenReturn(savedEntity);
        when(paymentMapper.toDto(savedEntity)).thenReturn(savedDto);

        // when
        PaymentDto result = paymentService.update(guid, updateDto);

        // then
        assertThat(result.getAmount()).isEqualTo(new BigDecimal("99.99"));
        assertThat(result.getCurrency()).isEqualTo("EUR");
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.APPROVED);

        verify(paymentRepository).findById(guid);
        verify(paymentRepository).save(existing);
        verify(paymentMapper).toDto(savedEntity);
        verifyNoMoreInteractions(paymentRepository, paymentMapper);
    }

    @Test
    void shouldThrowWhenUpdatingNonExistentPayment() {
        // given
        UUID guid = UUID.randomUUID();
        PaymentDto dto = paymentDto(paymentEntity(guid, new BigDecimal("10.00"), "USD", PaymentStatus.PENDING));

        when(paymentRepository.findById(guid)).thenReturn(Optional.empty());

        // when
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> paymentService.update(guid, dto)
        );

        // then
        assertEquals("Payment not found: " + guid, exception.getMessage());

        verify(paymentRepository).findById(guid);
        verifyNoMoreInteractions(paymentRepository);
        verifyNoInteractions(paymentMapper);
    }

    // -------------------------------------------------------------------------
    // updateNote
    // -------------------------------------------------------------------------

    @Test
    void shouldUpdateNoteAndReturnDto() {
        // given
        UUID guid = UUID.randomUUID();
        Payment existing = paymentEntity(guid, new BigDecimal("10.00"), "USD", PaymentStatus.APPROVED);
        Payment savedEntity = paymentEntity(guid, new BigDecimal("10.00"), "USD", PaymentStatus.APPROVED);
        savedEntity.setNote("new note");
        PaymentDto savedDto = paymentDto(savedEntity);

        when(paymentRepository.findById(guid)).thenReturn(Optional.of(existing));
        when(paymentRepository.save(existing)).thenReturn(savedEntity);
        when(paymentMapper.toDto(savedEntity)).thenReturn(savedDto);

        // when
        PaymentDto result = paymentService.updateNote(guid, "new note");

        // then
        assertThat(result.getNote()).isEqualTo("new note");

        verify(paymentRepository).findById(guid);
        verify(paymentRepository).save(existing);
        verify(paymentMapper).toDto(savedEntity);
        verifyNoMoreInteractions(paymentRepository, paymentMapper);
    }

    @Test
    void shouldThrowWhenUpdatingNoteOfNonExistentPayment() {
        // given
        UUID guid = UUID.randomUUID();
        when(paymentRepository.findById(guid)).thenReturn(Optional.empty());

        // when
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> paymentService.updateNote(guid, "note")
        );

        // then
        assertEquals("Payment not found: " + guid, exception.getMessage());

        verify(paymentRepository).findById(guid);
        verifyNoMoreInteractions(paymentRepository);
        verifyNoInteractions(paymentMapper);
    }

    // -------------------------------------------------------------------------
    // delete
    // -------------------------------------------------------------------------

    @Test
    void shouldDeletePaymentById() {
        // given
        UUID guid = UUID.randomUUID();
        when(paymentRepository.existsById(guid)).thenReturn(true);

        // when
        paymentService.delete(guid);

        // then
        verify(paymentRepository).existsById(guid);
        verify(paymentRepository).deleteById(guid);
        verifyNoMoreInteractions(paymentRepository);
        verifyNoInteractions(paymentMapper);
    }

    @Test
    void shouldThrowWhenDeletingNonExistentPayment() {
        // given
        UUID guid = UUID.randomUUID();
        when(paymentRepository.existsById(guid)).thenReturn(false);

        // when
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> paymentService.delete(guid)
        );

        // then
        assertEquals("Payment not found: " + guid, exception.getMessage());

        verify(paymentRepository).existsById(guid);
        verifyNoMoreInteractions(paymentRepository);
        verifyNoInteractions(paymentMapper);
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

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
