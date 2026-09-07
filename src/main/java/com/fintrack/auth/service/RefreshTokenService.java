package com.fintrack.auth.service;

import com.fintrack.auth.entity.RefreshToken;
import com.fintrack.auth.repository.RefreshTokenRepository;
import com.fintrack.common.exception.UnauthorizedException;
import com.fintrack.security.JwtProperties;
import com.fintrack.security.TokenHasher;
import com.fintrack.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    @Transactional
    public String issue(User user) {
        String rawToken = TokenHasher.generateRawToken();
        RefreshToken entity = RefreshToken.builder()
                .user(user)
                .tokenHash(TokenHasher.hash(rawToken))
                .expiresAt(Instant.now().plusMillis(jwtProperties.refreshTokenExpirationMs()))
                .build();
        refreshTokenRepository.save(entity);
        return rawToken;
    }

    /**
     * Refresh token'ı doğrular, eskisini iptal edip yenisini döner (rotation).
     * Rotation, çalınmış bir refresh token'ın süresiz kullanılabilmesini
     * engeller: her kullanım eskisini geçersiz kılar.
     */
    @Transactional
    public RotationResult validateAndRotate(String rawToken) {
        RefreshToken existing = refreshTokenRepository.findByTokenHash(TokenHasher.hash(rawToken))
                .filter(RefreshToken::isValid)
                .orElseThrow(() -> new UnauthorizedException("Refresh token geçersiz veya süresi dolmuş"));

        existing.setRevoked(true);
        User user = existing.getUser();
        String newRawToken = issue(user);
        return new RotationResult(user, newRawToken);
    }

    @Transactional
    public void revoke(String rawToken) {
        refreshTokenRepository.findByTokenHash(TokenHasher.hash(rawToken))
                .ifPresent(token -> token.setRevoked(true));
    }

    public record RotationResult(User user, String newRawToken) {
    }
}
