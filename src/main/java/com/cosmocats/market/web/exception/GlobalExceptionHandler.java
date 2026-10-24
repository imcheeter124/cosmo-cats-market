package com.cosmocats.market.web.exception;

import com.cosmocats.market.service.exception.CategoryNotFoundException;
import com.cosmocats.market.service.exception.ProductNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String NOT_FOUND_TYPE = "https://cosmo-cats.market/errors/not-found";
    private static final String VALIDATION_TYPE = "https://cosmo-cats.market/errors/validation";
    private static final String MALFORMED_REQUEST_TYPE = "https://cosmo-cats.market/errors/malformed-request";
    private static final String INTERNAL_ERROR_TYPE = "https://cosmo-cats.market/errors/internal";

    @ExceptionHandler(ProductNotFoundException.class)
    public ProblemDetail handleProductNotFound(ProductNotFoundException ex, HttpServletRequest request) {
        return buildProblem(HttpStatus.NOT_FOUND, NOT_FOUND_TYPE, "Product not found", ex.getMessage(), request);
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ProblemDetail handleCategoryNotFound(CategoryNotFoundException ex, HttpServletRequest request) {
        return buildProblem(HttpStatus.BAD_REQUEST, VALIDATION_TYPE, "Validation failed", ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> errors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add("Field '" + error.getField() + "' " + error.getDefaultMessage());
        }
        return buildValidationProblem(errors, request);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ProblemDetail handleParameterValidation(HandlerMethodValidationException ex, HttpServletRequest request) {
        List<String> errors = new ArrayList<>();
        for (ParameterValidationResult result : ex.getParameterValidationResults()) {
            String parameterName = result.getMethodParameter().getParameterName();
            for (MessageSourceResolvable error : result.getResolvableErrors()) {
                errors.add("Parameter '" + parameterName + "' " + error.getDefaultMessage());
            }
        }
        return buildValidationProblem(errors, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String expectedType = ex.getRequiredType() == null ? "a different type" : ex.getRequiredType().getSimpleName();
        String error = "Parameter '" + ex.getName() + "' must be of type " + expectedType;
        return buildValidationProblem(List.of(error), request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        // The parser message is not returned on purpose: it exposes internal class names
        log.debug("Unreadable request body on {}", request.getRequestURI(), ex);
        return buildProblem(HttpStatus.BAD_REQUEST, MALFORMED_REQUEST_TYPE, "Malformed request",
                "Request body is missing or is not valid JSON for this endpoint", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleOther(Exception ex, HttpServletRequest request) {
        // Spring MVC errors (404 unknown path, 405, 415, ...) already know their status and headers
        if (ex instanceof ErrorResponse errorResponse) {
            ProblemDetail problem = errorResponse.getBody();
            problem.setInstance(URI.create(request.getRequestURI()));
            return ResponseEntity.status(errorResponse.getStatusCode())
                    .headers(errorResponse.getHeaders())
                    .body(problem);
        }

        log.error("Unexpected error on {}", request.getRequestURI(), ex);
        ProblemDetail problem = buildProblem(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_ERROR_TYPE,
                "Internal server error", "Unexpected error occurred, please try again later", request);
        return ResponseEntity.internalServerError().body(problem);
    }

    private static ProblemDetail buildValidationProblem(List<String> errors, HttpServletRequest request) {
        ProblemDetail problem = buildProblem(HttpStatus.BAD_REQUEST, VALIDATION_TYPE, "Validation failed",
                String.join("; ", errors), request);
        problem.setProperty("errors", errors);
        return problem;
    }

    private static ProblemDetail buildProblem(HttpStatus status, String type, String title, String detail,
                                              HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create(type));
        problem.setTitle(title);
        problem.setInstance(URI.create(request.getRequestURI()));
        return problem;
    }
}
