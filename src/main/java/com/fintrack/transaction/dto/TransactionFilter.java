package com.fintrack.transaction.dto;

import com.fintrack.transaction.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** GET /transactions'ın isteğe bağlı filtreleri — hepsi null olabilir. */
public record TransactionFilter(
        TransactionType type,
        UUID categoryId,
        LocalDate from,
        LocalDate to,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        String search
) {
}
