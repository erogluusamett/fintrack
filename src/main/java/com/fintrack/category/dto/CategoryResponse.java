package com.fintrack.category.dto;

import com.fintrack.category.entity.Category;
import com.fintrack.category.entity.CategoryType;

import java.util.UUID;

public record CategoryResponse(UUID id, String name, CategoryType type, boolean isDefault) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getType(), category.isDefault());
    }
}
