package com.cosmocats.market.web.mapper;

import com.cosmocats.market.domain.Product;
import com.cosmocats.market.dto.ProductRequest;
import com.cosmocats.market.dto.ProductResponse;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toDomain(ProductRequest request) {
        return Product.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .categoryId(request.categoryId())
                .build();
    }

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategoryId()
        );
    }
}