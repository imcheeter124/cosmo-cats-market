package com.cosmocats.market.domain;

import java.util.List;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Cart {
    Long id;
    @Singular
    List<CartItem> items;
}