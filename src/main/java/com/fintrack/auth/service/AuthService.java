package com.fintrack.auth.service;

import com.fintrack.auth.dto.AuthResponse;
import com.fintrack.auth.dto.LoginRequest;
import com.fintrack.auth.dto.RegisterRequest;
import com.fintrack.auth.entity.VerificationTokenType;
import com.fintrack.auth.event.PasswordResetRequestedEvent;
import com.fintrack.auth.event.UserRegisteredEvent;
import com.fintrack.common.exception.DuplicateResourceException;
import com.fintrack.common.exception.ResourceNotFoundException;
import com.fintrack.security.AuthenticatedUser;
import com.fintrack.security.JwtProperties;
import com.fintrack.security.JwtService;
import com.fintrack.user.entity.Role;
import com.fintrack.user.entity.RoleName;
import com.fintrack.user.entity.User;
import com.fintrack.user.repository.RoleRepository;
import com.fintrack.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final RefreshTokenService refreshTokenService;
    private final VerificationTokenService verificationTokenService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Bu e-posta adresi zaten kayıtlı: " + request.email());
        }

        Role userRole = roleRepository.findByName(RoleName.USER.name())
                .orElseThrow(() -> new IllegalStateException("USER rolü seed edilmemiş — Flyway migration'ı kontrol et"));

        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .defaultCurrency(request.defaultCurrency())
                .roles(Set.of(userRole))
                .build();
        userRepository.save(user);

        String verificationToken = verificationTokenService.issueEmailVerificationToken(user);
        eventPublisher.publishEvent(new UserRegisteredEvent(user.getId(), user.getEmail(), user.getFirstName(), verificationToken));

        return issueTokens(user);
    }

    public AuthResponse login(LoginRequest request) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        UUID userId = ((AuthenticatedUser) authentication.getPrincipal()).getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", userId));

        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(String rawRefreshToken) {
        var result = refreshTokenService.validateAndRotate(rawRefreshToken);
        String accessToken = jwtService.generateAccessToken(result.user());
        return AuthResponse.of(accessToken, result.newRawToken(), jwtProperties.accessTokenExpirationMs());
    }

    public void logout(String rawRefreshToken) {
        refreshTokenService.revoke(rawRefreshToken);
    }

    @Transactional
    public void verifyEmail(String rawToken) {
        User user = verificationTokenService.consume(rawToken, VerificationTokenType.EMAIL_VERIFICATION);
        user.setEmailVerified(true);
    }

    /**
     * Kullanıcı enumeration'ı önlemek için bu metot her zaman sessizce döner —
     * e-posta sistemde olsun ya da olmasın controller aynı 202 cevabını verir.
     */
    @Transactional
    public void forgotPassword(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            String resetToken = verificationTokenService.issuePasswordResetToken(user);
            eventPublisher.publishEvent(new PasswordResetRequestedEvent(user.getId(), user.getEmail(), user.getFirstName(), resetToken));
        });
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        User user = verificationTokenService.consume(rawToken, VerificationTokenType.PASSWORD_RESET);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshTokenService.issue(user);
        return AuthResponse.of(accessToken, refreshToken, jwtProperties.accessTokenExpirationMs());
    }
}
