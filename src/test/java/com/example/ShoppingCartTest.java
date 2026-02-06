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
    Item item6;

    @BeforeEach
    public void beforeEach() {
        shoppingCart = new ShoppingCart();

        item1  = new Item(BigDecimal.valueOf(101));
        item2  = new Item(BigDecimal.valueOf(102));
        item3  = new Item(BigDecimal.valueOf(103));
        item4  = new Item(BigDecimal.valueOf(104));
        item5  = new Item(BigDecimal.valueOf(105));
        item6  = new Item(BigDecimal.valueOf(106));
    }

    @Test
    public void addItems() {
        shoppingCart.add(item1);
        shoppingCart.add(item2, 2);
        shoppingCart.add(List.of(item3, item4));
        shoppingCart.add(item5, item6);

        assertThat(shoppingCart.getCart())
                .containsEntry(item1, 1)
                .containsEntry(item2, 2)
                .containsEntry(item3, 1)
                .containsEntry(item4, 1)
                .containsEntry(item5, 1)
                .containsEntry(item6, 1);

    }

    @Test
    public void removeItems() {
        shoppingCart.add(item1, item2, item2, item3, item4, item5, item6);


        shoppingCart.remove(item1);
        assertThat(shoppingCart.getCart())
                .containsEntry(item2, 2)
                .containsEntry(item3, 1)
                .containsEntry(item4, 1)
                .containsEntry(item5, 1)
                .containsEntry(item6, 1);

        shoppingCart.remove(item2, 2);
        assertThat(shoppingCart.getCart())
                .containsEntry(item3, 1)
                .containsEntry(item4, 1)
                .containsEntry(item5, 1)
                .containsEntry(item6, 1);

        shoppingCart.remove(item3, item4);
        assertThat(shoppingCart.getCart())
                .containsEntry(item5, 1)
                .containsEntry(item6, 1);

        shoppingCart.empty();
        assertThat(shoppingCart.getCart()).isEmpty();
    }



    @Test
    public void calculateTotalPrice() {
        shoppingCart.add(item1, item2, item3, item4, item5, item6);

        assertThat(shoppingCart.getTotalPrice()
                .compareTo(BigDecimal.valueOf(101+102+103+104+105+106)))
                .isZero();
    }

    @Test
    public void applyDiscountPercentageToEntireShoppingCart() {
        shoppingCart.add(item1, item2, item3, item4, item5, item6);
        shoppingCart.applyDiscountPercentage(BigDecimal.valueOf(0.1));

        assertThat(shoppingCart.getTotalPrice()
                .compareTo(BigDecimal.valueOf(101+102+103+104+105+106)
                        .multiply(BigDecimal.valueOf(0.1))))
                .isZero();
    }

    @Test
    public void applyDiscountPercentageToItem() {
        shoppingCart.add(item1, item2, item3, item4, item5, item6);
        shoppingCart.applyDiscountPercentage(item1, BigDecimal.valueOf(0.1));

        assertThat(shoppingCart.getTotalPrice()
                .compareTo((BigDecimal.valueOf(101).multiply(BigDecimal.valueOf(0.1))
                        .add(BigDecimal.valueOf(102+103+104+105+106)))))
                .isZero();
    }

    @Test
    public void applyDiscountAmountToShoppingCart() {
        shoppingCart.add(item1, item2, item3, item4, item5, item6);
        shoppingCart.applyDiscountAmount(BigDecimal.valueOf(10));

        assertThat(shoppingCart.getTotalPrice()
                .compareTo(BigDecimal.valueOf(101+102+103+104+105+106)
                        .subtract(BigDecimal.valueOf(10*6))))
                .isZero();
    }

    @Test
    public void applyDiscountAmountToItem() {
        shoppingCart.add(item1, item2, item3, item4, item5, item6);
        shoppingCart.applyDiscountAmount(item1, BigDecimal.valueOf(10));

        assertThat(shoppingCart.getTotalPrice()
                .compareTo(BigDecimal.valueOf(101+102+103+104+105+106)
                        .subtract(BigDecimal.valueOf(10))))
                .isZero();
    }

    @Test
    public void applyCoupon() {
        shoppingCart.add(item1, item2, item3, item4, item5, item6);
        shoppingCart.applyCoupon(BigDecimal.valueOf(200));

        assertThat(shoppingCart.getTotalPrice()
                .compareTo(BigDecimal.valueOf(101+102+103+104+105+106)
                        .subtract(BigDecimal.valueOf(200))))
                .isZero();
    }

    @Test
    public void changeItemQuantity() {
        shoppingCart.add(item1, item2, item3, item4, item5, item6);
        shoppingCart.add(item1);
        shoppingCart.add(item2, 2);

        assertThat(shoppingCart.getCart())
                .containsEntry(item1, 2)
                .containsEntry(item2, 3)
                .containsEntry(item3, 1)
                .containsEntry(item4, 1)
                .containsEntry(item5, 1)
                .containsEntry(item6, 1);
    }
}
