package com.example.shop;

import java.math.BigDecimal;
import java.util.*;

public class ShoppingCart {
    Map<Item, Integer> items = new HashMap<>();
    List<BigDecimal> coupons = new ArrayList<>();

    public void add(Item item) {
        if (items.containsKey(item)) {
            items.put(item, items.get(item) + 1);
        }
        else {
            items.put(item, 1);
        };
    }
    public void add(Item item, int quantity) {
        if (items.containsKey(item)) {
            items.put(item, items.get(item) + quantity);
        }
        else {
            items.put(item, quantity);
        };
    }


    public void add(Collection<Item> items) {
        for (Item item : items) {
            this.add(item);
        };
    }

    public void add(Item... items) {
        for (Item item : items) {
            this.add(item);
        };
    }

    public Map<Item, Integer> getCart() {
        return items;
    }

    public void remove(Item item) {
        items.remove(item);
    }

    public void remove(Item... items) {
        for (Item item : items) {
            this.remove(item);
        };
    }

    public void remove(Item item, int quantity) {
        items.put(item, items.get(item) - quantity);
    }

    public void empty() {
        items.clear();
    }

    public BigDecimal getTotalPrice() {
        return items.entrySet().stream()
                .map(entry -> entry.getKey().getPrice()
                        .multiply(BigDecimal.valueOf(entry.getValue())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .subtract(getTotalCoupon());
    }

    public void applyDiscountPercentage(BigDecimal discount) {
        items.keySet().forEach(i -> i.setPrice(i.getPrice().multiply(discount)));
    }

    public void applyDiscountPercentage(Item item, BigDecimal discount) {
        items.keySet().stream().filter(i -> i.equals(item)).forEach(i -> i.setPrice(i.getPrice().multiply(discount)));
    }

    public void applyDiscountAmount(BigDecimal discount) {
        items.keySet().forEach(item -> item.setPrice(item.getPrice().subtract(discount)));
    }

    public void applyDiscountAmount(Item item, BigDecimal discount) {
        items.keySet().stream().filter(i -> i.equals(item)).forEach(i -> i.setPrice(i.getPrice().subtract(discount)));
    }

    public void applyCoupon(BigDecimal amount) {
        coupons.add(amount);
    }

    public BigDecimal getTotalCoupon() {
        return coupons.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
