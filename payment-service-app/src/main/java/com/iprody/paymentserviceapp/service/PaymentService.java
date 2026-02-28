package com.iprody.paymentserviceapp.service;

import com.iprody.paymentserviceapp.service.dto.PaymentDto;
import com.iprody.paymentserviceapp.persistence.PaymentFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface PaymentService {

    List<PaymentDto> findAll();

    Page<PaymentDto> search(PaymentFilter filter, Pageable pageable);

    PaymentDto findByGuid(UUID guid);

    PaymentDto create(PaymentDto dto);

    PaymentDto update(UUID guid, PaymentDto dto);

    PaymentDto updateNote(UUID guid, String note);

    void delete(UUID guid);
}
