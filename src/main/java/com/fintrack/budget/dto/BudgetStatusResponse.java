package com.fintrack.budget.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record BudgetStatusResponse(
        UUID budgetId,
        BigDecimal limit,
        BigDecimal spent,
        BigDecimal remaining,
        BigDecimal usagePercentage,
        Status status
) {
    public enum Status {
        OK, WARNING, EXCEEDED
    }
}
