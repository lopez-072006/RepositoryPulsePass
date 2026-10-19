package com.Unimag.PulsePass.dto.response;

public record AuthResponse(String accessToken, String tokenType, long expiresIn) {
    @Override public String toString() {
        return "AuthResponse[accessToken=REDACTED, tokenType=" + tokenType + ", expiresIn=" + expiresIn + "]";
    }
}
