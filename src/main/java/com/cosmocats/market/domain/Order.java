package com.cosmocats.market.domain;

import lombok.Singular;
import java.time.Instant;
import com.cosmocats.market.common.OrderStatus;
import java.util.List;
import lombok.Builder;
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
