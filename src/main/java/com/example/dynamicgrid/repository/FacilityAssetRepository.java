package com.example.dynamicgrid.repository;

import com.example.dynamicgrid.entity.FacilityAsset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// 6. Facility Asset
public interface FacilityAssetRepository extends JpaRepository<FacilityAsset, Long> {
    // [New] 검색어(keyword)가 포함된 자산명 or 모델명 조회 + 페이징
    // LOWER()를 사용하여 대소문자 구분 없이 검색
    @Query("SELECT f FROM FacilityAsset f WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(f.assetName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.modelName) LIKE LOWER(CONCAT('%', :keyword, '%'))) ")
    Page<FacilityAsset> searchAssets(@Param("keyword") String keyword, Pageable pageable);
}
