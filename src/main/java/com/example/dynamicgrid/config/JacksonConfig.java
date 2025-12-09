package com.example.dynamicgrid.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary // 여러 ObjectMapper가 있을 경우 이것을 최우선으로 사용
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // 1. Java 8 날짜/시간(LocalDate, LocalDateTime) 처리를 위한 모듈 등록
        objectMapper.registerModule(new JavaTimeModule());

        // 2. 날짜를 배열([2023, 12, 31])이 아닌 문자열("2023-12-31")로 직렬화
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return objectMapper;
    }
}