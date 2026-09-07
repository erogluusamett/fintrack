package com.fintrack.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Yalnızca {@code name} güncellenebilir — {@code type} oluşturulduktan sonra sabittir (bkz. CategoryService Javadoc). */
public record UpdateCategoryRequest(@NotBlank @Size(max = 100) String name) {
}
