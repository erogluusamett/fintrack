package com.fintrack.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenHasherTest {

    @Test
    void hash_isDeterministic_forTheSameInput() {
        String raw = "some-raw-token-value";

        assertThat(TokenHasher.hash(raw)).isEqualTo(TokenHasher.hash(raw));
    }

    @Test
    void hash_differsForDifferentInputs() {
        assertThat(TokenHasher.hash("token-a")).isNotEqualTo(TokenHasher.hash("token-b"));
    }

    @Test
    void hash_neverReturnsTheRawInput() {
        String raw = "sensitive-refresh-token";

        assertThat(TokenHasher.hash(raw)).isNotEqualTo(raw);
    }

    @Test
    void generateRawToken_producesUniqueValuesEachTime() {
        assertThat(TokenHasher.generateRawToken()).isNotEqualTo(TokenHasher.generateRawToken());
    }
}
