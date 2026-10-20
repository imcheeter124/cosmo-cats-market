package com.cosmocats.market.domain;

import lombok.Builder;
import lombok.Value;
import java.math.BigDecimal;

@Value
@Builder(toBuilder = true)
public class OrderItem {
    Long productId;
    int quantity;
    BigDecimal price;
}