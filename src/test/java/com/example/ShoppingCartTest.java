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
        Item item1  = new Item();
        Item item2  = new Item();
        Item item3  = new Item();
        Item item4  = new Item();
        Item item5  = new Item();
        shoppingCart.add(item1);
        shoppingCart.add(List.of(item2, item3));
        shoppingCart.add(item4, item5);

        assertThat(shoppingCart.getCart()).containsExactly(item1, item2, item3, item4, item5);
    }

    @Test
    public void removeItems() {
        Item item1  = new Item();
        Item item2  = new Item();
        Item item3  = new Item();
        Item item4  = new Item();
        Item item5  = new Item();
        shoppingCart.add(item1, item2, item3, item4, item5);

        shoppingCart.remove(item1);
        assertThat(shoppingCart.getCart()).containsExactly(item2, item3, item4, item5);

        shoppingCart.remove(item2, item3);
        assertThat(shoppingCart.getCart()).containsExactly(item4, item5);

        shoppingCart.empty();
        assertThat(shoppingCart.getCart()).isEmpty();
    }
}
