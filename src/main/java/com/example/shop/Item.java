package com.example.shop;

import java.math.BigDecimal;

public class Item {
    private String itemName;
    private BigDecimal price;

    public Item (String itemName, BigDecimal price) {
        this.itemName = itemName;
        this.price = price;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getName() {
        return itemName;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
