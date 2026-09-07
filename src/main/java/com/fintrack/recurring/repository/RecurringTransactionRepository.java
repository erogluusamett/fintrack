package com.fintrack.recurring.repository;

import com.fintrack.recurring.entity.RecurringTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, UUID> {

    List<RecurringTransaction> findByUserIdOrderByNextExecutionDateAsc(UUID userId);

    /** RecurringTransactionScheduler için — kısmi index (active=true) bu sorguyla eşleşir. */
    List<RecurringTransaction> findByActiveTrueAndNextExecutionDateLessThanEqual(LocalDate date);
}
