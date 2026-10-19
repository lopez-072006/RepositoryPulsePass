package com.Unimag.PulsePass.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String identifier, @NotBlank String password) {
    @Override public String toString() { return "LoginRequest[credentials=REDACTED]"; }
}
