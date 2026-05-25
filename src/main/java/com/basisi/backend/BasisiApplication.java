package com.basisi.backend;

// Spring Boot 애플리케이션 실행을 위한 기본 어노테이션입니다.
import org.springframework.boot.SpringApplication;
// 자동 설정 및 컴포넌트 스캔을 활성화합니다.
import org.springframework.boot.autoconfigure.SpringBootApplication;

// 애플리케이션의 시작점을 정의합니다.
@SpringBootApplication
public class BasisiApplication {

    // 프로그램 시작 시 가장 먼저 호출되는 메인 메서드입니다.
    public static void main(String[] args) {
        // Spring Boot 애플리케이션을 실행합니다.
        SpringApplication.run(BasisiApplication.class, args);
    }
}
