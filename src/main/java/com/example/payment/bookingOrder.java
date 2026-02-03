package com.example.payment;

import com.example.Booking;

public interface bookingOrder {

    Booking getBooking();

    String getEmail();

    double getAmount();

    double getRemainingBalance();

}
