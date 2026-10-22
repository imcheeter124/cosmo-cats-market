package com.cosmocats.market.domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class CartItem {
    Long productId;
    int quantity;
}