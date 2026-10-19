package com.Unimag.PulsePass.security;

import com.Unimag.PulsePass.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

public class SecurityPrincipal implements UserDetails {
    private final Long userId;
    private final String username;
    private final String email;
    private final String passwordHash;
    private final boolean active;
    private final List<GrantedAuthority> authorities;

    public SecurityPrincipal(User user, String passwordHash, List<String> roles) {
        this.userId = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.passwordHash = passwordHash;
        this.active = Boolean.TRUE.equals(user.getActive());
        this.authorities = roles.stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role)).toList();
    }

    public Long getUserId() { return userId; }
    public String getEmail() { return email; }
    public List<String> getRoles() {
        return authorities.stream().map(GrantedAuthority::getAuthority).map(role -> role.substring(5)).toList();
    }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return passwordHash; }
    @Override public String getUsername() { return username; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return active; }
}
