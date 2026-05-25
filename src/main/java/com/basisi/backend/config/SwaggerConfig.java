package com.basisi.backend.config;

// OpenAPI 문서 메타데이터 객체입니다.
import io.swagger.v3.oas.models.OpenAPI;
// OpenAPI 보안 요구사항 객체입니다.
import io.swagger.v3.oas.models.security.SecurityRequirement;
// OpenAPI 보안 스키마 객체입니다.
import io.swagger.v3.oas.models.security.SecurityScheme;
// OpenAPI 문서의 서비스 정보를 담는 객체입니다.
import io.swagger.v3.oas.models.info.Info;
// 스프링 빈 등록을 위한 어노테이션입니다.
import org.springframework.context.annotation.Bean;
// 설정 클래스를 선언하는 어노테이션입니다.
import org.springframework.context.annotation.Configuration;

// Swagger(OpenAPI) 문서의 기본 정보를 설정하는 클래스입니다.
@Configuration
public class SwaggerConfig {

    // OpenAPI 문서에 표시할 서비스 정보를 빈으로 등록합니다.
    @Bean
    public OpenAPI basisiOpenAPI() {
        // 문서 제목/버전/설명을 설정한 OpenAPI 객체를 생성합니다.
        return new OpenAPI()
                // 전역 보안 요구사항으로 Bearer 인증을 등록합니다.
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                // 컴포넌트에 Bearer 토큰 스키마를 등록합니다.
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                // HTTP 인증 타입을 지정합니다.
                                .type(SecurityScheme.Type.HTTP)
                                // Bearer 스킴을 사용합니다.
                                .scheme("bearer")
                                // JWT 형식을 명시합니다.
                                .bearerFormat("JWT")))
                .info(new Info()
                        // 문서 제목을 설정합니다.
                        .title("Basisi Backend API")
                        // 문서 버전을 설정합니다.
                        .version("v1")
                        // 문서 설명을 설정합니다.
                        .description("베시시(Basisi) 백엔드 API 문서"));
    }
}
