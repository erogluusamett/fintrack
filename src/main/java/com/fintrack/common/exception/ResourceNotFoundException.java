package com.fintrack.common.exception;

/** İstenen kaynak bulunamadığında (404) fırlatılır. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String resource, Object id) {
        return new ResourceNotFoundException("%s bulunamadı: %s".formatted(resource, id));
    }
}
