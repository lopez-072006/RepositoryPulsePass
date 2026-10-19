package com.Unimag.PulsePass.service;

import com.Unimag.PulsePass.domain.User;
import com.Unimag.PulsePass.domain.UserCredential;
import com.Unimag.PulsePass.domain.UserRole;
import com.Unimag.PulsePass.dto.request.LoginRequest;
import com.Unimag.PulsePass.dto.request.RegisterUserRequest;
import com.Unimag.PulsePass.dto.response.AuthResponse;
import com.Unimag.PulsePass.dto.response.UserResponse;
import com.Unimag.PulsePass.exception.InvalidPasswordException;
import com.Unimag.PulsePass.repository.UserCredentialRepository;
import com.Unimag.PulsePass.repository.UserRepository;
import com.Unimag.PulsePass.repository.UserRoleRepository;
import com.Unimag.PulsePass.security.JwtService;
import com.Unimag.PulsePass.security.SecurityPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final UserRepository users;
    private final UserCredentialRepository credentials;
    private final UserRoleRepository roles;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService,
                       PasswordEncoder passwordEncoder, UserService userService, UserRepository users,
                       UserCredentialRepository credentials, UserRoleRepository roles) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.users = users;
        this.credentials = credentials;
        this.roles = roles;
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.identifier(), request.password()));
        return jwtService.issueToken((SecurityPrincipal) authentication.getPrincipal());
    }

    @Transactional
    public UserResponse register(RegisterUserRequest request) {
        if (request.password() == null) throw new InvalidPasswordException("Password is required.");
        int passwordBytes = request.password().getBytes(StandardCharsets.UTF_8).length;
        if (passwordBytes < 12 || passwordBytes > 72)
            throw new InvalidPasswordException("Password must contain at least 12 and at most 72 UTF-8 bytes.");

        UserResponse response = userService.register(request);
        User user = users.findById(response.id()).orElseThrow();
        credentials.save(new UserCredential(user, passwordEncoder.encode(request.password())));
        roles.save(new UserRole(user, "USER"));
        return response;
    }
}
