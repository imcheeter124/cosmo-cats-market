package com.cosmocats.market.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cosmocats.market.common.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Order and Cart domain models")
class OrderTest {

    @Test
    void shouldBuildOrderWithItemsAndStatus() {
        Instant createdAt = Instant.parse("2026-10-20T10:00:00Z");

        Order order = Order.builder()
                .id(1L)
                .cartId(7L)
                .item(OrderItem.builder().productId(1L).quantity(2).price(new BigDecimal("12.50")).build())
                .item(OrderItem.builder().productId(2L).quantity(1).price(new BigDecimal("7.00")).build())
                .status(OrderStatus.CREATED)
                .createdAt(createdAt)
                .build();

        assertThat(order.getItems()).hasSize(2);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void shouldNotAllowModifyingOrderItemsAfterBuild() {
        Order order = Order.builder()
                .id(1L)
                .item(OrderItem.builder().productId(1L).quantity(1).price(BigDecimal.ONE).build())
                .status(OrderStatus.PAID)
                .build();

        OrderItem extraItem = OrderItem.builder().productId(2L).quantity(1).price(BigDecimal.TEN).build();

        assertThatThrownBy(() -> order.getItems().add(extraItem))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldNotAllowModifyingCartItemsAfterBuild() {
        Cart cart = Cart.builder()
                .id(7L)
                .item(CartItem.builder().productId(1L).quantity(2).build())
                .build();

        CartItem extraItem = CartItem.builder().productId(3L).quantity(5).build();

        assertThat(cart.getItems()).hasSize(1);
        assertThatThrownBy(() -> cart.getItems().add(extraItem))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
