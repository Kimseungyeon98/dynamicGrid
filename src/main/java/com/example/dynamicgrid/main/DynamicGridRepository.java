package com.example.dynamicgrid.main;

import com.example.dynamicgrid.entity.FacilityAsset;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DynamicGridRepository extends JpaRepository<FacilityAsset, Long> {
}
