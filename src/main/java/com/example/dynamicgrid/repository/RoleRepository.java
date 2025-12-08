package com.example.dynamicgrid.repository;

import com.example.dynamicgrid.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

// 2. Role (필요시)
public interface RoleRepository extends JpaRepository<Role, String> { }
