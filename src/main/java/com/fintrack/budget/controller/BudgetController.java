package com.fintrack.budget.controller;

import com.fintrack.budget.dto.BudgetResponse;
import com.fintrack.budget.dto.BudgetStatusResponse;
import com.fintrack.budget.dto.CreateBudgetRequest;
import com.fintrack.budget.service.BudgetService;
import com.fintrack.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    public ApiResponse<List<BudgetResponse>> list() {
        return ApiResponse.of(budgetService.list());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<BudgetResponse> create(@Valid @RequestBody CreateBudgetRequest request) {
        return ApiResponse.of(budgetService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<BudgetResponse> update(@PathVariable UUID id, @Valid @RequestBody CreateBudgetRequest request) {
        return ApiResponse.of(budgetService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        budgetService.delete(id);
    }

    @GetMapping("/{id}/status")
    public ApiResponse<BudgetStatusResponse> status(@PathVariable UUID id) {
        return ApiResponse.of(budgetService.status(id));
    }
}
