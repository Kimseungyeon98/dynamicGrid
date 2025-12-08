package com.example.dynamicgrid.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;

    // Role과는 관계 매핑 (EAGER로 설정하여 로그인시 바로 권한 로딩)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_name")
    private Role role;
}