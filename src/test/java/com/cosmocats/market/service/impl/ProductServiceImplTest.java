package com.cosmocats.market.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cosmocats.market.domain.Product;
import com.cosmocats.market.service.exception.CategoryNotFoundException;
import com.cosmocats.market.service.exception.ProductNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("ProductServiceImpl")
class ProductServiceImplTest {

    private ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        productService = new ProductServiceImpl();
    }

    @Test
    void shouldAssignSequentialIdsOnCreate() {
        Product first = productService.create(buildProduct("Star Milk", 1L));
        Product second = productService.create(buildProduct("Comet Yarn Ball", 2L));

        assertThat(first.getId()).isEqualTo(1L);
        assertThat(second.getId()).isEqualTo(2L);
    }

    @Test
    void shouldKeepAllFieldsOnCreate() {
        Product created = productService.create(buildProduct("Star Milk", 1L));

        assertThat(created.getName()).isEqualTo("Star Milk");
        assertThat(created.getDescription()).isEqualTo("Description of Star Milk");
        assertThat(created.getPrice()).isEqualByComparingTo("12.50");
        assertThat(created.getCategoryId()).isEqualTo(1L);
    }

    @Test
    void shouldThrowCategoryNotFoundWhenCreatingWithUnknownCategory() {
        assertThatThrownBy(() -> productService.create(buildProduct("Ghost Milk", 999L)))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessage("Category with id 999 does not exist");
        assertThat(productService.count()).isZero();
    }

    @Test
    void shouldReturnProductById() {
        Product created = productService.create(buildProduct("Star Milk", 1L));

        assertThat(productService.getById(created.getId())).isEqualTo(created);
    }

    @Test
    void shouldThrowProductNotFoundForUnknownId() {
        assertThatThrownBy(() -> productService.getById(42L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Product with id 42 not found");
    }

    @Test
    void shouldLoadSampleDataOnInit() {
        productService.initSampleData();

        assertThat(productService.count()).isEqualTo(2);
        assertThat(productService.getById(1L).getName()).isEqualTo("Star Milk");
        assertThat(productService.getById(2L).getName()).isEqualTo("Comet Yarn Ball");
    }

    @Test
    void shouldReturnFirstPage() {
        createProducts(5);

        List<Product> page = productService.getPage(0, 2);

        assertThat(page).extracting(Product::getId).containsExactly(1L, 2L);
    }

    @Test
    void shouldReturnLastIncompletePage() {
        createProducts(5);

        List<Product> page = productService.getPage(2, 2);

        assertThat(page).extracting(Product::getId).containsExactly(5L);
    }

    @Test
    void shouldReturnEmptyPageWhenPageIsOutOfRange() {
        createProducts(3);

        assertThat(productService.getPage(10, 20)).isEmpty();
    }

    @Test
    void shouldNotOverflowOnHugePageNumber() {
        createProducts(3);

        assertThat(productService.getPage(Integer.MAX_VALUE, 100)).isEmpty();
    }

    @Test
    void shouldCountProducts() {
        createProducts(3);

        assertThat(productService.count()).isEqualTo(3);
    }

    @Test
    void shouldUpdateExistingProduct() {
        Product created = productService.create(buildProduct("Star Milk", 1L));

        Product updated = productService.update(created.getId(), buildProduct("Star Milk Deluxe", 2L));

        assertThat(updated.getId()).isEqualTo(created.getId());
        assertThat(updated.getName()).isEqualTo("Star Milk Deluxe");
        assertThat(productService.getById(created.getId())).isEqualTo(updated);
    }

    @Test
    void shouldThrowProductNotFoundWhenUpdatingUnknownProduct() {
        assertThatThrownBy(() -> productService.update(42L, buildProduct("Star Milk", 1L)))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void shouldThrowCategoryNotFoundWhenUpdatingWithUnknownCategory() {
        Product created = productService.create(buildProduct("Star Milk", 1L));

        assertThatThrownBy(() -> productService.update(created.getId(), buildProduct("Star Milk", 999L)))
                .isInstanceOf(CategoryNotFoundException.class);
        assertThat(productService.getById(created.getId()).getCategoryId()).isEqualTo(1L);
    }

    @Test
    void shouldDeleteProduct() {
        Product created = productService.create(buildProduct("Star Milk", 1L));

        productService.delete(created.getId());

        assertThat(productService.count()).isZero();
        assertThatThrownBy(() -> productService.getById(created.getId()))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 42L})
    void shouldNotFailWhenDeletingTwiceOrUnknownProduct(Long id) {
        productService.create(buildProduct("Star Milk", 1L));

        productService.delete(id);
        productService.delete(id);

        assertThat(productService.count()).isEqualTo(id == 1L ? 0 : 1);
    }

    private void createProducts(int amount) {
        for (int i = 1; i <= amount; i++) {
            productService.create(buildProduct("Product " + i, 1L));
        }
    }

    private static Product buildProduct(String name, Long categoryId) {
        return Product.builder()
                .name(name)
                .description("Description of " + name)
                .price(new BigDecimal("12.50"))
                .categoryId(categoryId)
                .build();
    }
}
