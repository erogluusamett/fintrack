package com.fintrack.common.exception;

/**
 * Kullanıcı kimliği doğrulanmış ama başka bir kullanıcının kaynağına erişmeye
 * çalışıyorsa (403) fırlatılır. Ownership validation'ın standart hatası.
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
