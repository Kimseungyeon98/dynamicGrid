package com.example.dynamicgrid.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "facility_asset")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class FacilityAsset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String assetCode;
    private String assetName;
    private String category;
    private String location;
    private String modelName;
    private String manufacturer;
    private LocalDate installDate;
    private String status;

    // 민감 정보
    private BigDecimal purchaseCost;
    private String contractDetails;
}