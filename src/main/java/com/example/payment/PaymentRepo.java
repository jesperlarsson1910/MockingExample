package com.example.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

public interface PaymentRepo {

    record PaymentInstance(
            Billable order,
            BigDecimal amount,
            PaymentApiResponse response,
            LocalDateTime timestamp
    ){};

    void logPayment(Billable order, BigDecimal amount, PaymentApiResponse response);

    void failedPayment(Billable order, BigDecimal amount);

    Set<PaymentInstance> getPayments(String orderID);

    Set<PaymentInstance> getPayments(Billable order);

    PaymentInstance getPayment(String paymentID);
}
