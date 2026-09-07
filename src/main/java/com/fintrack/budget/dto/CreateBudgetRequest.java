package com.fintrack.budget.dto;

import com.fintrack.budget.entity.BudgetPeriod;
import com.fintrack.common.enums.Currency;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** {@code categoryId} boş bırakılırsa tüm kategorileri kapsayan genel bir bütçe oluşturulur. */
public record CreateBudgetRequest(
        UUID categoryId,
        @NotNull BudgetPeriod period,
        @NotNull @Positive BigDecimal amountLimit,
        @NotNull Currency currency,
        @NotNull LocalDate startDate
) {
}
