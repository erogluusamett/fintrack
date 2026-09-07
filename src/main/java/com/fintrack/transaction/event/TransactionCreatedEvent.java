package com.fintrack.transaction.event;

import com.fintrack.transaction.entity.TransactionType;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Mimari dökümanındaki (docs/ARCHITECTURE.md §2.3) event-driven örneğin
 * publish tarafı. Şu an dinleyicisi yok — Budget modülü (Faz 2) geldiğinde
 * {@code BudgetEventListener} bunu {@code AFTER_COMMIT} fazında dinleyip
 * limit kontrolü yapacak. Dinleyicisi olmayan bir event yayınlamak zararsızdır;
 * TransactionService'in Budget modülünü hiç bilmemesini sağlar.
 */
public record TransactionCreatedEvent(
        UUID transactionId,
        UUID userId,
        UUID categoryId,
        TransactionType type,
        BigDecimal amount
) {
}
