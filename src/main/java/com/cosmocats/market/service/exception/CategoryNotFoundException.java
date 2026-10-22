package com.cosmocats.market.service.exception;

public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException(Long categoryId) {
        super("Category with id " + categoryId + " does not exist");
    }
}
