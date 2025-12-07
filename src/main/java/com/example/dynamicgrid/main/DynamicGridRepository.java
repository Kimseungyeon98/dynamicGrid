package com.example.dynamicgrid.main;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DynamicGridRepository extends JpaRepository<FacilityAsset, Long> {
}
