package com.Unimag.PulsePass.security;

import com.Unimag.PulsePass.domain.User;
import com.Unimag.PulsePass.domain.UserCredential;
import com.Unimag.PulsePass.repository.UserCredentialRepository;
import com.Unimag.PulsePass.repository.UserRepository;
import com.Unimag.PulsePass.repository.UserRoleRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserCredentialRepository credentials;
    private final UserRoleRepository roles;
    private final UserRepository users;

    public CustomUserDetailsService(UserCredentialRepository credentials, UserRoleRepository roles, UserRepository users) {
        this.credentials = credentials;
        this.roles = roles;
        this.users = users;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String identifier) {
        UserCredential credential = credentials.findByUser_UsernameIgnoreCaseOrUser_EmailIgnoreCase(identifier, identifier)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials."));
        return principal(credential.getUser(), credential.getPasswordHash());
    }

    @Transactional(readOnly = true)
    public SecurityPrincipal loadById(Long userId) {
        User user = users.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User is unavailable."));
        UserCredential credential = credentials.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User is unavailable."));
        return principal(user, credential.getPasswordHash());
    }

    private SecurityPrincipal principal(User user, String passwordHash) {
        List<String> currentRoles = roles.findByUser_Id(user.getId()).stream().map(role -> role.getId().getRole()).toList();
        return new SecurityPrincipal(user, passwordHash, currentRoles);
    }
}
