package com.fintrack.budget.listener;

import com.fintrack.budget.dto.BudgetStatusResponse;
import com.fintrack.budget.entity.Budget;
import com.fintrack.budget.repository.BudgetRepository;
import com.fintrack.budget.service.BudgetService;
import com.fintrack.notification.entity.NotificationType;
import com.fintrack.notification.service.NotificationService;
import com.fintrack.transaction.entity.TransactionType;
import com.fintrack.transaction.event.TransactionCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

/**
 * docs/ARCHITECTURE.md §2.3'te tarif edilen zincirin uygulaması:
 * TransactionCreatedEvent → Budget kontrolü → Notification. TransactionService
 * bu sınıfın varlığından habersizdir (event-driven gevşek bağlılık).
 * {@code AFTER_COMMIT}: transaction gerçekten kaydedilmeden bütçe kontrolü
 * yapılmaz. Exception'lar burada yutulup loglanır — bu bir best-effort
 * bildirim akışıdır, budget kontrolündeki bir hata orijinal transaction'ı
 * (zaten commit olmuş) etkilememeli.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BudgetEventListener {

    private final BudgetRepository budgetRepository;
    private final BudgetService budgetService;
    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTransactionCreated(TransactionCreatedEvent event) {
        try {
            if (event.type() != TransactionType.EXPENSE) {
                return;
            }
            List<Budget> budgets = budgetRepository.findByUserIdOrderByStartDateDesc(event.userId());
            for (Budget budget : budgets) {
                if (matches(budget, event)) {
                    checkAndNotify(budget);
                }
            }
        } catch (Exception ex) {
            log.error("Budget kontrolü başarısız oldu: event={}", event, ex);
        }
    }

    private boolean matches(Budget budget, TransactionCreatedEvent event) {
        boolean categoryMatches = budget.getCategory() == null
                || budget.getCategory().getId().equals(event.categoryId());
        boolean currencyMatches = budget.getCurrency() == event.currency();
        boolean withinPeriod = !event.transactionDate().isBefore(budget.getStartDate())
                && !event.transactionDate().isAfter(budget.getEndDate());
        return categoryMatches && currencyMatches && withinPeriod;
    }

    private void checkAndNotify(Budget budget) {
        BudgetStatusResponse status = budgetService.computeStatus(budget);
        String scope = budget.getCategory() != null ? budget.getCategory().getName() : "Genel";

        if (status.status() == BudgetStatusResponse.Status.EXCEEDED) {
            notificationService.createIfAbsent(
                    budget.getUser().getId(), NotificationType.BUDGET_WARNING,
                    "Bütçe Aşıldı: " + scope,
                    "%s bütçen %%%.0f kullanıma ulaştı (%s / %s %s)".formatted(
                            scope, status.usagePercentage(), status.spent(), budget.getAmountLimit(), budget.getCurrency()),
                    budget.getId(), "BUDGET");
        } else if (status.status() == BudgetStatusResponse.Status.WARNING) {
            notificationService.createIfAbsent(
                    budget.getUser().getId(), NotificationType.BUDGET_WARNING,
                    "Bütçe Uyarısı: " + scope,
                    "%s bütçen %%%.0f kullanıma ulaştı (%s / %s %s)".formatted(
                            scope, status.usagePercentage(), status.spent(), budget.getAmountLimit(), budget.getCurrency()),
                    budget.getId(), "BUDGET");
        }
    }
}
