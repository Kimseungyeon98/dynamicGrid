package com.example.dynamicgrid.main;

import com.example.dynamicgrid.dto.RoleConfigReq;
import com.example.dynamicgrid.dto.UserConfigReq;
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
    private final RoleRepository roleRepository; // Role 조회용 추가 필요

    private final ObjectMapper objectMapper;

    // ==========================================
    // [New] 권한 관리용: 모든 롤 목록 가져오기
    // ==========================================
    public List<String> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toList());
    }

    // ==========================================
    // [New] 권한 관리용: 특정 롤의 현재 설정 가져오기 (보이는 컬럼 반환)
    // ==========================================
    public List<String> getVisibleColumnsForRole(String gridCode, String targetRoleName) {
        // 1. 전체 컬럼 가져오기
        GridMaster gridMaster = gridMasterRepository.findById(gridCode)
                .orElseThrow(() -> new RuntimeException("Grid not found"));

        List<String> allFields = gridMaster.getDefaultColumnsJson().stream()
                .map(col -> (String) col.get("field"))
                .collect(Collectors.toList());

        // 2. 숨겨진 컬럼 가져오기
        List<String> invisibleFields = gridRoleConfigRepository
                .findByGridMaster_GridCodeAndRole_RoleName(gridCode, targetRoleName)
                .map(GridRoleConfig::getInvisibleColumnsJson)
                .orElse(Collections.emptyList());

        // 3. 전체 - 숨김 = 보이는 컬럼
        return allFields.stream()
                .filter(field -> !invisibleFields.contains(field))
                .collect(Collectors.toList());
    }

    // ==========================================
    // [New] 권한 설정 저장 (핵심 로직)
    // ==========================================
    @Transactional // 쓰기 작업이므로 필수
    public void updateRoleConfig(RoleConfigReq req) {
        // 1. 보안 체크: 누가 누구를 수정하려 하는가?
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        User currentUser = userRepository.findByUsername(currentUsername).orElseThrow();
        String currentRole = currentUser.getRole().getRoleName();

        // [규칙] MANAGER는 ADMIN의 권한을 수정할 수 없음
        if ("ROLE_MANAGER".equals(currentRole) && "ROLE_ADMIN".equals(req.getTargetRoleName())) {
            throw new RuntimeException("접근 거부: 담당자는 관리자 권한을 수정할 수 없습니다.");
        }

        // [규칙] 일반 USER는 이 API 접근 불가 (Controller에서도 막겠지만 2중 체크)
        if ("ROLE_USER".equals(currentRole)) {
            throw new RuntimeException("접근 거부: 권한이 없습니다.");
        }

        // 2. 전체 컬럼 목록 조회
        GridMaster gridMaster = gridMasterRepository.findById(req.getGridCode()).orElseThrow();
        List<String> allFields = gridMaster.getDefaultColumnsJson().stream()
                .map(col -> (String) col.get("field"))
                .collect(Collectors.toList());

        // 3. 로직 변환: (전체 컬럼) - (프론트에서 온 보이는 컬럼) = (DB에 저장할 숨길 컬럼)
        List<String> columnsToHide = allFields.stream()
                .filter(field -> !req.getVisibleColumns().contains(field))
                .collect(Collectors.toList());

        // 4. DB 저장 (Update or Insert)
        GridRoleConfig config = gridRoleConfigRepository
                .findByGridMaster_GridCodeAndRole_RoleName(req.getGridCode(), req.getTargetRoleName())
                .orElseGet(() -> {
                    // 없으면 새로 생성
                    GridRoleConfig newConfig = new GridRoleConfig();
                    newConfig.setGridMaster(gridMaster);
                    newConfig.setRole(roleRepository.findById(req.getTargetRoleName()).orElseThrow());
                    return newConfig;
                });

        config.setInvisibleColumnsJson(columnsToHide);
        gridRoleConfigRepository.save(config);
    }

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

    // [New] 사용자 개인 설정 저장 (Auto-Save)
    @Transactional
    public void saveUserConfig(String username, UserConfigReq req) {
        // 1. 사용자 조회
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. 그리드 마스터 조회
        GridMaster gridMaster = gridMasterRepository.findById(req.getGridCode())
                .orElseThrow(() -> new RuntimeException("Grid not found"));

        // 3. 기존 설정 가져오기 (없으면 새로 생성)
        GridUserConfig userConfig = gridUserConfigRepository
                .findByGridMaster_GridCodeAndUser_Username(req.getGridCode(), username)
                .orElseGet(() -> {
                    GridUserConfig newConfig = new GridUserConfig();
                    newConfig.setGridMaster(gridMaster);
                    newConfig.setUser(user);
                    newConfig.setConfigJson(new HashMap<>()); // 빈 맵으로 초기화
                    return newConfig;
                });

        // 4. JSON 데이터 업데이트 (기존 너비 설정 등은 유지하고 hiddenColumns만 교체)
        Map<String, Object> currentJson = userConfig.getConfigJson();
        if (currentJson == null) {
            currentJson = new HashMap<>();
        }

        // "hiddenColumns" 키의 값을 교체
        currentJson.put("hiddenColumns", req.getHiddenColumns());

        // 5. 엔티티에 다시 세팅 (Dirty Checking에 의해 자동 저장되지만 명시적으로 set)
        userConfig.setConfigJson(currentJson);

        // (신규 생성일 경우를 위해 save 호출)
        gridUserConfigRepository.save(userConfig);
    }

}