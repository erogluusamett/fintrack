package com.fintrack.analytics.dto;

import java.math.BigDecimal;

public record TrendPoint(String period, BigDecimal income, BigDecimal expense, BigDecimal savings) {
}
