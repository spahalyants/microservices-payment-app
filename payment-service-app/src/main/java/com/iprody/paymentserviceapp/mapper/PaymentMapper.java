package com.iprody.paymentserviceapp.mapper;

import com.iprody.paymentserviceapp.persistence.model.Payment;
import com.iprody.paymentserviceapp.service.dto.CreatePaymentDto;
import com.iprody.paymentserviceapp.service.dto.PaymentDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    PaymentDto toDto(Payment payment);
    Payment toEntity(PaymentDto dto);

    @Mapping(target = "guid", ignore = true)
    @Mapping(target = "transactionRefId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Payment toEntity(CreatePaymentDto dto);

}
