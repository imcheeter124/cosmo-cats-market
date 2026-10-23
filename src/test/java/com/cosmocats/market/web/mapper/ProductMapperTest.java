package com.cosmocats.market.web.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.cosmocats.market.domain.Product;
import com.cosmocats.market.dto.ProductRequest;
import com.cosmocats.market.dto.ProductResponse;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ProductMapper")
class ProductMapperTest {

    private final ProductMapper productMapper = new ProductMapper();

    @Test
    void shouldMapRequestToDomainWithoutId() {
        ProductRequest request = new ProductRequest("Star Milk", "Fresh milk", new BigDecimal("12.50"), 1L);

        Product product = productMapper.toDomain(request);

        assertThat(product.getId()).isNull();
        assertThat(product.getName()).isEqualTo("Star Milk");
        assertThat(product.getDescription()).isEqualTo("Fresh milk");
        assertThat(product.getPrice()).isEqualByComparingTo("12.50");
        assertThat(product.getCategoryId()).isEqualTo(1L);
    }

    @Test
    void shouldMapDomainToResponse() {
        Product product = Product.builder()
                .id(7L)
                .name("Star Milk")
                .description("Fresh milk")
                .price(new BigDecimal("12.50"))
                .categoryId(1L)
                .build();

        ProductResponse response = productMapper.toResponse(product);

        assertThat(response).isEqualTo(new ProductResponse(7L, "Star Milk", "Fresh milk", new BigDecimal("12.50"), 1L));
    }
}
