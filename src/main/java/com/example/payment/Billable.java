package com.example.payment;

import java.math.BigDecimal;

public interface Billable {

    String getID();

    String getEmail();

    BigDecimal getPrice();

    BigDecimal getRemainingCost();

}
