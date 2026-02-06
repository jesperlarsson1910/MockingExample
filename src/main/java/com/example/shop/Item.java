package com.example.shop;

import java.math.BigDecimal;

public class Item {
    BigDecimal price;

    public Item (BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getPrice() {
        return price;
    }
}
