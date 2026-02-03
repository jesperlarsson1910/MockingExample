package com.example.payment;

import com.example.Booking;

import java.time.LocalDateTime;
import java.util.Set;

public interface PaymentRepo {

    record PaymentInstance(
            Booking booking,
            double amount,
            PaymentApiResponse response,
            LocalDateTime timestamp
    ){};

    void addPayment(Booking booking, double amount, PaymentApiResponse response);

    void failedPayment(Booking booking, double amount);

    Set<PaymentInstance> getPayments(String bookingID);

    Set<PaymentInstance> getPayments(Booking booking);

    PaymentInstance getPayment(String paymentID);
}
