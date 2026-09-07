package com.fintrack.security;

import com.fintrack.common.exception.UnauthorizedException;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

/**
 * Ownership validation'ın tek gerçek kaynağı: her feature service'i "bu
 * kaynak gerçekten bu kullanıcıya mı ait" kontrolünü buradan aldığı
 * kullanıcı id'siyle yapar. Tekrar eden {@code SecurityContextHolder}
 * okuma/cast mantığını tek yerde toplar.
 */
public final class CurrentUserProvider {

    private CurrentUserProvider() {
    }

    public static UUID getUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            throw new UnauthorizedException("Kimliği doğrulanmış kullanıcı bulunamadı");
        }
        return principal.getUserId();
    }
}
