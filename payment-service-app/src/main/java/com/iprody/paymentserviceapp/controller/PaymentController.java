package com.iprody.paymentserviceapp.controller;

import com.iprody.paymentserviceapp.model.Payment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")

    public class PaymentController {
        private final Payment payment = new Payment(1L, 99.99);

        @GetMapping
        public Payment getPayment() {
            return payment;
        }

    }