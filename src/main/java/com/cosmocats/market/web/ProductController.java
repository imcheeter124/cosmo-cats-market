package com.cosmocats.market.web;

import com.cosmocats.market.domain.Product;
import com.cosmocats.market.dto.ProductRequest;
import com.cosmocats.market.dto.ProductResponse;
import com.cosmocats.market.service.ProductService;
import com.cosmocats.market.web.mapper.ProductMapper;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import com.cosmocats.market.dto.ProductPage;
import jakarta.validation.constraints.Max;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        Product product = productService.getById(id);
        return ResponseEntity.ok(productMapper.toResponse(product));
    }

    @GetMapping
    public ResponseEntity<ProductPage> getAll(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        List<ProductResponse> content = new ArrayList<>();
        for (Product product : productService.getPage(page, size)) {
            content.add(productMapper.toResponse(product));
        }
        return ResponseEntity.ok(new ProductPage(content, page, size, productService.count()));
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        Product created = productService.create(productMapper.toDomain(request));
        URI location = URI.create("/api/v1/products/" + created.getId());
        return ResponseEntity.created(location).body(productMapper.toResponse(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        Product updated = productService.update(id, productMapper.toDomain(request));
        return ResponseEntity.ok(productMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}