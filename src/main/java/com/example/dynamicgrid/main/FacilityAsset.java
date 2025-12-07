package com.example.dynamicgrid.main;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter@Setter
@Table(name = "facility_asset")
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

    // 민감 정보 (관리자만 접근)
    private BigDecimal purchaseCost;
    private String contractDetails;
}
