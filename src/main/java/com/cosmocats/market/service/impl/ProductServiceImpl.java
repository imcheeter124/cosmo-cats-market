package com.cosmocats.market.service.impl;

import com.cosmocats.market.domain.Product;
import com.cosmocats.market.service.ProductService;
import com.cosmocats.market.service.exception.ProductNotFoundException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {

    private final Map<Long, Product> products = new ConcurrentSkipListMap<>();
    private final AtomicLong idGenerator = new AtomicLong();

    public ProductServiceImpl() {
        create(Product.builder()
                .name("Star Milk")
                .description("Fresh milk from the Milky Way")
                .price(new BigDecimal("12.50"))
                .categoryId(1L)
                .build());
        create(Product.builder()
                .name("Comet Yarn Ball")
                .description("Anti-gravity yarn for space cats")
                .price(new BigDecimal("7.00"))
                .categoryId(2L)
                .build());
    }

    @Override
    public Product create(Product product) {
        Long id = idGenerator.incrementAndGet();
        Product saved = product.toBuilder().id(id).build();
        products.put(id, saved);
        return saved;
    }

    @Override
    public Product getById(Long id) {
        Product product = products.get(id);
        if (product == null) {
            throw new ProductNotFoundException(id);
        }
        return product;
    }

    @Override
    public List<Product> getAll() {
        return new ArrayList<>(products.values());
    }

    @Override
    public Product update(Long id, Product product) {
        if (!products.containsKey(id)) {
            throw new ProductNotFoundException(id);
        }
        Product updated = product.toBuilder().id(id).build();
        products.put(id, updated);
        return updated;
    }

    @Override
    public void delete(Long id) {
        products.remove(id);
    }
}