package com.fintrack.auth.entity;

import com.fintrack.common.entity.BaseEntity;
import com.fintrack.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * Refresh token'ın kendisi burada DÜZ METİN olarak saklanmaz — {@code tokenHash}
 * alanı SHA-256 hash'idir. DB sızıntısında token'ların kullanılamaz olması içindir
 * (parola hash'lemenin arkasındaki mantığın aynısı). Doğrulama sırasında gelen
 * token yeniden hash'lenip bu alanla karşılaştırılır.
 */
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class RefreshToken extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean revoked = false;

    public boolean isValid() {
        return !revoked && expiresAt.isAfter(Instant.now());
    }
}
