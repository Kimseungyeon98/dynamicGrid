package com.example.dynamicgrid.repository;

import com.example.dynamicgrid.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// 1. User
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}