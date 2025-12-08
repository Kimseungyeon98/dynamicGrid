package com.example.dynamicgrid.entity;

import com.example.dynamicgrid.converter.StringListJsonConverter;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "grid_role_config",
        uniqueConstraints = @UniqueConstraint(columnNames = {"grid_code", "role_name"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class GridRoleConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // GridMaster와 연관관계
    @ManyToOne
    @JoinColumn(name = "grid_code")
    private GridMaster gridMaster;

    // Role과 연관관계
    @ManyToOne
    @JoinColumn(name = "role_name")
    private Role role;

    // JSONB 컬럼 -> List<String> 자동 변환
    // ["purchaseCost", "contractDetails"]
    @Convert(converter = StringListJsonConverter.class)
    @Column(columnDefinition = "jsonb")
    private List<String> invisibleColumnsJson;
}