package com.example;

import com.example.shop.Item;
import com.example.shop.ShoppingCart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ShoppingCartTest {

    ShoppingCart shoppingCart;

    @BeforeEach
    public void beforeEach() {
        shoppingCart = new ShoppingCart();
    }

    @Test
    public void addItems() {
        Item item1  = new Item(BigDecimal.valueOf(101));
        Item item2  = new Item(BigDecimal.valueOf(102));
        Item item3  = new Item(BigDecimal.valueOf(103));
        Item item4  = new Item(BigDecimal.valueOf(104));
        Item item5  = new Item(BigDecimal.valueOf(105));
        shoppingCart.add(item1);
        shoppingCart.add(List.of(item2, item3));
        shoppingCart.add(item4, item5);

        assertThat(shoppingCart.getCart()).containsExactly(item1, item2, item3, item4, item5);
    }

    @Test
    public void removeItems() {
        Item item1  = new Item(BigDecimal.valueOf(101));
        Item item2  = new Item(BigDecimal.valueOf(102));
        Item item3  = new Item(BigDecimal.valueOf(103));
        Item item4  = new Item(BigDecimal.valueOf(104));
        Item item5  = new Item(BigDecimal.valueOf(105));
        shoppingCart.add(item1, item2, item3, item4, item5);

        shoppingCart.remove(item1);
        assertThat(shoppingCart.getCart()).containsExactly(item2, item3, item4, item5);

        shoppingCart.remove(item2, item3);
        assertThat(shoppingCart.getCart()).containsExactly(item4, item5);

        shoppingCart.empty();
        assertThat(shoppingCart.getCart()).isEmpty();
    }

    @Test
    public void calculateTotalPrice() {
        Item item1  = new Item(BigDecimal.valueOf(101));
        Item item2  = new Item(BigDecimal.valueOf(102));
        Item item3  = new Item(BigDecimal.valueOf(103));
        Item item4  = new Item(BigDecimal.valueOf(104));
        Item item5  = new Item(BigDecimal.valueOf(105));
        shoppingCart.add(item1, item2, item3, item4, item5);

        assertThat(shoppingCart.getTotalPrice().compareTo(BigDecimal.valueOf(101+102+103+104+105))).isZero();
    }
}
