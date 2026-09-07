package com.fintrack.analytics.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CategoryAmount(UUID categoryId, String categoryName, BigDecimal amount) {
}
