package com.fintrack.user.controller;

import com.fintrack.common.dto.ApiResponse;
import com.fintrack.user.dto.ChangePasswordRequest;
import com.fintrack.user.dto.UpdateProfileRequest;
import com.fintrack.user.dto.UserResponse;
import com.fintrack.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ApiResponse<UserResponse> getCurrentUser() {
        return ApiResponse.of(userService.getCurrentUser());
    }

    @PutMapping
    public ApiResponse<UserResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.of(userService.updateProfile(request));
    }

    @PutMapping("/password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ApiResponse.of(null, "Şifre güncellendi");
    }
}
