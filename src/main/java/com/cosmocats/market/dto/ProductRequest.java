package com.cosmocats.market.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank
        @Size(min = 3, max = 100)
        String name,

        @Size(max = 1000)
        String description,

        @NotNull
        @Positive
        @Digits(integer = 10, fraction = 2)
        BigDecimal price,

        @NotNull
        Long categoryId
) {
}