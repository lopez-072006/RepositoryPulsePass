package com.Unimag.PulsePass.controller;

import com.Unimag.PulsePass.dto.request.LoginRequest;
import com.Unimag.PulsePass.dto.response.AuthenticatedUserResponse;
import com.Unimag.PulsePass.dto.response.AuthResponse;
import com.Unimag.PulsePass.dto.response.ErrorResponse;
import com.Unimag.PulsePass.security.SecurityPrincipal;
import com.Unimag.PulsePass.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.HttpStatus;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try { return ResponseEntity.ok(authService.login(request)); }
        catch (AuthenticationException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(LocalDateTime.now(), 401,
                    "Unauthorized", "Credenciales inválidas", Map.of()));
        }
    }

    @GetMapping("/me")
    public AuthenticatedUserResponse me(Authentication authentication) {
        SecurityPrincipal principal = (SecurityPrincipal) authentication.getPrincipal();
        return new AuthenticatedUserResponse(principal.getUserId(), principal.getUsername(),
                principal.getEmail(), principal.getRoles());
    }
}
