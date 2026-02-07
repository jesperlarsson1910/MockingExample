package com.example;

import com.example.shop.Item;
import com.example.shop.ShoppingCart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

        item1  = new Item("Apple", BigDecimal.valueOf(101));
        item2  = new Item("Banana", BigDecimal.valueOf(102));
        item3  = new Item("Car", BigDecimal.valueOf(103));
        item4  = new Item("Dog", BigDecimal.valueOf(104));
        item5  = new Item("E-book", BigDecimal.valueOf(105));
        item6  = new Item("Flute", BigDecimal.valueOf(106));
    }

    static private Stream<Arguments> nullOrBlankItemFieldProvider (){
        return Stream.of(
                Arguments.of(new Item(null, BigDecimal.valueOf(404))),
                Arguments.of(new Item("", BigDecimal.valueOf(404))),
                Arguments.of(new Item(null, BigDecimal.valueOf(404)))
        );
    }

    @Test
    public void addItems() {
        shoppingCart.add(item1);
        shoppingCart.add(item2, 2);
        shoppingCart.add(new ArrayList<>(List.of(item3, item4)));
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


        assertThat(shoppingCart.remove(item1)).isTrue();
        assertThat(shoppingCart.getCart())
                .containsEntry(item2, 2)
                .containsEntry(item3, 1)
                .containsEntry(item4, 1)
                .containsEntry(item5, 1)
                .containsEntry(item6, 1);

        assertThat(shoppingCart.remove(item2, 2)).isTrue();
        assertThat(shoppingCart.getCart())
                .containsEntry(item3, 1)
                .containsEntry(item4, 1)
                .containsEntry(item5, 1)
                .containsEntry(item6, 1);

        assertThat(shoppingCart.remove(item3, item4)).isTrue();
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
        shoppingCart.applyDiscountPercentage(BigDecimal.valueOf(10));

        assertThat(shoppingCart.getTotalPrice()
                .compareTo(BigDecimal.valueOf(101+102+103+104+105+106)
                        .multiply(BigDecimal.valueOf(0.1))))
                .isZero();
    }

    @Test
    public void applyDiscountPercentageToItem() {
        shoppingCart.add(item1, item2, item3, item4, item5, item6);
        shoppingCart.applyDiscountPercentage(item1, BigDecimal.valueOf(10));

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

    @Test
    public void addNullItemsShouldThrowException() {
        assertThatThrownBy(() -> shoppingCart.add((Item) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("item is null");

        assertThatThrownBy(() -> shoppingCart.add(null, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("item is null");

        assertThatThrownBy(() -> shoppingCart.add((List<Item>) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("items is null");

        List<Item> itemsWithNull = new ArrayList<>();
        itemsWithNull.add(item1);
        itemsWithNull.add(null);

        assertThatThrownBy(() -> shoppingCart.add(itemsWithNull))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("items contains null value");

        assertThatThrownBy(() -> shoppingCart.add(item1, item2, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("items contains null value");
    }

    @ParameterizedTest
    @MethodSource("nullOrBlankItemFieldProvider")
    public void nullOrBlankFieldsShouldThrowException(Item item) {
        assertThatThrownBy(() -> shoppingCart.add(item))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("item contains null or blank");

        assertThatThrownBy(() -> shoppingCart.add(item, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("item contains null or blank");

        List<Item> itemsWithNullOrBlank = new ArrayList<>();
        itemsWithNullOrBlank.add(item1);
        itemsWithNullOrBlank.add(item);

        assertThatThrownBy(() -> shoppingCart.add(itemsWithNullOrBlank))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("item contains null or blank");

        assertThatThrownBy(() -> shoppingCart.add(item1, item))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("item contains null or blank");
    }

    @Test
    public void removeNullItemsShouldThrowException() {
        assertThatThrownBy(() -> shoppingCart.remove((Item) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("item is null");

        assertThatThrownBy(() -> shoppingCart.remove(((Item[]) null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("items is null");


        assertThatThrownBy(() -> shoppingCart.remove(item1, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("items contains null value");

        assertThatThrownBy(() -> shoppingCart.remove(null, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("item is null");
    }

    @Test
    public void cannotRemoveItemsNotInCart() {
        assertThat(shoppingCart.remove(item1)).isFalse();
        assertThat(shoppingCart.remove(item2, item3)).isFalse();
        assertThat(shoppingCart.remove(item4, 4)).isFalse();
    }

    @Test
    public void cannotDiscountItemsNotInCart() {
        assertThat(shoppingCart.applyDiscountPercentage(item1, BigDecimal.TEN)).isFalse();
        assertThat(shoppingCart.applyDiscountAmount(item2, BigDecimal.TEN)).isFalse();
    }

    @Test
    public void discountLessThan0MoreThan100PercentThrowsException() {
        shoppingCart.add(item1);

        assertThatThrownBy(() -> shoppingCart.applyDiscountPercentage(BigDecimal.valueOf(101)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("discount must be greater than 0 but not above 100%");

        assertThatThrownBy(() -> shoppingCart.applyDiscountPercentage(BigDecimal.valueOf(-101)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("discount must be greater than 0 but not above 100%");

        assertThatThrownBy(() -> shoppingCart.applyDiscountPercentage(item1, BigDecimal.valueOf(101)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("discount must be greater than 0 but not above 100%");

        assertThatThrownBy(() -> shoppingCart.applyDiscountPercentage(item1, BigDecimal.valueOf(-101)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("discount must be greater than 0 but not above 100%");
    }

    @Test
    public void negativeDiscountAmountThrowsException() {
        shoppingCart.add(item1);

        assertThatThrownBy(() -> shoppingCart.applyDiscountAmount(BigDecimal.valueOf(-1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("discount must be greater than 0");

        assertThatThrownBy(() -> shoppingCart.applyDiscountAmount(item1, BigDecimal.valueOf(-1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("discount must be greater than 0");
    }

    @Test
    public void itemAndCartCannotHaveNegativePrice(){
        shoppingCart.add(item1, item2, item3, item4, item5, item6);

        shoppingCart.applyDiscountAmount(item1, item1.getPrice().add(BigDecimal.valueOf(1)));
        assertThat(item1.getPrice().compareTo(BigDecimal.ZERO) == 0).isTrue();

        shoppingCart.applyDiscountAmount(shoppingCart.getTotalPrice().add(BigDecimal.valueOf(1)));
        assertThat(shoppingCart.getTotalPrice().compareTo(BigDecimal.ZERO) == 0).isTrue();

        shoppingCart.applyCoupon(shoppingCart.getTotalPrice().add(BigDecimal.valueOf(1000)));
        assertThat(shoppingCart.getTotalPrice().compareTo(BigDecimal.ZERO) == 0).isTrue();
    }
}