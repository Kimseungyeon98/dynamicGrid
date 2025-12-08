package com.example.dynamicgrid.repository;

import com.example.dynamicgrid.entity.GridUserConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// 5. Grid User Config (사용자 설정 조회용)
public interface GridUserConfigRepository extends JpaRepository<GridUserConfig, Long> {
    Optional<GridUserConfig> findByGridMaster_GridCodeAndUser_Username(String gridCode, String username);
}
