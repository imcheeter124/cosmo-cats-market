package com.cosmocats.market.domain;

import com.cosmocats.market.common.OrderStatus;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Order {
    Long id;
    Long cartId;
    @Singular
    List<OrderItem> items;
    OrderStatus status;
    Instant createdAt;
}
