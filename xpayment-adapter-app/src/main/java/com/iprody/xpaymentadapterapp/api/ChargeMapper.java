package com.iprody.xpaymentadapterapp.api;

import com.iprody.xpayment.app.api.model.ChargeResponse;
import com.iprody.xpayment.app.api.model.CreateChargeRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
interface ChargeMapper {

    CreateChargeRequest toCreateChargeRequest(CreateChargeRequestDto dto);

    CreateChargeResponseDto toCreateChargeResponseDto(ChargeResponse chargeResponse);
}
