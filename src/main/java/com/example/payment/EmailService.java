package com.example.payment;

import com.example.Booking;
import com.example.NotificationException;

public interface EmailService {
    void sendPaymentConfirmation(String email, Booking booking,  double amount, PaymentStatus status) throws NotificationException;
}
