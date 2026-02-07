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
        if(quantity <= 0) throw new IllegalArgumentException("quantity must be greater than 0");
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

    public boolean remove(Item item) {
        if(item == null) throw new IllegalArgumentException("item is null");

        if (items.containsKey(item)) {
            items.remove(item);
            return true;
        }
        else {
            System.out.println("\"" + item.getName() + "\" is not in cart");
            return false;
        }
    }

    public boolean remove(Item... items) {
        if(items == null) throw new IllegalArgumentException("items is null");
        if(Arrays.stream(items).toList().contains(null)) throw new IllegalArgumentException("items contains null value");

        for (Item item : items) {
            if(!this.remove(item)){
                return false;
            }
        };
        return true;
    }

    public boolean remove(Item item, int quantity) {
        if(item == null) throw new IllegalArgumentException("item is null");

        quantity = Math.abs(quantity);
        if (items.containsKey(item)) {
            if(items.get(item) <= quantity) {
                remove(item);
            }
            else {
                items.put(item, items.get(item) - quantity);
            }
            return  true;
        }
        else {
            System.out.println("\"" + item.getName() + "\" is not in cart");
            return false;
        }
    }

    public void empty() {
        items.clear();
    }

    public BigDecimal getTotalPrice() {
        BigDecimal total = items.entrySet().stream()
                .map(entry -> entry.getKey().getPrice()
                        .multiply(BigDecimal.valueOf(entry.getValue())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .subtract(getTotalCoupon());

        if(total.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return total;
    }

    public void applyDiscountPercentage(BigDecimal discount) {
        if(discount == null) throw new IllegalArgumentException("discount is null");
        if(discount.compareTo(BigDecimal.ZERO) <= 0 || discount.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("discount must be greater than 0 but not above 100%");
        }

        items.keySet().forEach(i -> i.setPrice(
                i.getPrice().multiply(BigDecimal.valueOf(100).subtract(discount)
                        .multiply(BigDecimal.valueOf(0.01)))));
    }

    public boolean applyDiscountPercentage(Item item, BigDecimal discount) {
        if(item == null) throw new IllegalArgumentException("item is null");
        if(discount == null) throw new IllegalArgumentException("discount is null");
        if(discount.compareTo(BigDecimal.ZERO) <= 0 || discount.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("discount must be greater than 0 but not above 100%");
        }

        if (!items.containsKey(item)) {
            System.out.println("\"" + item.getName() + "\" is not in cart");
            return false;
        }
        else {
            items.keySet().stream().filter(i -> i.equals(item)).forEach(i -> i.setPrice(
                    i.getPrice().multiply(BigDecimal.valueOf(100).subtract(discount)
                            .multiply(BigDecimal.valueOf(0.01)))));
            return true;
        }
    }

    public void applyDiscountAmount(BigDecimal discount) {
        if(discount == null) throw new IllegalArgumentException("discount is null");
        if(discount.compareTo(BigDecimal.valueOf(0)) <= 0) throw new IllegalArgumentException("discount must be greater than 0");

        items.keySet().forEach(item -> {if(item.getPrice().compareTo(discount) <= 0) {
            item.setPrice(BigDecimal.ZERO);
        }else {
            item.setPrice(item.getPrice().subtract(discount));
        }});
    }

    public boolean applyDiscountAmount(Item item, BigDecimal discount) {
        if(item == null) throw new IllegalArgumentException("item is null");
        if(discount == null) throw new IllegalArgumentException("discount is null");
        if(discount.compareTo(BigDecimal.valueOf(0)) <= 0) throw new IllegalArgumentException("discount must be greater than 0");

        if (!items.containsKey(item)) {
            System.out.println("\"" + item.getName() + "\" is not in cart");
            return false;
        }
        else {
            items.keySet().stream().filter(i -> i.equals(item))
                    .forEach(i -> {if(i.getPrice().compareTo(discount) <= 0) {
                        i.setPrice(BigDecimal.ZERO);
                    }else {
                        i.setPrice(i.getPrice().subtract(discount));
                    }});
            return true;
        }
    }

    public void applyCoupon(BigDecimal amount) {
        if(amount == null) throw new IllegalArgumentException("amount is null");
        if(amount.compareTo(BigDecimal.valueOf(0)) <= 0) throw new IllegalArgumentException("coupon must be greater than 0");

        coupons.add(amount);
    }

    public BigDecimal getTotalCoupon() {
        return coupons.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}