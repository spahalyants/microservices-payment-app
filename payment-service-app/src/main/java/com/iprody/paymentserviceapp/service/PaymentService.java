package com.iprody.paymentserviceapp.service;

import com.iprody.paymentserviceapp.service.dto.PaymentDto;
import com.iprody.paymentserviceapp.persistence.PaymentFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PaymentService {
    List<PaymentDto> findAll();
    Page<PaymentDto> search(PaymentFilter filter, Pageable pageable);
}
