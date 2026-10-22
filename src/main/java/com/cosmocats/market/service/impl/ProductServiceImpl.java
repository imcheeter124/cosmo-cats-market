package com.cosmocats.market.service.impl;

import com.cosmocats.market.domain.Product;
import com.cosmocats.market.service.ProductService;
import com.cosmocats.market.service.exception.ProductNotFoundException;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    private final Map<Long, Product> products = new ConcurrentSkipListMap<>();
    private final AtomicLong idGenerator = new AtomicLong();

    @PostConstruct
    void initSampleData() {
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
        log.info("Created product id={} name={}", id, saved.getName());
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
    public List<Product> getPage(int page, int size) {
        List<Product> all = new ArrayList<>(products.values());
        long from = (long) page * size;
        if (from >= all.size()) {
            return List.of();
        }
        int to = (int) Math.min(from + size, all.size());
        return all.subList((int) from, to);
    }

    @Override
    public long count() {
        return products.size();
    }

    @Override
    public Product update(Long id, Product product) {
        if (!products.containsKey(id)) {
            throw new ProductNotFoundException(id);
        }
        Product updated = product.toBuilder().id(id).build();
        products.put(id, updated);
        log.info("Updated product id={}", id);
        return updated;
    }

    @Override
    public void delete(Long id) {
        products.remove(id);
        log.info("Deleted product id={}", id);
    }
}