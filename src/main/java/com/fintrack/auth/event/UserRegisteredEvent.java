package com.fintrack.auth.event;

import java.util.UUID;

/** AuthService tarafından kayıt tamamlandığında yayınlanır; doğrulama e-postasını tetikler. */
public record UserRegisteredEvent(UUID userId, String email, String firstName, String rawVerificationToken) {
}
