package com.fintrack.notification.service;

import com.fintrack.notification.entity.NotificationType;
import com.fintrack.notification.repository.NotificationRepository;
import com.fintrack.user.entity.User;
import com.fintrack.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * BudgetEventListener/SubscriptionReminderScheduler'ın spam yapmaması bu
 * dedup mantığına dayanıyor — {@code createIfAbsent} yanlış davranırsa her
 * gelen transaction/scheduler koşumu ayrı bir bildirim üretir.
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void createIfAbsent_doesNotSave_whenANotificationWithTheSameRelatedEntityAndTitleAlreadyExists() {
        UUID relatedEntityId = UUID.randomUUID();
        when(notificationRepository.existsByRelatedEntityIdAndTitle(relatedEntityId, "Bütçe Aşıldı: Food"))
                .thenReturn(true);

        notificationService.createIfAbsent(
                UUID.randomUUID(), NotificationType.BUDGET_WARNING,
                "Bütçe Aşıldı: Food", "mesaj", relatedEntityId, "BUDGET");

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void createIfAbsent_saves_whenNoMatchingNotificationExistsYet() {
        UUID relatedEntityId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(notificationRepository.existsByRelatedEntityIdAndTitle(relatedEntityId, "Bütçe Aşıldı: Food"))
                .thenReturn(false);
        when(userRepository.getReferenceById(userId)).thenReturn(mock(User.class));

        notificationService.createIfAbsent(
                userId, NotificationType.BUDGET_WARNING,
                "Bütçe Aşıldı: Food", "mesaj", relatedEntityId, "BUDGET");

        verify(notificationRepository, times(1)).save(any());
    }
}
