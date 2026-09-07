package com.fintrack.user.entity;

/**
 * Sistemin bildiği "iyi bilinen" roller — seed data ve varsayılan atama için
 * derleme zamanı güvenliği sağlar. {@link Role} tablosu yine de serbest
 * metin olduğu için yeni bir rol eklemek (örn. ACCOUNTANT) kod değişikliği
 * değil, sadece yeni bir satır gerektirir.
 */
public enum RoleName {
    USER, ADMIN
}
