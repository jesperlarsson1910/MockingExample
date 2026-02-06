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

    Item item1;
    Item item2;
    Item item3;
    Item item4;
    Item item5;

    @BeforeEach
    public void beforeEach() {
        shoppingCart = new ShoppingCart();

        item1  = new Item(BigDecimal.valueOf(101));
        item2  = new Item(BigDecimal.valueOf(102));
        item3  = new Item(BigDecimal.valueOf(103));
        item4  = new Item(BigDecimal.valueOf(104));
        item5  = new Item(BigDecimal.valueOf(105));
    }

    @Test
    public void addItems() {
        shoppingCart.add(item1);
        shoppingCart.add(List.of(item2, item3));
        shoppingCart.add(item4, item5);

        assertThat(shoppingCart.getCart()).containsExactly(item1, item2, item3, item4, item5);
    }

    @Test
    public void removeItems() {
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
        shoppingCart.add(item1, item2, item3, item4, item5);

        assertThat(shoppingCart.getTotalPrice()
                .compareTo(BigDecimal.valueOf(101+102+103+104+105)))
                .isZero();
    }

    @Test
    public void applyDiscountPercentageToEntireShoppingCart() {
        shoppingCart.add(item1, item2, item3, item4, item5);
        shoppingCart.applyDiscountPercentage(BigDecimal.valueOf(0.1));

        assertThat(shoppingCart.getTotalPrice()
                .compareTo(BigDecimal.valueOf(101+102+103+104+105).multiply(BigDecimal.valueOf(0.1))))
                .isZero();
    }

    @Test
    public void applyDiscountPercentageToItem() {
        shoppingCart.add(item1, item2, item3, item4, item5);
        shoppingCart.applyDiscountPercentage(item1, BigDecimal.valueOf(0.1));

        assertThat(shoppingCart.getTotalPrice()
                .compareTo((BigDecimal.valueOf(101).multiply(BigDecimal.valueOf(0.1))
                        .add(BigDecimal.valueOf(102+103+104+105)))))
                .isZero();
    }

    @Test
    public void applyDiscountAmountToShoppingCart() {
        shoppingCart.add(item1, item2, item3, item4, item5);
        shoppingCart.applyDiscountAmount(BigDecimal.valueOf(10));

        assertThat(shoppingCart.getTotalPrice()
                .compareTo(BigDecimal.valueOf(101+102+103+104+105).subtract(BigDecimal.valueOf(10*5))))
                .isZero();
    }

    @Test
    public void applyDiscountAmountToItem() {
        shoppingCart.add(item1, item2, item3, item4, item5);
        shoppingCart.applyDiscountAmount(item1, BigDecimal.valueOf(10));

        assertThat(shoppingCart.getTotalPrice()
                .compareTo(BigDecimal.valueOf(101+102+103+104+105).subtract(BigDecimal.valueOf(10))))
                .isZero();
    }

    @Test
    public void applyCoupon() {
        shoppingCart.add(item1, item2, item3, item4, item5);
        shoppingCart.applyCoupon(BigDecimal.valueOf(200));

        assertThat(shoppingCart.getTotalPrice()
                .compareTo(BigDecimal.valueOf(101+102+103+104+105).subtract(BigDecimal.valueOf(200))))
                .isZero();
    }
}
