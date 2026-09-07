package com.fintrack.notification.service;

import com.fintrack.common.dto.PageResponse;
import com.fintrack.common.exception.ForbiddenException;
import com.fintrack.common.exception.ResourceNotFoundException;
import com.fintrack.notification.dto.NotificationResponse;
import com.fintrack.notification.entity.Notification;
import com.fintrack.notification.entity.NotificationType;
import com.fintrack.notification.repository.NotificationRepository;
import com.fintrack.security.CurrentUserProvider;
import com.fintrack.user.entity.User;
import com.fintrack.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> list(Pageable pageable) {
        UUID userId = CurrentUserProvider.getUserId();
        return PageResponse.from(notificationRepository
                .findByUserIdOrderByReadAscCreatedAtDesc(userId, pageable)
                .map(NotificationResponse::from));
    }

    @Transactional
    public void markRead(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Notification", id));
        if (!notification.getUser().getId().equals(CurrentUserProvider.getUserId())) {
            throw new ForbiddenException("Bu bildirime erişim yetkin yok");
        }
        notification.setRead(true);
    }

    @Transactional
    public void markAllRead() {
        notificationRepository.markAllReadForUser(CurrentUserProvider.getUserId());
    }

    /**
     * Event listener'lar ve scheduler'lar tarafından çağrılır — kullanıcıya
     * doğrudan expose edilmez. {@code REQUIRES_NEW} gerekçesi için
     * {@link #createIfAbsent} Javadoc'una bak.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void create(UUID userId, NotificationType type, String title, String message, UUID relatedEntityId, String relatedEntityType) {
        User user = userRepository.getReferenceById(userId);
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .relatedEntityId(relatedEntityId)
                .relatedEntityType(relatedEntityType)
                .build();
        notificationRepository.save(notification);
    }

    /**
     * Aynı kaynak için aynı başlıkta bildirim zaten varsa tekrar üretmez
     * (spam önleme). {@code REQUIRES_NEW}: bu metot event listener'lardan
     * ({@code @TransactionalEventListener(AFTER_COMMIT)}) çağrılıyor — o
     * noktada orijinal transaction'ın synchronization'ı henüz tam
     * temizlenmemiş olabiliyor ve normal {@code REQUIRED} propagation bu
     * belirsiz duruma "katılmaya" çalışıp yazma işlemini gerçekten commit
     * etmeyebiliyor (bilinen bir Spring davranışı). {@code REQUIRES_NEW}
     * bağımsız, garantili bir fiziksel transaction açar.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createIfAbsent(UUID userId, NotificationType type, String title, String message, UUID relatedEntityId, String relatedEntityType) {
        if (!notificationRepository.existsByRelatedEntityIdAndTitle(relatedEntityId, title)) {
            create(userId, type, title, message, relatedEntityId, relatedEntityType);
        }
    }
}
