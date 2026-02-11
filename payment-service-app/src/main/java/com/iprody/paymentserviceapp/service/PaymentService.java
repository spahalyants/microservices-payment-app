package com.iprody.paymentserviceapp.service;

import com.iprody.paymentserviceapp.persistence.PaymentFilter;
import com.iprody.paymentserviceapp.persistence.PaymentFilterFactory;
import com.iprody.paymentserviceapp.persistence.PaymentRepository;
import com.iprody.paymentserviceapp.persistence.model.Payment;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;

    public List<Payment> findAll() {
        return this.paymentRepository.findAll();
    }

    public Page<Payment> search(PaymentFilter filter, Pageable pageable) {
        return this.paymentRepository.findAll(
                PaymentFilterFactory.fromFilter(filter), pageable);
    }
}
