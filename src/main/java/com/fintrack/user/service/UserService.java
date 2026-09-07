package com.fintrack.user.service;

import com.fintrack.common.exception.ForbiddenException;
import com.fintrack.common.exception.ResourceNotFoundException;
import com.fintrack.security.CurrentUserProvider;
import com.fintrack.user.dto.ChangePasswordRequest;
import com.fintrack.user.dto.UpdateProfileRequest;
import com.fintrack.user.dto.UserResponse;
import com.fintrack.user.entity.User;
import com.fintrack.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        return UserResponse.from(currentUser());
    }

    @Transactional
    public UserResponse updateProfile(UpdateProfileRequest request) {
        User user = currentUser();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setDefaultCurrency(request.defaultCurrency());
        return UserResponse.from(user);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = currentUser();
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new ForbiddenException("Mevcut şifre hatalı");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
    }

    private User currentUser() {
        UUID userId = CurrentUserProvider.getUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", userId));
    }
}
