package com.fintrack.user.entity;

import com.fintrack.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Roller kasıtlı olarak serbest metin ({@code name}) olarak modellenmiştir,
 * Java enum olarak değil — böylece yeni bir rol eklemek şema/kod değişikliği
 * değil, tek bir INSERT gerektirir. Bkz. {@link RoleName} (bilinen roller için
 * derleme zamanı sabitleri).
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Role extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String name;
}
