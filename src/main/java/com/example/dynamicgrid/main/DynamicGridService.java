package com.example.dynamicgrid.main;

import com.example.dynamicgrid.entity.FacilityAsset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DynamicGridService {
    @Autowired
    private DynamicGridRepository dynamicGridRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // 1. 모든 사용자가 볼 수 있는 기본 컬럼
    private static final List<String> COMMON_COLUMNS = Arrays.asList(
            "id", "assetCode", "assetName", "category", "location",
            "modelName", "manufacturer", "installDate", "status"
    );

    // 2. 관리자(ADMIN)만 볼 수 있는 민감 컬럼
    private static final List<String> ADMIN_ONLY_COLUMNS = Arrays.asList(
            "purchaseCost", "contractDetails"
    );

    /**
     * 권한에 따라 필터링된 데이터 반환
     */
    public List<Map<String, Object>> getFilteredAssets() {
        List<FacilityAsset> assets = dynamicGridRepository.findAll();
        boolean isAdmin = checkIsAdmin();

        return assets.stream().map(asset -> {
            // 1. Entity를 Map으로 변환
            Map<String, Object> map = objectMapper.convertValue(asset, Map.class);

            // 2. 권한이 없다면 민감한 키(컬럼) 삭제 (메모리상에서 제거됨)
            if (!isAdmin) {
                for (String secretCol : ADMIN_ONLY_COLUMNS) {
                    map.remove(secretCol);
                }
            }
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * 현재 로그인한 사용자가 볼 수 있는 '컬럼 리스트' 반환
     */
    public List<String> getAllowedColumns() {
        List<String> columns = new ArrayList<>(COMMON_COLUMNS);
        if (checkIsAdmin()) {
            columns.addAll(ADMIN_ONLY_COLUMNS);
        }
        return columns;
    }

    /**
     * 사용자 개인 설정 (숨김 처리할 컬럼) 가져오기 (DB 연동 대신 하드코딩 예시)
     */
    public List<String> getUserHiddenColumns() {
        // 실제로는 UserPreferenceRepository에서 사용자 ID로 조회해야 함
        // 예시: 사용자가 'modelName'과 'manufacturer'는 굳이 보고 싶어 하지 않음
        return Arrays.asList("modelName", "manufacturer");
    }

    // Security Context에서 권한 확인
    private boolean checkIsAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
