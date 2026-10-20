package com.cosmocats.market.service;

import com.cosmocats.market.domain.Product;
import java.util.List;

public interface ProductService {

    Product create(Product product);

    Product getById(Long id);

    List<Product> getAll();

    Product update(Long id, Product product);

    void delete(Long id);
}