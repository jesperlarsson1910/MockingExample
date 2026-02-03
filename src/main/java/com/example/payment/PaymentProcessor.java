package com.example.payment;

import com.example.NotificationException;

import java.time.LocalDateTime;

public class PaymentProcessor {

    private final String API_KEY;
    private final PaymentApi paymentApi;
    private final PaymentRepo paymentRepo;
    private final EmailService emailService;

    public PaymentProcessor(String API_KEY, PaymentApi paymentApi, PaymentRepo paymentRepo, EmailService emailService) {
        this.API_KEY = API_KEY;
        this.paymentApi = paymentApi;
        this.paymentRepo = paymentRepo;
        this.emailService = emailService;
    }

    public PaymentStatus processPayment(bookingOrder order, double amount, String email) throws PaymentException {

        if(amount <= 0) {
            throw new PaymentException("Amount must be greater than 0.");
        }
        if(order.getRemainingBalance() <= 0){
            throw new PaymentException("Order has no outstanding balance");
        }
        if(amount > order.getRemainingBalance()) {
            throw new PaymentException("Amount is greater than outstanding balance.");
        }


        // Anropar extern betaltjänst direkt med statisk API-nyckel
        PaymentApiResponse response;

        try{
            response = paymentApi.charge(API_KEY, amount);
        }
        catch(PaymentException e){
            paymentRepo.failedPayment(order.getBooking(), amount);
            throw new PaymentException("External service failure", e);
        }


        // Skriver till databas direkt
        paymentRepo.addPayment(order.getBooking(), amount, response);

        if (response.status().equals(PaymentStatus.FAIL)) {
            throw new PaymentException("Payment failed");
        }


        // Skickar e-post direkt
        try {

            emailService.sendPaymentConfirmation(email, order.getBooking(), amount, response.status());
        } catch (NotificationException e) {
            // Continue if confirmation fails
        }

        return response.status();
    }
}
