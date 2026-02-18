package com.iprody.paymentserviceapp.mapper;

import com.iprody.paymentserviceapp.persistence.model.Payment;
import com.iprody.paymentserviceapp.persistence.model.PaymentStatus;
import com.iprody.paymentserviceapp.service.dto.PaymentDto;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentMapperTest {

    // Even though our mapper is componentModel="spring",
    // MapStruct still generates an implementation that can be obtained like this for unit tests.

    //Create the object we are testing (the mapper implementation) without Spring.

    private final PaymentMapper mapper = Mappers.getMapper(PaymentMapper.class);

    @Test
    void shouldMapEntityToDto() {
        // given
        UUID guid = UUID.randomUUID();
        UUID inquiryRefId = UUID.randomUUID();
        UUID transactionRefId = UUID.randomUUID();
        Instant createdAt = Instant.now();
        Instant updatedAt = Instant.now();

        Payment payment = new Payment();
        payment.setGuid(guid);
        payment.setInquiryRefId(inquiryRefId);
        payment.setAmount(new BigDecimal("123.45"));
        payment.setCurrency("USD");
        payment.setTransactionRefId(transactionRefId);
        payment.setStatus(PaymentStatus.APPROVED);
        payment.setNote("some note");
        payment.setCreatedAt(createdAt);
        payment.setUpdatedAt(updatedAt);

        // when
        PaymentDto dto = mapper.toDto(payment);

        // then
        assertThat(dto).isNotNull();
        assertThat(dto.getGuid()).isEqualTo(payment.getGuid());
        assertThat(dto.getInquiryRefId()).isEqualTo(payment.getInquiryRefId());
        assertThat(dto.getAmount()).isEqualTo(payment.getAmount());
        assertThat(dto.getCurrency()).isEqualTo(payment.getCurrency());
        assertThat(dto.getTransactionRefId()).isEqualTo(payment.getTransactionRefId());
        assertThat(dto.getStatus()).isEqualTo(payment.getStatus());
        assertThat(dto.getNote()).isEqualTo(payment.getNote());
        assertThat(dto.getCreatedAt()).isEqualTo(payment.getCreatedAt());
        assertThat(dto.getUpdatedAt()).isEqualTo(payment.getUpdatedAt());
    }

    @Test
    void shouldMapDtoToEntity() {

        // given
        UUID guid = UUID.randomUUID();
        UUID inquiryRefId = UUID.randomUUID();
        UUID transactionRefId = UUID.randomUUID();
        Instant createdAt = Instant.now();
        Instant updatedAt = Instant.now();

        PaymentDto dto = new PaymentDto();
        dto.setGuid(guid);
        dto.setInquiryRefId(inquiryRefId);
        dto.setAmount(new BigDecimal("999.99"));
        dto.setCurrency("EUR");
        dto.setTransactionRefId(transactionRefId);
        dto.setStatus(PaymentStatus.PENDING);
        dto.setNote("dto note");
        dto.setCreatedAt(createdAt);
        dto.setUpdatedAt(updatedAt);

        // when
        Payment entity = mapper.toEntity(dto);

        // then
        assertThat(entity).isNotNull();
        assertThat(entity.getGuid()).isEqualTo(dto.getGuid());
        assertThat(entity.getInquiryRefId()).isEqualTo(dto.getInquiryRefId());
        assertThat(entity.getAmount()).isEqualTo(dto.getAmount());
        assertThat(entity.getCurrency()).isEqualTo(dto.getCurrency());
        assertThat(entity.getTransactionRefId()).isEqualTo(dto.getTransactionRefId());
        assertThat(entity.getStatus()).isEqualTo(dto.getStatus());
        assertThat(entity.getNote()).isEqualTo(dto.getNote());
        assertThat(entity.getCreatedAt()).isEqualTo(dto.getCreatedAt());
        assertThat(entity.getUpdatedAt()).isEqualTo(dto.getUpdatedAt());
    }
}