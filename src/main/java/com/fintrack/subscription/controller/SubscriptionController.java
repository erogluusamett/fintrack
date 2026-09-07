package com.fintrack.subscription.controller;

import com.fintrack.common.dto.ApiResponse;
import com.fintrack.subscription.dto.CreateSubscriptionRequest;
import com.fintrack.subscription.dto.SubscriptionResponse;
import com.fintrack.subscription.service.SubscriptionService;
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
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping
    public ApiResponse<List<SubscriptionResponse>> list() {
        return ApiResponse.of(subscriptionService.list());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SubscriptionResponse> create(@Valid @RequestBody CreateSubscriptionRequest request) {
        return ApiResponse.of(subscriptionService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<SubscriptionResponse> update(@PathVariable UUID id, @Valid @RequestBody CreateSubscriptionRequest request) {
        return ApiResponse.of(subscriptionService.update(id, request));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<SubscriptionResponse> cancel(@PathVariable UUID id) {
        return ApiResponse.of(subscriptionService.cancel(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        subscriptionService.delete(id);
    }
}
