package com.fintrack.transaction.event;

import com.fintrack.common.enums.Currency;
import com.fintrack.transaction.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Mimari dökümanındaki (docs/ARCHITECTURE.md §2.3) event-driven örneğin
 * publish tarafı. {@code budget.listener.BudgetEventListener} bunu
 * {@code AFTER_COMMIT} fazında dinleyip ilgili bütçelerin limit durumunu
 * kontrol eder. TransactionService, Budget modülünü hiç bilmez.
 */
public record TransactionCreatedEvent(
        UUID transactionId,
        UUID userId,
        UUID categoryId,
        TransactionType type,
        BigDecimal amount,
        Currency currency,
        LocalDate transactionDate
) {
}
