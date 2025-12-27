package com.phegon.foodapp.payment.services;

import com.phegon.foodapp.payment.dtos.PaymentDTO;
import com.phegon.foodapp.response.Response;

import java.util.List;

public interface PaymentService {

    Response<?> initializePayment(PaymentDTO paymentDTO);
    void updatePaymentForOrder(PaymentDTO paymentDTO);
    Response<List<PaymentDTO>> getAllPayments();
    Response<PaymentDTO> getPaymentById(Long paymentId);



}
