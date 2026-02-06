package com.example;

import com.example.shop.Item;
import com.example.shop.ShoppingCart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        Item item  = new Item();
        shoppingCart.add(item);
        shoppingCart.add(List.of(item, item));
        shoppingCart.add(item, item);

        assertThat(shoppingCart.getCart()).containsExactly(item, item, item, item, item);
    }
}
