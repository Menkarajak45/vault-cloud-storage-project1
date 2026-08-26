package com.cloudstorage.repository;

import com.cloudstorage.model.Role;
import com.cloudstorage.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByResetToken(String resetToken);
    boolean existsByRole(Role role);
    long countByRole(Role role);
}
