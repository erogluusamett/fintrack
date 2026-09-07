package com.fintrack.subscription.dto;

import com.fintrack.common.enums.Currency;
import com.fintrack.subscription.entity.BillingCycle;
import com.fintrack.subscription.entity.Subscription;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record SubscriptionResponse(
        UUID id,
        UUID categoryId,
        String categoryName,
        String name,
        BigDecimal amount,
        Currency currency,
        BillingCycle billingCycle,
        LocalDate nextBillingDate,
        boolean active
) {
    public static SubscriptionResponse from(Subscription subscription) {
        var category = subscription.getCategory();
        return new SubscriptionResponse(
                subscription.getId(),
                category != null ? category.getId() : null,
                category != null ? category.getName() : null,
                subscription.getName(),
                subscription.getAmount(),
                subscription.getCurrency(),
                subscription.getBillingCycle(),
                subscription.getNextBillingDate(),
                subscription.isActive()
        );
    }
}
