package com.fintrack.notification.controller;

import com.fintrack.common.dto.ApiResponse;
import com.fintrack.common.dto.PageResponse;
import com.fintrack.notification.dto.NotificationResponse;
import com.fintrack.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ApiResponse<PageResponse<NotificationResponse>> list(@PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.of(notificationService.list(pageable));
    }

    @PutMapping("/{id}/read")
    public ApiResponse<Void> markRead(@PathVariable UUID id) {
        notificationService.markRead(id);
        return ApiResponse.of(null, "Bildirim okundu olarak işaretlendi");
    }

    @PutMapping("/read-all")
    public ApiResponse<Void> markAllRead() {
        notificationService.markAllRead();
        return ApiResponse.of(null, "Tüm bildirimler okundu olarak işaretlendi");
    }
}
