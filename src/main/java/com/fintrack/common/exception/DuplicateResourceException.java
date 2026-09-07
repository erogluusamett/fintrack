package com.fintrack.common.exception;

/** Benzersiz olması gereken bir kaynak zaten varsa (409) fırlatılır. */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
