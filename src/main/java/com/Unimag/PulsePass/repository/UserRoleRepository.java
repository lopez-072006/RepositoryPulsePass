package com.Unimag.PulsePass.repository;

import com.Unimag.PulsePass.domain.UserRole;
import com.Unimag.PulsePass.domain.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
    List<UserRole> findByUser_Id(Long userId);
}
