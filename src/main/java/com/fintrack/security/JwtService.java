package com.fintrack.security;

import com.fintrack.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Yalnızca <b>access token</b> (JWT) üretir/doğrular. Refresh token kasıtlı
 * olarak JWT değildir — bkz. {@link com.fintrack.auth.service.RefreshTokenService}
 * — opak, DB'de tutulan ve iptal edilebilen bir token'dır. Access token'ın
 * imzası burada doğrulanır ve içeriğine güvenilir (stateless): kullanıcının
 * rolü değişirse bu değişiklik ancak token süresi dolup yenilendiğinde
 * yansır — 15 dakikalık ömür için kabul edilebilir bir esneklik/performans
 * dengesi.
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    private static final String CLAIM_ROLES = "roles";

    private final JwtProperties properties;

    private SecretKey signingKey;

    private SecretKey key() {
        if (signingKey == null) {
            signingKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
        }
        return signingKey;
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        List<String> roles = user.getRoles().stream().map(r -> "ROLE_" + r.getName()).toList();

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim(CLAIM_ROLES, roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(properties.accessTokenExpirationMs())))
                .signWith(key())
                .compact();
    }

    public Optional<ParsedToken> parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            UUID userId = UUID.fromString(claims.getSubject());
            @SuppressWarnings("unchecked")
            List<String> roles = claims.get(CLAIM_ROLES, List.class);
            return Optional.of(new ParsedToken(userId, roles));
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public record ParsedToken(UUID userId, List<String> roles) {
    }
}
