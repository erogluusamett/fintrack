package com.fintrack.subscription.repository;

import com.fintrack.subscription.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    List<Subscription> findByUserIdOrderByNextBillingDateAsc(UUID userId);

    /** SubscriptionReminderScheduler için — yaklaşan (ve gecikmiş) yenilemeler. */
    List<Subscription> findByActiveTrueAndNextBillingDateLessThanEqual(LocalDate date);
}
