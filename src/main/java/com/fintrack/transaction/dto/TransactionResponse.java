package com.fintrack.transaction.dto;

import com.fintrack.common.enums.Currency;
import com.fintrack.transaction.entity.Transaction;
import com.fintrack.transaction.entity.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        UUID categoryId,
        String categoryName,
        TransactionType type,
        BigDecimal amount,
        Currency currency,
        LocalDate transactionDate,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
    public static TransactionResponse from(Transaction transaction) {
        var category = transaction.getCategory();
        return new TransactionResponse(
                transaction.getId(),
                category != null ? category.getId() : null,
                category != null ? category.getName() : null,
                transaction.getType(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getTransactionDate(),
                transaction.getDescription(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt()
        );
    }
}
