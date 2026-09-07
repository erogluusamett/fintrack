package com.fintrack.notification.entity;

import com.fintrack.common.entity.BaseEntity;
import com.fintrack.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

import java.util.UUID;

/**
 * {@code relatedEntityId}/{@code relatedEntityType} polimorfik bir referans
 * (örn. "BUDGET" + budget id) — frontend'in bildirime tıklayınca ilgili
 * kaynağa gitmesini sağlar. Ayrı bir tablo/FK yerine bu şekilde tutulması
 * bilinçli: bildirim üretilebilecek kaynak türü zamanla artacak
 * (Budget/Subscription/RecurringTransaction/Insight), her biri için ayrı
 * nullable FK kolonu eklemek yerine tek, genişleyebilir bir çift kullanıldı.
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false)
    @Builder.Default
    private boolean read = false;

    @Column(name = "related_entity_id")
    private UUID relatedEntityId;

    @Column(name = "related_entity_type", length = 30)
    private String relatedEntityType;
}
