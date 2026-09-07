package com.fintrack.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * Her entity için ortak kimlik ve audit alanları.
 * createdAt/updatedAt Spring Data JPA auditing ile otomatik doldurulur
 * (bkz. {@code @EnableJpaAuditing} FintrackApplication'da).
 *
 * <p>{@code @SuperBuilder} kullanan her alt sınıf, üst sınıfın da bu
 * annotation'a sahip olmasını gerektirir — Lombok'un builder zincirleme
 * kısıtıdır. {@code @NoArgsConstructor} ise JPA'nın reflection ile entity
 * oluşturabilmesi ve alt sınıfların {@code @NoArgsConstructor}'ının
 * {@code super()} çağırabilmesi için zorunludur.</p>
 */
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;
}
