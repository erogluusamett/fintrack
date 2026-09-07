package com.fintrack.recurring.scheduler;

import com.fintrack.notification.entity.NotificationType;
import com.fintrack.notification.service.NotificationService;
import com.fintrack.recurring.entity.RecurringFrequency;
import com.fintrack.recurring.entity.RecurringTransaction;
import com.fintrack.recurring.repository.RecurringTransactionRepository;
import com.fintrack.transaction.dto.CreateTransactionRequest;
import com.fintrack.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Faz 1/2'de sadece şablon olarak var olan RecurringTransaction'ları gerçek
 * Transaction kayıtlarına çevirir. Her koşum bağımsız bir transaction
 * sınırıdır ({@code TransactionService.createForUser} kendi
 * {@code @Transactional}'ına sahip) — bir kaydın işlenmesi başarısız olsa
 * bile diğerleri etkilenmez.
 * <p>
 * Bir kullanıcı zamanında çalıştırılamamışsa (uygulama bir süre kapalı
 * kaldıysa) {@code nextExecutionDate} birden fazla dönem geride kalabilir;
 * bu yüzden tek koşumda değil, tarih bugünü geçene kadar döngüyle işlenir.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RecurringTransactionScheduler {

    private static final int MAX_CATCHUP_ITERATIONS = 366;

    private final RecurringTransactionRepository recurringTransactionRepository;
    private final TransactionService transactionService;
    private final NotificationService notificationService;

    @Scheduled(cron = "${fintrack.scheduler.recurring-transaction-cron:0 0 1 * * *}")
    public void executeDueRecurringTransactions() {
        LocalDate today = LocalDate.now();
        List<RecurringTransaction> due = recurringTransactionRepository
                .findByActiveTrueAndNextExecutionDateLessThanEqual(today);

        log.info("Recurring transaction scheduler: {} kayıt işlenecek", due.size());
        for (RecurringTransaction recurring : due) {
            try {
                processOne(recurring, today);
            } catch (Exception ex) {
                log.error("Recurring transaction işlenemedi: id={}", recurring.getId(), ex);
            }
        }
    }

    private void processOne(RecurringTransaction recurring, LocalDate today) {
        int iterations = 0;
        LocalDate nextDate = recurring.getNextExecutionDate();

        while (!nextDate.isAfter(today) && iterations++ < MAX_CATCHUP_ITERATIONS) {
            UUID categoryId = recurring.getCategory() != null ? recurring.getCategory().getId() : null;
            String description = "Otomatik: " + (recurring.getDescription() != null ? recurring.getDescription() : recurring.getType());

            var request = new CreateTransactionRequest(
                    categoryId, recurring.getType(), recurring.getAmount(), recurring.getCurrency(), nextDate, description);
            transactionService.createForUser(recurring.getUser().getId(), request);

            notificationService.create(
                    recurring.getUser().getId(), NotificationType.PAYMENT_REMINDER,
                    "Otomatik İşlem Oluşturuldu",
                    "%s: %s %s tutarında bir işlem otomatik olarak oluşturuldu (%s)".formatted(
                            recurring.getType(), recurring.getAmount(), recurring.getCurrency(), nextDate),
                    recurring.getId(), "RECURRING_TRANSACTION");

            nextDate = advance(nextDate, recurring.getFrequency());
        }

        recurring.setNextExecutionDate(nextDate);
        recurringTransactionRepository.save(recurring);
    }

    private LocalDate advance(LocalDate date, RecurringFrequency frequency) {
        return switch (frequency) {
            case DAILY -> date.plusDays(1);
            case WEEKLY -> date.plusWeeks(1);
            case MONTHLY -> date.plusMonths(1);
            case YEARLY -> date.plusYears(1);
        };
    }
}
