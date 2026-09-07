package com.fintrack.transaction.dto;

import com.fintrack.common.enums.Currency;
import com.fintrack.transaction.entity.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateTransactionRequest(
        UUID categoryId,
        @NotNull TransactionType type,
        @NotNull @Positive BigDecimal amount,
        @NotNull Currency currency,
        @NotNull LocalDate transactionDate,
        @Size(max = 500) String description
) {
}
