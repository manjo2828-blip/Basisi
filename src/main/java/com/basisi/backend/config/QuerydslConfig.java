package com.basisi.backend.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// QueryDSL 쿼리 팩토리 빈을 등록하는 설정입니다.
@Configuration
public class QuerydslConfig {

    // JPA EntityManager를 주입받습니다.
    @PersistenceContext
    private EntityManager entityManager;

    // QueryDSL JPAQueryFactory 빈을 등록합니다.
    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        // EntityManager 기반 QueryFactory를 반환합니다.
        return new JPAQueryFactory(entityManager);
    }
}
