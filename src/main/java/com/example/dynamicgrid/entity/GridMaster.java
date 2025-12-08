package com.example.dynamicgrid.entity;

import com.example.dynamicgrid.converter.MapListJsonConverter;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "grid_master")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class GridMaster {

    @Id
    @Column(name = "grid_code")
    private String gridCode; // 예: FACILITY_ASSET_GRID

    private String gridName;

    // JSONB 컬럼 -> List<Map> 자동 변환
    // [{"field": "id", ...}, ...]
    @Convert(converter = MapListJsonConverter.class)
    @Column(columnDefinition = "jsonb")
    private List<Map<String, Object>> defaultColumnsJson;
}