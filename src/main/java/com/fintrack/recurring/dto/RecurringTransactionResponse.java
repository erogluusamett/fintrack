package com.fintrack.recurring.dto;

import com.fintrack.common.enums.Currency;
import com.fintrack.recurring.entity.RecurringFrequency;
import com.fintrack.recurring.entity.RecurringTransaction;
import com.fintrack.transaction.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RecurringTransactionResponse(
        UUID id,
        UUID categoryId,
        String categoryName,
        TransactionType type,
        BigDecimal amount,
        Currency currency,
        RecurringFrequency frequency,
        LocalDate nextExecutionDate,
        boolean active,
        String description
) {
    public static RecurringTransactionResponse from(RecurringTransaction recurring) {
        var category = recurring.getCategory();
        return new RecurringTransactionResponse(
                recurring.getId(),
                category != null ? category.getId() : null,
                category != null ? category.getName() : null,
                recurring.getType(),
                recurring.getAmount(),
                recurring.getCurrency(),
                recurring.getFrequency(),
                recurring.getNextExecutionDate(),
                recurring.isActive(),
                recurring.getDescription()
        );
    }
}
