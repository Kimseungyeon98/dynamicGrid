package com.example.dynamicgrid.repository;

import com.example.dynamicgrid.entity.GridMaster;
import org.springframework.data.jpa.repository.JpaRepository;

// 3. Grid Master
public interface GridMasterRepository extends JpaRepository<GridMaster, String> { }
