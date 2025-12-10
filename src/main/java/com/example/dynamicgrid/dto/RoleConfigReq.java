package com.example.dynamicgrid.dto;

import lombok.Data;
import java.util.List;

@Data
public class RoleConfigReq {
    private String gridCode;       // 예: FACILITY_ASSET_GRID
    private String targetRoleName; // 설정하려는 대상 권한 (예: ROLE_USER)
    private List<String> visibleColumns; // 화면에서 체크된(보여줄) 컬럼 리스트
}