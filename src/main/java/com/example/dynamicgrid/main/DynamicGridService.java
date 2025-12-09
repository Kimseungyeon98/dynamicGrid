package com.example.dynamicgrid.main;

import com.example.dynamicgrid.entity.*;
import com.example.dynamicgrid.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DynamicGridService {

    private final FacilityAssetRepository assetRepository;
    private final GridMasterRepository gridMasterRepository;
    private final GridRoleConfigRepository gridRoleConfigRepository;
    private final GridUserConfigRepository gridUserConfigRepository;
    private final UserRepository userRepository;

    private final ObjectMapper objectMapper;

    /**
     * 그리드 초기화에 필요한 모든 데이터(Data + Meta + Config)를 묶어서 반환
     */
    public Map<String, Object> getGridContext(String gridCode) {
        // 1. 현재 로그인한 사용자 및 권한 확인
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        // DB에서 사용자 조회 (Role 정보를 얻기 위해)
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        String roleName = user.getRole().getRoleName();

        // 2. 그리드 마스터 정보 조회 (전체 컬럼 정의)
        GridMaster gridMaster = gridMasterRepository.findById(gridCode)
                .orElseThrow(() -> new RuntimeException("Grid definition not found: " + gridCode));

        List<Map<String, Object>> allColumns = new ArrayList<>(gridMaster.getDefaultColumnsJson());

        // 3. [보안] 권한별 숨김 컬럼 조회 (GridRoleConfig)
        List<String> invisibleColumns = gridRoleConfigRepository
                .findByGridMaster_GridCodeAndRole_RoleName(gridCode, roleName)
                .map(GridRoleConfig::getInvisibleColumnsJson)
                .orElse(Collections.emptyList());

        // 4. [보안] 렌더링할 컬럼 리스트 필터링 (Invisible 컬럼은 헤더 정의에서도 제거)
        // 화면에 아예 그리지 않기 위함
        List<Map<String, Object>> allowedColumns = allColumns.stream()
                .filter(colDef -> !invisibleColumns.contains((String) colDef.get("field")))
                .collect(Collectors.toList());

        // 5. [편의] 사용자 개인 설정 조회 (GridUserConfig)
        Map<String, Object> userConfig = gridUserConfigRepository
                .findByGridMaster_GridCodeAndUser_Username(gridCode, username)
                .map(GridUserConfig::getConfigJson)
                .orElse(Collections.emptyMap()); // 설정 없으면 빈 객체

        // 6. 실제 데이터 조회 및 보안 필터링
        List<FacilityAsset> assets = assetRepository.findAll();
        List<Map<String, Object>> filteredData = assets.stream().map(asset -> {
            // Entity -> Map 변환
            Map<String, Object> map = objectMapper.convertValue(asset, Map.class);

            // [중요] 권한 없는 컬럼 데이터 삭제 (네트워크 전송 자체를 막음)
            invisibleColumns.forEach(map::remove);

            return map;
        }).collect(Collectors.toList());

        // 7. 결과 맵핑
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("gridName", gridMaster.getGridName());     // 그리드 제목
        resultMap.put("columnDefs", allowedColumns);             // 렌더링할 컬럼 정의 (헤더용)
        resultMap.put("tableData", filteredData);                // 실제 데이터 (필터링됨)
        resultMap.put("userConfig", userConfig);                 // 사용자 UI 설정 (숨김, 너비 등)

        return resultMap;
    }
}