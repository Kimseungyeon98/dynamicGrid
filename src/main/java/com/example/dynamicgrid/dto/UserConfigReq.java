package com.example.dynamicgrid.dto;

import lombok.Data;
import java.util.List;

@Data
public class UserConfigReq {
    private String gridCode;         // 어떤 그리드인지 (예: FACILITY_ASSET_GRID)
    private List<String> hiddenColumns; // 사용자가 숨긴 컬럼 목록
}