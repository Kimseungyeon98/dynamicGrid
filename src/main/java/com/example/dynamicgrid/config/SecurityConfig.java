package com.example.dynamicgrid.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .formLogin(form -> form.defaultSuccessUrl("/", true));
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