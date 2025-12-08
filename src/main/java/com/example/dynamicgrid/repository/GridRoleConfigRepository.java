package com.example.dynamicgrid.repository;

import com.example.dynamicgrid.entity.GridRoleConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// 4. Grid Role Config (권한 설정 조회용)
public interface GridRoleConfigRepository extends JpaRepository<GridRoleConfig, Long> {
    Optional<GridRoleConfig> findByGridMaster_GridCodeAndRole_RoleName(String gridCode, String roleName);
}
