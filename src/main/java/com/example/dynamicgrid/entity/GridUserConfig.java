package com.example.dynamicgrid.entity;

import com.example.dynamicgrid.converter.MapJsonConverter;
import jakarta.persistence.*;
import lombok.*;
import java.util.Map;

@Entity
@Table(name = "grid_user_config",
        uniqueConstraints = @UniqueConstraint(columnNames = {"grid_code", "user_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class GridUserConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "grid_code")
    private GridMaster gridMaster;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // JSONB 컬럼 -> Map<String, Object> 자동 변환
    // {"hidden": ["id"], "width": {"name": 200}}
    @Convert(converter = MapJsonConverter.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> configJson;
}