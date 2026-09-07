package com.fintrack.common.exception;

/**
 * Sözdizimsel olarak geçerli ama iş kuralını ihlal eden istekler için (422)
 * fırlatılır. Örn: zaten iptal edilmiş bir aboneliği tekrar iptal etmeye
 * çalışmak. Bean Validation hatalarından (400) kasıtlı olarak ayrılmıştır.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
