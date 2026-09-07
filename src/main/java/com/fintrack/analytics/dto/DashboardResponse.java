package com.fintrack.analytics.dto;

import com.fintrack.budget.dto.BudgetStatusResponse;
import com.fintrack.common.enums.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record DashboardResponse(
        String period,
        Currency currency,
        BigDecimal monthlyIncome,
        BigDecimal monthlyExpense,
        BigDecimal savings,
        BigDecimal savingsRate,
        BigDecimal subscriptionCost,
        CategoryAmount topSpendingCategory,
        HighestExpense highestExpense,
        List<BudgetUsage> budgetUsage
) {
    public record HighestExpense(UUID transactionId, String categoryName, BigDecimal amount, LocalDate date, String description) {
    }

    public record BudgetUsage(UUID budgetId, String categoryName, BigDecimal usagePercentage, BudgetStatusResponse.Status status) {
    }
}
