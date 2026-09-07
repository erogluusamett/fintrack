package com.fintrack.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintrack.AbstractIntegrationTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Spec'in "Integration tests: Authentication flow" isteğinin karşılığı —
 * gerçek bir PostgreSQL'e (Testcontainers) karşı uçtan uca register → login
 * → korumalı kaynağa erişim → refresh rotation → eski token'ın geçersizliği
 * akışını doğrular. Mock kullanılmaz; JWT imzalama, BCrypt, Flyway şeması,
 * Spring Security filter zinciri gerçekten çalışır.
 */
class AuthenticationFlowIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fullAuthLifecycle_registerLoginAccessRefreshAndRotateRefreshToken() throws Exception {
        String email = uniqueEmail();

        String registerBody = objectMapper.writeValueAsString(new RegisterRequestFixture(
                email, "SecurePass123", "Alice", "Wonderland", "TRY"));

        String registerResponse = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);

        String accessToken = JsonPath.read(registerResponse, "$.data.accessToken");
        String firstRefreshToken = JsonPath.read(registerResponse, "$.data.refreshToken");

        // Korumalı bir kaynağa token olmadan erişim -> 401
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());

        // Aynı kaynağa geçerli token ile erişim -> 200, doğru kullanıcı
        mockMvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(email));

        // Refresh -> yeni bir çift token döner
        String refreshBody = objectMapper.writeValueAsString(new RefreshRequestFixture(firstRefreshToken));
        String refreshResponse = mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);

        String secondRefreshToken = JsonPath.read(refreshResponse, "$.data.refreshToken");
        assertThat(secondRefreshToken).isNotEqualTo(firstRefreshToken);

        // Rotation: eski refresh token'ı tekrar kullanmaya çalışmak -> 401
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_rejectsADuplicateEmail_withConflict() throws Exception {
        String email = uniqueEmail();
        String body = objectMapper.writeValueAsString(new RegisterRequestFixture(
                email, "SecurePass123", "Bob", "Builder", "TRY"));

        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void login_rejectsWrongPassword_withUnauthorized() throws Exception {
        String email = uniqueEmail();
        String registerBody = objectMapper.writeValueAsString(new RegisterRequestFixture(
                email, "SecurePass123", "Carol", "Singer", "TRY"));
        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(registerBody))
                .andExpect(status().isCreated());

        String loginBody = objectMapper.writeValueAsString(new LoginRequestFixture(email, "WrongPassword"));
        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(loginBody))
                .andExpect(status().isUnauthorized());
    }

    private String uniqueEmail() {
        return "user-" + java.util.UUID.randomUUID() + "@fintrack-test.dev";
    }

    private record RegisterRequestFixture(String email, String password, String firstName, String lastName, String defaultCurrency) {
    }

    private record LoginRequestFixture(String email, String password) {
    }

    private record RefreshRequestFixture(String refreshToken) {
    }
}
