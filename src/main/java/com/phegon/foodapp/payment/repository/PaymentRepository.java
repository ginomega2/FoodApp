package com.phegon.foodapp.payment.repository;

import com.phegon.foodapp.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
