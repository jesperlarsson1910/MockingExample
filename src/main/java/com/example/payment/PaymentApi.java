package com.example.payment;

import java.math.BigDecimal;

public interface PaymentApi {
    PaymentApiResponse charge(String API_KEY, BigDecimal amount) throws PaymentException;
}
