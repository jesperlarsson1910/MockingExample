package com.example.shop;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ShoppingCart {
    List<Item> items = new ArrayList<>();
    List<BigDecimal> coupons = new ArrayList<>();

    public void add(Item item) {
        items.add(item);
    }

    public void add(Collection<Item> items) {
        this.items.addAll(items);
    }

    public void add(Item... items) {
        this.items.addAll(List.of(items));
    }

    public List<Item> getCart() {
        return items;
    }

    public void remove(Item item) {
        items.remove(item);
    }

    public void remove(Item... items) {
        this.items.removeAll(List.of(items));
    }

    public void empty() {
        items.clear();
    }

    public BigDecimal getTotalPrice() {
        return items.stream().map(Item::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add).subtract(getTotalCoupon());
    }

    public void applyDiscountPercentage(BigDecimal discount) {
        items.forEach(item -> {item.setPrice(item.getPrice().multiply(discount));});
    }

    public void applyDiscountPercentage(Item item, BigDecimal discount) {
        items.stream().filter(i -> i.equals(item)).findFirst().ifPresent(i -> {item.setPrice(item.getPrice().multiply(discount));});
    }

    public void applyDiscountAmount(BigDecimal discount) {
        items.forEach(item -> {item.setPrice(item.getPrice().subtract(discount));});
    }

    public void applyDiscountAmount(Item item, BigDecimal discount) {
        items.stream().filter(i -> i.equals(item)).findFirst().ifPresent(i -> {item.setPrice(item.getPrice().subtract(discount));});
    }

    public void applyCoupon(BigDecimal amount) {
        coupons.add(amount);
    }

    public BigDecimal getTotalCoupon() {
        return coupons.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
