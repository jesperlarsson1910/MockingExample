package com.example.shop;

import java.math.BigDecimal;
import java.util.*;

public class ShoppingCart {
    Map<Item, Integer> items = new HashMap<>();
    List<BigDecimal> coupons = new ArrayList<>();

    public void add(Item item) {
        if(item == null) throw new IllegalArgumentException("item is null");
        if(item.getName() == null || item.getName().isBlank() || item.getPrice() == null){
            throw new IllegalArgumentException("item contains null or blank");
        }

        if (items.containsKey(item)) {
            items.put(item, items.get(item) + 1);
        }
        else {
            items.put(item, 1);
        };
    }
    public void add(Item item, int quantity) {
        if(item == null) throw new IllegalArgumentException("item is null");
        if(item.getName() == null || item.getName().isBlank() || item.getPrice() == null){
            throw new IllegalArgumentException("item contains null or blank");
        }

        if (items.containsKey(item)) {
            items.put(item, items.get(item) + quantity);
        }
        else {
            items.put(item, quantity);
        };
    }


    public void add(List<Item> items) {
        if(items == null) throw new IllegalArgumentException("items is null");
        if(items.contains(null)) throw new IllegalArgumentException("items contains null value");
        if(items.stream().anyMatch(item -> item.getName() == null || item.getName().isBlank() || item.getPrice() == null)){
            throw new IllegalArgumentException("item contains null or blank");
        }

        for (Item item : items) {
            this.add(item);
        };
    }

    public void add(Item... items) {
        if(items == null) throw new IllegalArgumentException("items is null");
        if(Arrays.stream(items).toList().contains(null)) throw new IllegalArgumentException("items contains null value");
        if(Arrays.stream(items).anyMatch(item -> item.getName() == null || item.getName().isBlank() || item.getPrice() == null)){
            throw new IllegalArgumentException("item contains null or blank");
        }

        for (Item item : items) {
            this.add(item);
        };
    }

    public Map<Item, Integer> getCart() {
        return items;
    }

    public void remove(Item item) {
        if(item == null) throw new IllegalArgumentException("item is null");

        items.remove(item);
    }

    public void remove(Item... items) {
        if(items == null) throw new IllegalArgumentException("items is null");
        if(Arrays.stream(items).toList().contains(null)) throw new IllegalArgumentException("items contains null value");

        for (Item item : items) {
            this.remove(item);
        };
    }

    public void remove(Item item, int quantity) {
        if(item == null) throw new IllegalArgumentException("item is null");

        if(items.get(item) < quantity) {
            remove(item);
        }
        else {
            items.put(item, items.get(item) - quantity);
        }
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
        if(discount == null) throw new IllegalArgumentException("discount is null");

        items.keySet().forEach(i -> i.setPrice(i.getPrice().multiply(discount)));
    }

    public void applyDiscountPercentage(Item item, BigDecimal discount) {
        if(item == null) throw new IllegalArgumentException("item is null");
        if(discount == null) throw new IllegalArgumentException("discount is null");

        items.keySet().stream().filter(i -> i.equals(item)).forEach(i -> i.setPrice(i.getPrice().multiply(discount)));
    }

    public void applyDiscountAmount(BigDecimal discount) {
        if(discount == null) throw new IllegalArgumentException("discount is null");

        items.keySet().forEach(item -> item.setPrice(item.getPrice().subtract(discount)));
    }

    public void applyDiscountAmount(Item item, BigDecimal discount) {
        if(item == null) throw new IllegalArgumentException("item is null");
        if(discount == null) throw new IllegalArgumentException("discount is null");

        items.keySet().stream().filter(i -> i.equals(item)).forEach(i -> i.setPrice(i.getPrice().subtract(discount)));
    }

    public void applyCoupon(BigDecimal amount) {
        if(amount == null) throw new IllegalArgumentException("amount is null");

        coupons.add(amount);
    }

    public BigDecimal getTotalCoupon() {
        return coupons.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}