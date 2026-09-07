package com.fintrack.notification.repository;

import com.fintrack.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    /** {@code read ASC} okunmamışları (false=0) önce getirir. */
    Page<Notification> findByUserIdOrderByReadAscCreatedAtDesc(UUID userId, Pageable pageable);

    /** Aynı kaynak+başlık için tekrar bildirim üretmemek için (bkz. BudgetEventListener). */
    boolean existsByRelatedEntityIdAndTitle(UUID relatedEntityId, String title);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.user.id = :userId AND n.read = false")
    int markAllReadForUser(@Param("userId") UUID userId);
}
