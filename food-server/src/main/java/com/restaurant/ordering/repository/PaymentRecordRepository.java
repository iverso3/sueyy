package com.restaurant.ordering.repository;

import com.restaurant.ordering.model.entity.PaymentRecord;
import com.restaurant.ordering.model.enums.PaymentRecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, Long> {

    Optional<PaymentRecord> findByPaymentNo(String paymentNo);

    Optional<PaymentRecord> findByTransactionId(String transactionId);

    Optional<PaymentRecord> findByOrderId(Long orderId);
}