package com.example.dynamicgrid.entity;

import com.example.dynamicgrid.converter.MapListJsonConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
    // @Convert(converter = MapListJsonConverter.class) <-- 제거
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<Map<String, Object>> defaultColumnsJson;
}