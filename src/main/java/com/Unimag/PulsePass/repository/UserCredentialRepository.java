package com.Unimag.PulsePass.repository;

import com.Unimag.PulsePass.domain.UserCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserCredentialRepository extends JpaRepository<UserCredential, Long> {
    Optional<UserCredential> findByUser_UsernameIgnoreCaseOrUser_EmailIgnoreCase(String username, String email);
}
