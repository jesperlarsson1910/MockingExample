package com.example.shop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ShoppingCart {
    List<Item> items = new ArrayList<>();

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
}
