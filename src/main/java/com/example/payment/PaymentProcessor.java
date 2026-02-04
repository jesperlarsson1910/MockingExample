package com.example.payment;

import com.example.NotificationException;

import java.math.BigDecimal;

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

    /**
     *
     * @param order Object tied to the booking that should contain the price
     * @param amount Value to be paid
     * @param email Preferred email for receipt if different from order email
     * @return Status of the payment {@link PaymentStatus#SUCCESS} {@link PaymentStatus#PENDING} {@link PaymentStatus#FAIL}
     * @throws PaymentException
     */
    public PaymentStatus processPayment(Billable order, BigDecimal amount, String email) throws PaymentException {
        if(order == null){
            throw new IllegalArgumentException("Order cannot be null");
        }
        if(order.getEmail() == null || order.getEmail().isEmpty() || order.getID() == null || order.getID().isEmpty()
            || order.getPrice() == null || order.getRemainingCost() == null){
            throw new IllegalArgumentException("Order cannot contain null or empty values");
        }
        if(amount == null){
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if(amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentException("Amount must be greater than 0.");
        }
        if(order.getRemainingCost().compareTo(BigDecimal.ZERO) <= 0){
            throw new PaymentException("Order has no outstanding balance");
        }
        if(amount.compareTo(order.getRemainingCost()) > 0) {
            throw new PaymentException("Amount exceeds outstanding balance.");
        }

        //If no preferred email is provided the one connected to the booking is used
        if(email == null || email.isEmpty()) {
            email = order.getEmail();
        }


        // Call external API to handle charge
        PaymentApiResponse response;

        try{
            response = paymentApi.charge(API_KEY, amount);
        }
        catch(PaymentException e){
            //Error thrown if something went wrong with the external API, e.g. connection error
            //Still keep track of attempt to ensure complete history and possibly try again
            paymentRepo.failedPayment(order, amount);
            throw new PaymentException("External service failure", e);
        }


        //Write to db using repo as long as error wasn't thrown from external API
        paymentRepo.logPayment(order, amount, response);


        //If the payment failed due to reasons like not enough money. Still logged but error thrown to indicate issue
        if (response.status().equals(PaymentStatus.FAIL)) {
            throw new PaymentException("Payment failed");
        }


        //Send email if payment is success or pending
        try {
            emailService.sendPaymentConfirmation(email, order, amount, response.status());
        } catch (NotificationException e) {
            // Continue if confirmation fails
        }

        //Should be success most time but can be pending indicating that we should check status again later
        return response.status();
    }
}
