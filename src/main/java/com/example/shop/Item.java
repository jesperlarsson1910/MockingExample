package com.example.shop;

import java.math.BigDecimal;

import java.util.Objects;

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

    `@Override`
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return Objects.equals(itemName, item.itemName);
    }

    `@Override`
    public int hashCode() {
        return Objects.hash(itemName);
    }
}
