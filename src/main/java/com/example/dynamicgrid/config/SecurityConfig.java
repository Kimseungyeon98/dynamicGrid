package com.example.dynamicgrid.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. [핵심 수정 부분] API 경로에 대해 CSRF 비활성화
            .csrf(csrf -> csrf.disable())

            // 2. 권한 설정 (추가 보안)
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/dynamicGrid").authenticated() // 메인 그리드는 로그인만 하면 접근
                    .requestMatchers("/admin/**").hasAnyRole("ADMIN", "MANAGER") // 관리자 페이지는 ADMIN/MANAGER만 접근 가능
                    .anyRequest().permitAll() // 나머지 요청 허용
            )

            // 3. 로그인 설정
            .formLogin(form -> form
                    .permitAll()
            )

            // 4. 로그아웃 설정
            .logout(logout -> logout
                    .logoutSuccessUrl("/dynamicGrid") // 로그아웃 성공 시 그리드 화면으로 리다이렉트
            );

        return http.build();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        // 관리자 (모든 컬럼 보임)
        UserDetails admin = User.withDefaultPasswordEncoder()
                .username("admin").password("1234").roles("ADMIN").build();

        // 일반 사용자 (비용, 계약내용 안보임)
        UserDetails user = User.withDefaultPasswordEncoder()
                .username("user").password("1234").roles("USER").build();

        return new InMemoryUserDetailsManager(admin, user);
    }
}