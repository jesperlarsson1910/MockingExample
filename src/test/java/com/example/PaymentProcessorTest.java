package com.example;

import com.example.payment.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentProcessorTest {

    //Mock classes
    @Mock
    private PaymentApi paymentApi;
    @Mock
    private PaymentRepo paymentRepo;
    @Mock
    private EmailService emailService;
    @Mock
    private static Billable order;
    @Mock
    private static PaymentConfig paymentConfig;

    @InjectMocks
    private PaymentProcessor paymentProcessor;

    //Default values
    private static final String API_KEY = "sk_test_123456";
    private static final String ORDER_ID = "B1234";
    private static final String EMAIL = "user@example.com";
    private static final String ALT_EMAIL = "alt.user@example.com";
    private static final BigDecimal PRICE = BigDecimal.valueOf(2000);
    private static final BigDecimal AMOUNT = BigDecimal.valueOf(1000);

    private static final String PAYMENT_ID = "Q1029384";
    private static final LocalDateTime TIMESTAMP = LocalDateTime.of(2026, 1, 1, 0, 0);

    //Help methods
    //Simulate an order and paymentConfig
    @BeforeEach
    public void setupInterfaces(TestInfo info){
        if (info.getDisplayName().contains("empty")){
            return; //avoid unnecessary stubbing
        }
        when(paymentConfig.getApiKey()).thenReturn(API_KEY);
        if(info.getDisplayName().contains("null")){
            return; //avoid unnecessary stubbing
        }
        when(order.getID()).thenReturn(ORDER_ID);
        when(order.getEmail()).thenReturn(EMAIL);
        when(order.getPrice()).thenReturn(PRICE);
        if(info.getDisplayName().contains("Currency")){
            return; //avoid unnecessary stubbing
        }
        when(order.getRemainingCost()).thenReturn(AMOUNT);
    }

    //Simulate apiResponse with desired status
    public PaymentApiResponse createPaymentApiResponse(PaymentStatus status){
        return new PaymentApiResponse(ORDER_ID, status, TIMESTAMP);
    }

    //Provide combos of null parameters
    private static Stream<Arguments> processPaymentNullParameterProvider() {
        return Stream.of(
                Arguments.of(null, AMOUNT),
                Arguments.of(order, null),
                Arguments.of(null, null),
                Arguments.of(order, null),
                Arguments.of(null, AMOUNT),
                Arguments.of(null, null)
        );
    }


    /*
     * Test for PaymentProcessor#processPayment(Billable order, BigDecimal amount, String email)
     *
     * - Valid parameters and no outages should be process, saved and email sent to preferred adress
     *
     * - Valid parameters with external outage should throw exception but still be saved
     *
     * - Valid parameters with paiment failure should save and then throw exception
     *
     * - Valid parameters with EmailService outage should still return payment status
     *
     * - Invalid parameters:
     *  - Null
     *  - Amount is zero
     *  - Order is already paid
     *  - Amount exceeds what is left to pay
     */


    @Test
    @DisplayName("Payment should be processed and saved with all valid parameters")
    public void tryProcessPaymentAllGreenDefaultEmail() throws ExternalServiceException, PaymentException, NotificationException {
        PaymentApiResponse response = createPaymentApiResponse(PaymentStatus.SUCCESS);
        when(paymentApi.charge(paymentConfig.getApiKey(), AMOUNT)).thenReturn(response);

        PaymentStatus charge = paymentProcessor.processPayment(order, AMOUNT, "");

        //should return success
        assertThat(charge.equals(PaymentStatus.SUCCESS)).isTrue();
        //make sure that transaction was saved and emails sent
        verify(paymentRepo).logPayment(order, AMOUNT, response);
        verify(emailService).sendPaymentConfirmation(EMAIL, order, AMOUNT, PaymentStatus.SUCCESS);
    }

    @Test
    @DisplayName("Pending payments should be handled as success")
    public void tryProcessPaymentPending() throws ExternalServiceException, PaymentException, NotificationException {
        PaymentApiResponse response = createPaymentApiResponse(PaymentStatus.PENDING);
        when(paymentApi.charge(paymentConfig.getApiKey(), AMOUNT)).thenReturn(response);

        PaymentStatus charge = paymentProcessor.processPayment(order, AMOUNT, "");

        //should return pending
        assertThat(charge.equals(PaymentStatus.PENDING)).isTrue();
        //make sure that transaction was saved and emails sent
        verify(paymentRepo).logPayment(order, AMOUNT, response);
        verify(emailService).sendPaymentConfirmation(EMAIL, order, AMOUNT, PaymentStatus.PENDING);
    }

    @Test
    @DisplayName("Failed payments should throw an error but still be logged")
    public void tryProcessPaymentFailed() throws ExternalServiceException, NotificationException {
        PaymentApiResponse response = createPaymentApiResponse(PaymentStatus.FAIL);
        when(paymentApi.charge(paymentConfig.getApiKey(), AMOUNT)).thenReturn(response);

        assertThatThrownBy(() -> paymentProcessor.processPayment(order, AMOUNT, ""))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("Payment failed");

        //make sure that transaction was saved
        verify(paymentRepo).logPayment(order, AMOUNT, response);
        //should not send email on failed transaction
        verify(emailService, never()).sendPaymentConfirmation(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Email is sent to preferred adress when specified")
    public void tryProcessPaymentPreferredEmail() throws ExternalServiceException, PaymentException, NotificationException {
        PaymentApiResponse response = createPaymentApiResponse(PaymentStatus.SUCCESS);
        when(paymentApi.charge(paymentConfig.getApiKey(), AMOUNT)).thenReturn(response);

        paymentProcessor.processPayment(order, AMOUNT, ALT_EMAIL);

        //ensure mail was sent to alt email rather than order email
        verify(emailService).sendPaymentConfirmation(ALT_EMAIL, order, AMOUNT, PaymentStatus.SUCCESS);
        verify(emailService, never()).sendPaymentConfirmation(EMAIL, order, AMOUNT, PaymentStatus.SUCCESS);
    }

    @Test
    @DisplayName("Payment should be saved even with external outage")
    public void tryProcessPaymentExternalOutage() throws ExternalServiceException, NotificationException {
        when(paymentApi.charge(paymentConfig.getApiKey(), AMOUNT)).thenThrow(new ExternalServiceException("Connection failure"));

        //make sure that exception was thrown
        assertThatThrownBy(() -> paymentProcessor.processPayment(order, AMOUNT, ""))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("External service failure");

        //should still be saved
        verify(paymentRepo).failedPayment(order, AMOUNT);
        //email should not be sent
        verify(emailService, never()).sendPaymentConfirmation(anyString(), any(), any(), any());
    }

    @Test
    @DisplayName("Status should be returned even if EmailService fails")
    public void tryProcessPaymentEmailServiceFails() throws ExternalServiceException, PaymentException, NotificationException {
        PaymentApiResponse response = createPaymentApiResponse(PaymentStatus.SUCCESS);
        when(paymentApi.charge(paymentConfig.getApiKey(), AMOUNT)).thenReturn(response);

        //simulate emailService throwing exception
        doThrow(new NotificationException("Email failure")).when(emailService).sendPaymentConfirmation(anyString(), any(), any(), any());

        PaymentStatus charge = paymentProcessor.processPayment(order, AMOUNT, EMAIL);

        //ensure status is still returned
        assertThat(charge.equals(PaymentStatus.SUCCESS)).isTrue();
        //ensure we called and tried to send email
        verify(emailService).sendPaymentConfirmation(EMAIL, order, AMOUNT, PaymentStatus.SUCCESS);
    }

    @ParameterizedTest
    @DisplayName("Null parameters should throw exception")
    @MethodSource("processPaymentNullParameterProvider")
    void tryProcessPaymentNullParameters(Billable order, BigDecimal amount) {

        //Null parameters should give exceptions for order and amount but not email
        assertThatThrownBy(() -> paymentProcessor.processPayment(order, amount, EMAIL))
                .isInstanceOf(IllegalArgumentException.class)
                .message()
                .containsAnyOf(
                        "Order cannot be null",
                                "Amount cannot be null");
    }

    @Test
    @DisplayName("No preferred email should not throw exception")
    void tryProcessPaymentNullEmail() throws ExternalServiceException {
        PaymentApiResponse response = createPaymentApiResponse(PaymentStatus.SUCCESS);
        when(paymentApi.charge(paymentConfig.getApiKey(), AMOUNT)).thenReturn(response);

        assertDoesNotThrow(() -> paymentProcessor.processPayment(order, AMOUNT, null));
    }

    @Test
    @DisplayName("Null or empty return from interfaces should throw exception")
    void tryProcessPaymentNullEmptyReturnFromInterface(){
        when(paymentConfig.getApiKey()).thenReturn(null, API_KEY);
        //Should throw exception if api key is null
        assertThatThrownBy(() -> paymentProcessor.processPayment(order, AMOUNT, ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ApiKey is null");


        when(order.getEmail()).thenReturn(null, "", "",EMAIL);


        //Should throw exceptions any critical data in order is missing
        for (int i = 0; i < 2; i++) {
            assertThatThrownBy(() -> paymentProcessor.processPayment(order, AMOUNT, ""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Order email is null or blank");
        }

        when(order.getID()).thenReturn(null,"", "", ORDER_ID);

        for (int i = 0; i < 2; i++) {
            assertThatThrownBy(() -> paymentProcessor.processPayment(order, AMOUNT, ""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Order ID is null or blank");
        }

        when(order.getPrice()).thenReturn(null,AMOUNT);
        when(order.getRemainingCost()).thenReturn(null,PRICE);

        for (int i = 0; i < 2; i++) {
            assertThatThrownBy(() -> paymentProcessor.processPayment(order, AMOUNT, ""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Price or remaining cost is null");
        }

    }

    @Test
    @DisplayName("Currency errors should throw exceptions")
    void tryProcessPaymentCurrencyErrors() {
        when(order.getRemainingCost()).thenReturn(AMOUNT, AMOUNT, BigDecimal.ZERO,AMOUNT);

        //Amount 0
        assertThatThrownBy(() -> paymentProcessor.processPayment(order, BigDecimal.ZERO, EMAIL))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("Amount must be greater than 0");

        //Order paid
        assertThatThrownBy(() -> paymentProcessor.processPayment(order, AMOUNT, EMAIL))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("Order has no outstanding balance");

        //Amount is greater than remaining cost
        assertThatThrownBy(() -> paymentProcessor.processPayment(order, AMOUNT.add(BigDecimal.ONE), EMAIL))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining("Amount exceeds outstanding balance");
    }


}