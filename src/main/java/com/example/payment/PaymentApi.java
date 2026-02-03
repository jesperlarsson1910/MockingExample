package com.example.payment;

public interface PaymentApi {
    PaymentApiResponse charge(String API_KEY, double amount) throws PaymentException;
}
