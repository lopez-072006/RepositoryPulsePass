package com.Unimag.PulsePass.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(

        @NotBlank(message = "Username is required")
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        String phone,

        String city,

        @NotNull(message = "Birth date is required")
        LocalDate birthDate,

        @NotBlank(message = "Password is required")
        @Size(min = 12, message = "Password must contain at least 12 characters")
        String password

) {
    // Keeps existing service clients source-compatible; the public registration endpoint requires it.
    public RegisterUserRequest(String username, String email, String firstName, String lastName,
                               String phone, String city, LocalDate birthDate) {
        this(username, email, firstName, lastName, phone, city, birthDate, null);
    }

    @Override public String toString() {
        return "RegisterUserRequest[username=" + username + ", email=" + email + ", password=REDACTED]";
    }
}
