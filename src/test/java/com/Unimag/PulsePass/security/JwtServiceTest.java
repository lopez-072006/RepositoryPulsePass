package com.Unimag.PulsePass.security;

import com.Unimag.PulsePass.domain.User;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import java.util.Base64;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {
    private static final String SECRET = Base64.getEncoder().encodeToString(new byte[32]);

    @Test
    void issuesAndParsesHs256TokenForPersistentSubject() {
        JwtService service = service("pulsepass-api");
        User user = new User();
        user.setId(42L);
        user.setUsername("andrea");
        user.setEmail("andrea@example.com");
        user.setActive(true);
        SecurityPrincipal principal = new SecurityPrincipal(user, "hash", List.of("USER"));

        var response = service.issueToken(principal);

        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(900);
        assertThat(service.parseUserId(response.accessToken())).isEqualTo(42L);
        assertThat(response.toString()).doesNotContain(response.accessToken());
    }

    @Test
    void rejectsTamperedAndWrongIssuerTokens() {
        JwtService issuer = service("pulsepass-api");
        User user = new User();
        user.setId(42L);
        user.setActive(true);
        var token = issuer.issueToken(new SecurityPrincipal(user, "hash", List.of("USER"))).accessToken();
        String tampered = token.substring(0, token.length() - 1) + (token.endsWith("a") ? "b" : "a");

        assertThatThrownBy(() -> issuer.parseUserId(tampered)).isInstanceOf(JwtException.class);
        assertThatThrownBy(() -> service("different-issuer").parseUserId(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void refusesMissingOrWeakSecret() {
        JwtProperties missing = new JwtProperties();
        assertThatThrownBy(() -> new JwtService(missing)).isInstanceOf(IllegalStateException.class);

        JwtProperties weak = new JwtProperties();
        weak.setSecretBase64(Base64.getEncoder().encodeToString(new byte[16]));
        assertThatThrownBy(() -> new JwtService(weak)).isInstanceOf(IllegalStateException.class);
    }

    private JwtService service(String issuer) {
        JwtProperties properties = new JwtProperties();
        properties.setIssuer(issuer);
        properties.setSecretBase64(SECRET);
        return new JwtService(properties);
    }
}
