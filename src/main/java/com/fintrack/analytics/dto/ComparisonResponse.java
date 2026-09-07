package com.fintrack.analytics.dto;

import java.math.BigDecimal;

public record ComparisonResponse(
        PeriodSummary currentPeriod,
        PeriodSummary previousPeriod,
        BigDecimal incomeChangePercentage,
        BigDecimal expenseChangePercentage,
        BigDecimal savingsChangePercentage
) {
    public record PeriodSummary(String period, BigDecimal income, BigDecimal expense, BigDecimal savings) {
    }
}
