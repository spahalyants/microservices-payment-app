package com.iprody.paymentserviceapp.mapper;

import com.iprody.paymentserviceapp.async.XPaymentAdapterRequestMessage;
import com.iprody.paymentserviceapp.persistence.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting a {@link Payment} entity into an
 * {@link XPaymentAdapterRequestMessage} to be sent to the X Payment Adapter.
 */

@Mapper(componentModel = "spring")
public interface XPaymentAdapterMapper {

    @Mapping(source = "guid", target = "paymentGuid")
    @Mapping(source = "amount", target = "amount")
    @Mapping(source = "currency", target = "currency")
    @Mapping(source = "updatedAt", target = "occurredAt")
    @Mapping(target = "messageId", expression = "java(java.util.UUID.randomUUID())")
    XPaymentAdapterRequestMessage toRequestMessage(Payment payment);

}
