package com.fintrack.subscription.dto;

import com.fintrack.common.enums.Currency;
import com.fintrack.subscription.entity.BillingCycle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateSubscriptionRequest(
        UUID categoryId,
        @NotBlank @Size(max = 150) String name,
        @NotNull @Positive BigDecimal amount,
        @NotNull Currency currency,
        @NotNull BillingCycle billingCycle,
        @NotNull LocalDate nextBillingDate
) {
}
