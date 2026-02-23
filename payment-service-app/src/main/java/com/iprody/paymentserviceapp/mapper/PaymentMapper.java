package com.iprody.paymentserviceapp.mapper;

import com.iprody.paymentserviceapp.persistence.model.Payment;
import com.iprody.paymentserviceapp.service.dto.PaymentDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    PaymentDto toDto(Payment payment);
    Payment toEntity(PaymentDto dto);

}
