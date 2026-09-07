package com.fintrack.auth.event;

import java.util.UUID;

/** AuthService tarafından forgot-password akışında yayınlanır; sıfırlama e-postasını tetikler. */
public record PasswordResetRequestedEvent(UUID userId, String email, String firstName, String rawToken) {
}
