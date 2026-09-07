package com.fintrack.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * Fiziksel olarak silinmemesi gereken finansal kayıtlar için taban sınıf
 * (örn. Transaction). Sadece {@code deleted}/{@code deletedAt} alanlarını taşır.
 *
 * <p>{@code @SQLDelete} ve {@code @SQLRestriction} her somut entity'de ayrıca
 * tanımlanmalıdır — Hibernate bu annotation'ları table adı gerektirdiği için
 * {@code @MappedSuperclass} seviyesinde güvenilir şekilde miras almaz. Örnek
 * kullanım Transaction entity'sinde.</p>
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@MappedSuperclass
public abstract class SoftDeleteEntity extends BaseEntity {

    @Column(nullable = false)
    private boolean deleted = false;

    private Instant deletedAt;
}
