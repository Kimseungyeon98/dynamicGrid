package com.example.dynamicgrid.repository;

import com.example.dynamicgrid.entity.FacilityAsset;
import org.springframework.data.jpa.repository.JpaRepository;

// 6. Facility Asset
public interface FacilityAssetRepository extends JpaRepository<FacilityAsset, Long> { }
