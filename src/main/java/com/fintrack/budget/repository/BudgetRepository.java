package com.fintrack.budget.repository;

import com.fintrack.budget.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {

    List<Budget> findByUserIdOrderByStartDateDesc(UUID userId);
}
