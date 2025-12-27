package com.phegon.foodapp.payment.controller;

import com.phegon.foodapp.payment.dtos.PaymentDTO;
import com.phegon.foodapp.payment.services.PaymentService;
import com.phegon.foodapp.response.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymenController {

    private final PaymentService paymentService;

    @PostMapping("/pay")
    public ResponseEntity<Response<?>> initializePayment(@RequestBody @Valid PaymentDTO paymentRequest) {
        return ResponseEntity.ok(paymentService.initializePayment(paymentRequest));
    }


    @PutMapping("/update")
    public void updatePaymentForOrder(@RequestBody PaymentDTO paymentRequest) {
        paymentService.updatePaymentForOrder(paymentRequest);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response<List<PaymentDTO>>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }


    @GetMapping("/{paymendId}")
    public ResponseEntity<Response<PaymentDTO>> getPaymentById(@PathVariable Long paymendId) {
        return ResponseEntity.ok(paymentService.getPaymentById(paymendId));


    }
}
