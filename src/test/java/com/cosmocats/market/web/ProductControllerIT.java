package com.cosmocats.market.web;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@DisplayName("Product API")
class ProductControllerIT {

    private static final String PRODUCTS_URL = "/api/v1/products";
    private static final String VALIDATION_TYPE = "https://cosmo-cats.market/errors/validation";
    private static final String NOT_FOUND_TYPE = "https://cosmo-cats.market/errors/not-found";

    private static final String VALID_PRODUCT = """
            {
              "name": "Galaxy Catnip",
              "description": "Premium catnip from Andromeda",
              "price": 3.99,
              "categoryId": 1
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnProductById() throws Exception {
        mockMvc.perform(get(PRODUCTS_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Star Milk"))
                .andExpect(jsonPath("$.price").value(12.50))
                .andExpect(jsonPath("$.categoryId").value(1));
    }

    @Test
    void shouldReturnNotFoundProblemForUnknownProduct() throws Exception {
        expectProblem(mockMvc.perform(get(PRODUCTS_URL + "/999")), 404, NOT_FOUND_TYPE)
                .andExpect(jsonPath("$.title").value("Product not found"))
                .andExpect(jsonPath("$.detail").value("Product with id 999 not found"))
                .andExpect(jsonPath("$.instance").value(PRODUCTS_URL + "/999"));
    }

    @Test
    void shouldReturnFirstPageWithDefaults() throws Exception {
        mockMvc.perform(get(PRODUCTS_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void shouldReturnRequestedPage() throws Exception {
        mockMvc.perform(get(PRODUCTS_URL).param("page", "1").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Comet Yarn Ball"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void shouldReturnEmptyPageWhenPageIsOutOfRange() throws Exception {
        mockMvc.perform(get(PRODUCTS_URL).param("page", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @ParameterizedTest
    @ValueSource(strings = {"page=-1", "size=0", "size=101"})
    void shouldRejectInvalidPagination(String query) throws Exception {
        expectProblem(mockMvc.perform(get(PRODUCTS_URL + "?" + query)), 400, VALIDATION_TYPE);
    }

    @Test
    void shouldCreateProduct() throws Exception {
        mockMvc.perform(post(PRODUCTS_URL).contentType(MediaType.APPLICATION_JSON).content(VALID_PRODUCT))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", PRODUCTS_URL + "/3"))
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("Galaxy Catnip"));

        mockMvc.perform(get(PRODUCTS_URL + "/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Galaxy Catnip"));
    }

    @Test
    void shouldCreateProductWithoutOptionalDescription() throws Exception {
        String body = """
                {"name": "Galaxy Catnip", "price": 3.99, "categoryId": 1}
                """;

        mockMvc.perform(post(PRODUCTS_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").doesNotExist());
    }

    @Test
    void shouldReturnAllValidationErrors() throws Exception {
        String body = """
                {"name": "", "price": -5}
                """;

        expectProblem(mockMvc.perform(post(PRODUCTS_URL).contentType(MediaType.APPLICATION_JSON).content(body)),
                400, VALIDATION_TYPE)
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors", hasItem("Field 'price' must be greater than 0")))
                .andExpect(jsonPath("$.errors", hasItem("Field 'categoryId' must not be null")))
                .andExpect(jsonPath("$.errors", hasItem("Field 'name' must not be blank")));

        mockMvc.perform(get(PRODUCTS_URL)).andExpect(jsonPath("$.totalElements").value(2));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"name\": \"ab\", \"price\": 1.00, \"categoryId\": 1}",
            "{\"name\": \"   \", \"price\": 1.00, \"categoryId\": 1}",
            "{\"name\": \"Galaxy Catnip\", \"price\": 0, \"categoryId\": 1}",
            "{\"name\": \"Galaxy Catnip\", \"categoryId\": 1}"
    })
    void shouldRejectInvalidProduct(String body) throws Exception {
        expectProblem(mockMvc.perform(post(PRODUCTS_URL).contentType(MediaType.APPLICATION_JSON).content(body)),
                400, VALIDATION_TYPE);
    }

    @Test
    void shouldRejectUnknownCategory() throws Exception {
        String body = """
                {"name": "Ghost Milk", "price": 1.00, "categoryId": 999}
                """;

        expectProblem(mockMvc.perform(post(PRODUCTS_URL).contentType(MediaType.APPLICATION_JSON).content(body)),
                400, VALIDATION_TYPE)
                .andExpect(jsonPath("$.detail").value("Category with id 999 does not exist"));
    }

    @Test
    void shouldUpdateProduct() throws Exception {
        String body = """
                {"name": "Star Milk Deluxe", "description": "Even fresher", "price": 15.00, "categoryId": 2}
                """;

        mockMvc.perform(put(PRODUCTS_URL + "/1").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Star Milk Deluxe"))
                .andExpect(jsonPath("$.categoryId").value(2));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingUnknownProduct() throws Exception {
        expectProblem(mockMvc.perform(put(PRODUCTS_URL + "/999").contentType(MediaType.APPLICATION_JSON)
                .content(VALID_PRODUCT)), 404, NOT_FOUND_TYPE);
    }

    @Test
    void shouldRejectInvalidUpdate() throws Exception {
        String body = """
                {"name": "", "price": 1.00, "categoryId": 1}
                """;

        expectProblem(mockMvc.perform(put(PRODUCTS_URL + "/1").contentType(MediaType.APPLICATION_JSON).content(body)),
                400, VALIDATION_TYPE);

        mockMvc.perform(get(PRODUCTS_URL + "/1")).andExpect(jsonPath("$.name").value("Star Milk"));
    }

    @Test
    void shouldDeleteProductIdempotently() throws Exception {
        mockMvc.perform(delete(PRODUCTS_URL + "/1")).andExpect(status().isNoContent());
        mockMvc.perform(delete(PRODUCTS_URL + "/1")).andExpect(status().isNoContent());

        mockMvc.perform(get(PRODUCTS_URL + "/1")).andExpect(status().isNotFound());
    }

    private static ResultActions expectProblem(ResultActions result, int status, String type) throws Exception {
        return result
                .andExpect(status().is(status))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value(type))
                .andExpect(jsonPath("$.status").value(status));
    }
}
