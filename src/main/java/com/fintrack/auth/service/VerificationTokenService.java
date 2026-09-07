package com.fintrack.auth.service;

import com.fintrack.auth.entity.VerificationToken;
import com.fintrack.auth.entity.VerificationTokenType;
import com.fintrack.auth.repository.VerificationTokenRepository;
import com.fintrack.common.exception.BusinessException;
import com.fintrack.security.TokenHasher;
import com.fintrack.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class VerificationTokenService {

    private static final Duration EMAIL_VERIFICATION_TTL = Duration.ofHours(24);
    private static final Duration PASSWORD_RESET_TTL = Duration.ofHours(1);

    private final VerificationTokenRepository verificationTokenRepository;

    @Transactional
    public String issueEmailVerificationToken(User user) {
        return issue(user, VerificationTokenType.EMAIL_VERIFICATION, EMAIL_VERIFICATION_TTL);
    }

    @Transactional
    public String issuePasswordResetToken(User user) {
        return issue(user, VerificationTokenType.PASSWORD_RESET, PASSWORD_RESET_TTL);
    }

    private String issue(User user, VerificationTokenType type, Duration ttl) {
        String rawToken = TokenHasher.generateRawToken();
        VerificationToken entity = VerificationToken.builder()
                .user(user)
                .tokenHash(TokenHasher.hash(rawToken))
                .type(type)
                .expiresAt(Instant.now().plus(ttl))
                .build();
        verificationTokenRepository.save(entity);
        return rawToken;
    }

    /** Token'ı doğrular, tüketir (usedAt işaretler — tek kullanımlık) ve sahibi User'ı döner. */
    @Transactional
    public User consume(String rawToken, VerificationTokenType expectedType) {
        VerificationToken token = verificationTokenRepository.findByTokenHash(TokenHasher.hash(rawToken))
                .filter(t -> t.getType() == expectedType)
                .filter(VerificationToken::isValid)
                .orElseThrow(() -> new BusinessException("Token geçersiz veya süresi dolmuş"));

        token.setUsedAt(Instant.now());
        return token.getUser();
    }
}
