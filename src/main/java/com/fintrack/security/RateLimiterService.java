package com.fintrack.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Redis {@code INCR} + {@code EXPIRE} ile sabit pencereli (fixed-window)
 * rate limiting. {@code INCR} atomik olduğu için race condition riski yok:
 * aynı anda gelen iki istek bile sayaçta çakışmaz. Pencerenin ilk isteğinde
 * ({@code count == 1}) TTL set edilir — sonraki istekler sadece sayacı
 * artırır, TTL'i sıfırlamaz (yani pencere "kayan" değil "sabit"tir).
 * <p>
 * <b>Fail-open:</b> Redis'e ulaşılamazsa isteğe izin verilir (rate limit
 * uygulanmaz), engellenmez. Rate limiting savunma katmanının ikincil bir
 * parçasıdır (asıl güvenlik parola hash'leme + JWT'de); Redis'in geçici bir
 * kesintisi TÜM kullanıcıların login olamamasına yol açmamalı — bu, rate
 * limiter'ın kendisinin bir DoS vektörüne dönüşmesi anlamına gelir.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimiterService {

    private final StringRedisTemplate redisTemplate;

    public boolean tryConsume(String key, int maxRequests, Duration window) {
        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count == null) {
                return true;
            }
            if (count == 1L) {
                redisTemplate.expire(key, window);
            }
            return count <= maxRequests;
        } catch (Exception ex) {
            log.warn("Redis'e ulaşılamadı, rate limiting bu istek için atlanıyor (fail-open): {}", ex.getMessage());
            return true;
        }
    }
}
