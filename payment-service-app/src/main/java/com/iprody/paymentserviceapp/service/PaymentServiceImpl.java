package com.iprody.paymentserviceapp.service;

import com.iprody.paymentserviceapp.exceptions.EntityNotFoundException;
import com.iprody.paymentserviceapp.exceptions.Operation;
import com.iprody.paymentserviceapp.mapper.PaymentMapper;
import com.iprody.paymentserviceapp.persistence.PaymentFilter;
import com.iprody.paymentserviceapp.persistence.PaymentFilterFactory;
import com.iprody.paymentserviceapp.persistence.PaymentRepository;
import com.iprody.paymentserviceapp.persistence.model.Payment;
import com.iprody.paymentserviceapp.service.dto.PaymentDto;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

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

    @Override
    public PaymentDto findByGuid(UUID guid) {
        return paymentRepository.findById(guid)
                .map(paymentMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Payment not found: " + guid,
                        Operation.FIND_BY_ID_OP,
                        guid
                ));
    }

    @Override
    public PaymentDto create(PaymentDto dto) {
        Payment entity = paymentMapper.toEntity(dto);
        entity.setGuid(null);
        Payment saved = paymentRepository.save(entity);
        return paymentMapper.toDto(saved);
    }

    @Override
    public PaymentDto update(UUID guid, PaymentDto dto) {
        Payment existing = paymentRepository.findById(guid)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Payment not found: " + guid,
                        Operation.UPDATE_OP,
                        guid
                ));

        existing.setInquiryRefId(dto.getInquiryRefId());
        existing.setAmount(dto.getAmount());
        existing.setCurrency(dto.getCurrency());
        existing.setStatus(dto.getStatus());
        existing.setTransactionRefId(dto.getTransactionRefId());
        existing.setNote(dto.getNote());

        return paymentMapper.toDto(paymentRepository.save(existing));
    }

    @Override
    public PaymentDto updateNote(UUID guid, String note) {
        Payment entity = paymentRepository.findById(guid)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Payment not found: " + guid,
                        Operation.UPDATE_NOTE_OP,
                        guid
                ));
        entity.setNote(note);
        return paymentMapper.toDto(paymentRepository.save(entity));
    }

    @Override
    public void delete(UUID guid) {
        if (!paymentRepository.existsById(guid)) {
            throw new EntityNotFoundException(
                    "Payment not found: " + guid,
                    Operation.DELETE_OP,
                    guid
            );
        }
        paymentRepository.deleteById(guid);
    }
}