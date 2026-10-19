package com.Unimag.PulsePass.controller;

import com.Unimag.PulsePass.dto.request.RegisterUserRequest;
import com.Unimag.PulsePass.dto.response.UserResponse;
import com.Unimag.PulsePass.service.AuthService;
import com.Unimag.PulsePass.service.UserService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

  
    @PostMapping
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterUserRequest request) {

        UserResponse registeredUser = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(registeredUser);
    }

  
    @GetMapping("/by-email")
    public ResponseEntity<UserResponse> findByEmail(
            @RequestParam String email) {

        return ResponseEntity.ok(
                userService.findByEmail(email)
        );
    }

   
    @GetMapping("/by-username")
    public ResponseEntity<UserResponse> findByUsername(
            @RequestParam String username) {

        return ResponseEntity.ok(
                userService.findByUsername(username)
        );
    }
}
