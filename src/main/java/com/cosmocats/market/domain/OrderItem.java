package com.cosmocats.market.domain;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class OrderItem {
    Long productId;
    int quantity;
    BigDecimal price;
}