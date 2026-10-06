package com.hostel.management.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "a-test-signing-secret-with-at-least-32-bytes";

    @Test
    void createsAndParsesRoleBoundToken() {
        JwtService jwtService = new JwtService(SECRET, 3600);

        var claims = jwtService.parseToken(jwtService.createToken("26215A0535", "STUDENT"));

        assertThat(claims.getSubject()).isEqualTo("26215A0535");
        assertThat(claims.get("role", String.class)).isEqualTo("STUDENT");
    }

    @Test
    void rejectsShortSigningSecret() {
        assertThatThrownBy(() -> new JwtService("too-short", 3600))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32 bytes");
    }

    @Test
    void rejectsTokenSignedByDifferentSecret() {
        JwtService issuer = new JwtService(SECRET, 3600);
        JwtService verifier = new JwtService("another-signing-secret-that-is-over-32-bytes", 3600);

        assertThatThrownBy(() -> verifier.parseToken(issuer.createToken("admin", "ADMIN")))
                .isInstanceOf(io.jsonwebtoken.JwtException.class);
    }
}
