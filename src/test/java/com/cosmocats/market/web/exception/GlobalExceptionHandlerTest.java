package com.cosmocats.market.web.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldHideInternalDetailsOfUnexpectedError() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/products");

        ResponseEntity<ProblemDetail> response =
                handler.handleOther(new IllegalStateException("db password is 12345"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        ProblemDetail problem = response.getBody();
        assertThat(problem).isNotNull();
        assertThat(problem.getType()).isEqualTo(URI.create("https://cosmo-cats.market/errors/internal"));
        assertThat(problem.getTitle()).isEqualTo("Internal server error");
        assertThat(problem.getDetail()).doesNotContain("12345");
        assertThat(problem.getInstance()).isEqualTo(URI.create("/api/v1/products"));
    }
}
