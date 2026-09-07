package com.fintrack.budget.dto;

import com.fintrack.budget.entity.Budget;
import com.fintrack.budget.entity.BudgetPeriod;
import com.fintrack.common.enums.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record BudgetResponse(
        UUID id,
        UUID categoryId,
        String categoryName,
        BudgetPeriod period,
        BigDecimal amountLimit,
        Currency currency,
        LocalDate startDate,
        LocalDate endDate
) {
    public static BudgetResponse from(Budget budget) {
        var category = budget.getCategory();
        return new BudgetResponse(
                budget.getId(),
                category != null ? category.getId() : null,
                category != null ? category.getName() : null,
                budget.getPeriod(),
                budget.getAmountLimit(),
                budget.getCurrency(),
                budget.getStartDate(),
                budget.getEndDate()
        );
    }
}
