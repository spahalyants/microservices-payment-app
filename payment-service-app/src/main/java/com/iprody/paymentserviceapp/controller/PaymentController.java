package com.iprody.paymentserviceapp.controller;

import com.iprody.paymentserviceapp.model.Payment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final Map<Long, Payment> storage = new HashMap<>();

    public PaymentController() {
        storage.put(1L, new Payment(1L, 99.99));
        storage.put(2L, new Payment(2L, 150.50));
        storage.put(3L, new Payment(3L, 20.00));
        storage.put(4L, new Payment(4L, 75.25));
    }

    @GetMapping ()
    public List<Payment> getAllPayments() {
        return new ArrayList<>(storage.values());

    }@GetMapping("/{id}")
    public Payment getPaymentById(@PathVariable Long id) {
        return storage.get(id);
    }

}