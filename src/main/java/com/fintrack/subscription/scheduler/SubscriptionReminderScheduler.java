package com.fintrack.subscription.scheduler;

import com.fintrack.notification.entity.NotificationType;
import com.fintrack.notification.service.NotificationService;
import com.fintrack.subscription.entity.Subscription;
import com.fintrack.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Aboneliklerin {@code nextBillingDate}'ini otomatik ilerletmez — spec'te
 * bu sadece RecurringTransaction için istenmişti (bkz. RecurringTransactionScheduler
 * Javadoc'u). Bu job yalnızca hatırlatma bildirimi üretir; kullanıcı
 * aboneliği kendi güncellemelidir. Aynı yenileme tarihi için tekrar
 * bildirim üretilmemesi {@code NotificationService#createIfAbsent}'in
 * başlığa gömülü tarihle dedup etmesiyle sağlanır.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionReminderScheduler {

    private final SubscriptionRepository subscriptionRepository;
    private final NotificationService notificationService;

    @Value("${fintrack.scheduler.subscription-reminder-window-days:3}")
    private int reminderWindowDays;

    @Scheduled(cron = "${fintrack.scheduler.subscription-reminder-cron:0 0 8 * * *}")
    public void remindUpcomingRenewals() {
        LocalDate windowEnd = LocalDate.now().plusDays(reminderWindowDays);
        List<Subscription> upcoming = subscriptionRepository.findByActiveTrueAndNextBillingDateLessThanEqual(windowEnd);

        log.info("Subscription reminder scheduler: {} abonelik kontrol ediliyor", upcoming.size());
        for (Subscription subscription : upcoming) {
            String title = "Abonelik Yenileniyor: %s (%s)".formatted(subscription.getName(), subscription.getNextBillingDate());
            notificationService.createIfAbsent(
                    subscription.getUser().getId(), NotificationType.SUBSCRIPTION_REMINDER,
                    title,
                    "%s aboneliğin %s tarihinde %s %s tutarında yenilenecek".formatted(
                            subscription.getName(), subscription.getNextBillingDate(), subscription.getAmount(), subscription.getCurrency()),
                    subscription.getId(), "SUBSCRIPTION");
        }
    }
}
