package com.Unimag.PulsePass.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class UserRoleId implements Serializable {
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "role", length = 20)
    private String role;

    protected UserRoleId() { }
    public UserRoleId(Long userId, String role) { this.userId = userId; this.role = role; }
    public Long getUserId() { return userId; }
    public String getRole() { return role; }

    @Override public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof UserRoleId that)) return false;
        return Objects.equals(userId, that.userId) && Objects.equals(role, that.role);
    }
    @Override public int hashCode() { return Objects.hash(userId, role); }
}
