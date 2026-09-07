package com.fintrack.security;

import com.fintrack.user.entity.Role;
import com.fintrack.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtServiceTest {

    private static final String TEST_SECRET = "unit-test-secret-key-must-be-at-least-32-bytes-long-for-hs256";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties(TEST_SECRET, 900_000L, 604_800_000L);
        jwtService = new JwtService(properties);
    }

    @Test
    void generateAndParse_roundTripsTheSameUserIdAndRoles() {
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(user.getRoles()).thenReturn(Set.of(Role.builder().name("USER").build()));

        String token = jwtService.generateAccessToken(user);
        var parsed = jwtService.parse(token);

        assertThat(parsed).isPresent();
        assertThat(parsed.get().userId()).isEqualTo(userId);
        assertThat(parsed.get().roles()).containsExactly("ROLE_USER");
    }

    @Test
    void parse_returnsEmpty_forGarbageInput() {
        assertThat(jwtService.parse("not-a-jwt-at-all")).isEmpty();
    }

    @Test
    void parse_returnsEmpty_forTokenSignedWithADifferentSecret() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(UUID.randomUUID());
        when(user.getRoles()).thenReturn(Set.of());

        JwtService otherService = new JwtService(new JwtProperties(
                "a-completely-different-secret-key-also-at-least-32-bytes", 900_000L, 604_800_000L));
        String tokenFromOtherService = otherService.generateAccessToken(user);

        assertThat(jwtService.parse(tokenFromOtherService)).isEmpty();
    }
}
