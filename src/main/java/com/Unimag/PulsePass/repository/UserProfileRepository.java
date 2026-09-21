package com.Unimag.PulsePass.repository;

import com.Unimag.PulsePass.domain.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

}