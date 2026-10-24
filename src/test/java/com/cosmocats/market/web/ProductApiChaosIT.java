package com.cosmocats.market.web;

import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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

/**
 * Requests a careless or malicious client could send. Every error must still be
 * answered in RFC 9457 format and must never leave broken data behind.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@DisplayName("Product API chaos scenarios")
class ProductApiChaosIT {

    private static final String PRODUCTS_URL = "/api/v1/products";

    @Autowired
    private MockMvc mockMvc;

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"name\": \"Galaxy Catnip\", \"price\": 3.99, ",
            "not a json at all",
            "{\"name\": \"Galaxy Catnip\", \"price\": \"cheap\", \"categoryId\": 1}",
            "{\"name\": \"Galaxy Catnip\", \"price\": 3.99, \"categoryId\": \"one\"}",
            "null"
    })
    void shouldAnswerBrokenJsonWithProblemDetail(String body) throws Exception {
        expectProblem(mockMvc.perform(post(PRODUCTS_URL).contentType(MediaType.APPLICATION_JSON).content(body)), 400);
        expectNoProductsCreated();
    }

    @Test
    void shouldAnswerMissingBodyWithProblemDetail() throws Exception {
        expectProblem(mockMvc.perform(post(PRODUCTS_URL).contentType(MediaType.APPLICATION_JSON)), 400);
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "1.5", "99999999999999999999"})
    void shouldAnswerInvalidIdWithProblemDetail(String id) throws Exception {
        expectProblem(mockMvc.perform(get(PRODUCTS_URL + "/" + id)), 400);
    }

    @Test
    void shouldAnswerNonNumericPaginationWithProblemDetail() throws Exception {
        expectProblem(mockMvc.perform(get(PRODUCTS_URL).param("page", "first")), 400);
    }

    @Test
    void shouldAnswerUnsupportedMediaTypeWithProblemDetail() throws Exception {
        expectProblem(mockMvc.perform(post(PRODUCTS_URL).contentType(MediaType.TEXT_PLAIN).content("Star Milk")), 415);
    }

    @Test
    void shouldAnswerUnsupportedMethodWithProblemDetail() throws Exception {
        expectProblem(mockMvc.perform(patch(PRODUCTS_URL + "/1").contentType(MediaType.APPLICATION_JSON).content("{}")),
                405);
    }

    @Test
    void shouldAnswerUnknownEndpointWithProblemDetail() throws Exception {
        expectProblem(mockMvc.perform(get("/api/v1/unicorns")), 404);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.001", "12.999", "100000000000.00"})
    void shouldRejectPriceThatIsNotAValidAmountOfMoney(String price) throws Exception {
        String body = "{\"name\": \"Galaxy Catnip\", \"price\": " + price + ", \"categoryId\": 1}";

        expectProblem(mockMvc.perform(post(PRODUCTS_URL).contentType(MediaType.APPLICATION_JSON).content(body)), 400);
        expectNoProductsCreated();
    }

    @Test
    void shouldRejectHugeName() throws Exception {
        String body = "{\"name\": \"" + "a".repeat(10_000) + "\", \"price\": 1.00, \"categoryId\": 1}";

        expectProblem(mockMvc.perform(post(PRODUCTS_URL).contentType(MediaType.APPLICATION_JSON).content(body)), 400);
    }

    @Test
    void shouldIgnoreIdSentByClient() throws Exception {
        String body = """
                {"id": 1, "name": "Galaxy Catnip", "price": 3.99, "categoryId": 1}
                """;

        mockMvc.perform(post(PRODUCTS_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(not(1)));

        mockMvc.perform(get(PRODUCTS_URL + "/1")).andExpect(jsonPath("$.name").value("Star Milk"));
    }

    private void expectNoProductsCreated() throws Exception {
        mockMvc.perform(get(PRODUCTS_URL)).andExpect(jsonPath("$.totalElements").value(2));
    }

    private static void expectProblem(ResultActions result, int status) throws Exception {
        result.andExpect(status().is(status))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(status))
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.instance").exists());
    }
}
