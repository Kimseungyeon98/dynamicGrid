package com.example.dynamicgrid.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode; // [핵심 Import 1]
import org.hibernate.type.SqlTypes;            // [핵심 Import 2]

import java.util.List;

@Entity
@Table(name = "grid_role_config",
        uniqueConstraints = @UniqueConstraint(columnNames = {"grid_code", "role_name"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class GridRoleConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "grid_code")
    private GridMaster gridMaster;

    @ManyToOne
    @JoinColumn(name = "role_name")
    private Role role;

    // [수정 전]
    // @Convert(converter = StringListJsonConverter.class) <-- 이거 지우세요! (문자열로 보내서 에러남)

    // [수정 후] Hibernate 6의 Native JSON 기능 사용
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "invisible_columns_json", columnDefinition = "jsonb")
    private List<String> invisibleColumnsJson;
}