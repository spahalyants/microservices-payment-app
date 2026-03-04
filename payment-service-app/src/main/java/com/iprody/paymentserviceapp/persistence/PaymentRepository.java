package com.iprody.paymentserviceapp.persistence;

import com.iprody.paymentserviceapp.persistence.model.Payment;
import com.iprody.paymentserviceapp.persistence.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID>, JpaSpecificationExecutor<Payment> {

    List<Payment> findByStatus(PaymentStatus status);

}

