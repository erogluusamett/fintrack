package com.fintrack.user.dto;

import com.fintrack.common.enums.Currency;
import com.fintrack.user.entity.User;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record UserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        Currency defaultCurrency,
        boolean emailVerified,
        Set<String> roles,
        Instant createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getDefaultCurrency(),
                user.isEmailVerified(),
                user.getRoles().stream().map(com.fintrack.user.entity.Role::getName).collect(Collectors.toSet()),
                user.getCreatedAt()
        );
    }
}
