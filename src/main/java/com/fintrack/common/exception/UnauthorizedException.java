package com.fintrack.common.exception;

/** Kimlik doğrulama başarısız veya eksik olduğunda (401) fırlatılır. */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
