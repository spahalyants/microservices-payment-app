package com.iprody.paymentserviceapp.service;

import com.iprody.paymentserviceapp.mapper.PaymentMapper;
import com.iprody.paymentserviceapp.persistence.PaymentFilter;
import com.iprody.paymentserviceapp.persistence.PaymentFilterFactory;
import com.iprody.paymentserviceapp.persistence.PaymentRepository;
import com.iprody.paymentserviceapp.service.dto.PaymentDto;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    @Override
    public List<PaymentDto> findAll() {
        return paymentRepository.findAll()
                .stream()
                .map(paymentMapper::toDto)
                .toList();
    }

    @Override
    public Page<PaymentDto> search(PaymentFilter filter, Pageable pageable) {
        return paymentRepository
                .findAll(PaymentFilterFactory.fromFilter(filter), pageable)
                .map(paymentMapper::toDto);
    }
}