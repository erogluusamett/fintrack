package com.fintrack.recurring.controller;

import com.fintrack.common.dto.ApiResponse;
import com.fintrack.recurring.dto.CreateRecurringTransactionRequest;
import com.fintrack.recurring.dto.RecurringTransactionResponse;
import com.fintrack.recurring.service.RecurringTransactionService;
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
@RequestMapping("/api/v1/recurring-transactions")
@RequiredArgsConstructor
public class RecurringTransactionController {

    private final RecurringTransactionService recurringTransactionService;

    @GetMapping
    public ApiResponse<List<RecurringTransactionResponse>> list() {
        return ApiResponse.of(recurringTransactionService.list());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RecurringTransactionResponse> create(@Valid @RequestBody CreateRecurringTransactionRequest request) {
        return ApiResponse.of(recurringTransactionService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<RecurringTransactionResponse> update(@PathVariable UUID id, @Valid @RequestBody CreateRecurringTransactionRequest request) {
        return ApiResponse.of(recurringTransactionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        recurringTransactionService.delete(id);
    }

    @PostMapping("/{id}/pause")
    public ApiResponse<RecurringTransactionResponse> pause(@PathVariable UUID id) {
        return ApiResponse.of(recurringTransactionService.pause(id));
    }

    @PostMapping("/{id}/resume")
    public ApiResponse<RecurringTransactionResponse> resume(@PathVariable UUID id) {
        return ApiResponse.of(recurringTransactionService.resume(id));
    }
}
