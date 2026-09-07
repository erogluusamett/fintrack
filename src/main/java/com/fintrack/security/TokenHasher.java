package com.fintrack.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Refresh token ve verification token'lar için ortak üretim/hash mantığı.
 * SHA-256 kasıtlı olarak seçildi (BCrypt gibi yavaş bir hash değil): bu
 * token'lar parolanın aksine zaten yüksek entropili, rastgele üretilmiş
 * değerler olduğu için brute-force riski yok — burada asıl amaç DB
 * sızıntısında token'ların doğrudan kullanılabilir olmasını önlemek.
 */
public final class TokenHasher {

    private static final SecureRandom RANDOM = new SecureRandom();

    private TokenHasher() {
    }

    public static String generateRawToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 kullanılamıyor", ex);
        }
    }
}
