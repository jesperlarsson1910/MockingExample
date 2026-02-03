package com.example.payment;

import java.time.LocalDateTime;

public record PaymentApiResponse(
        String paymentID,
        PaymentStatus status,
        LocalDateTime Timestamp) {

}
