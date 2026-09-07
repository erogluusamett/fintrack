package com.fintrack.common.dto;

import java.time.Instant;

/**
 * Tüm başarılı response'lar için tek tip zarf. Entity'ler asla doğrudan
 * dönmez; controller'lar her zaman bu tipte (veya {@code Page<T>} sarmalayan
 * bir türevinde) yanıt verir.
 */
public record ApiResponse<T>(boolean success, T data, String message, Instant timestamp) {

    public static <T> ApiResponse<T> of(T data) {
        return new ApiResponse<>(true, data, null, Instant.now());
    }

    public static <T> ApiResponse<T> of(T data, String message) {
        return new ApiResponse<>(true, data, message, Instant.now());
    }
}
