# Basisi
AI 데이터 분석 기반의 프리미엄 베이비시터 검증 및 매칭 플랫폼

[프리미엄 베이비시터 검증 플랫폼: 베시시(Basisi)]
팀원: 김준하(2021145020), 서석현(2021145037), 왕서빈(2022145049), 임다솔(2023145072), 이주영(2021145062)

1. 프로젝트 개요
"데이터로 검증된 안심, AI가 설계한 프리미엄 매칭으로 아이의 미소를 지켜주세요."
서비스 명: 국문 베시시 (아이의 환한 웃음 형상화) / 영문 Basisi (Babysitter System)

한 줄 요약: 베이비시터의 상세 이력과 활동 데이터를 바탕으로 부모가 직접 최적의 시터를 탐색·신청하고, AI가 분석한 **‘성장형 활동 온도(불꽃)’**와 비동기 데이터 파이프라인을 통해 매칭의 신뢰도와 생동감을 극대화한 프리미엄 베이비시터 검증 플랫폼.

2. 핵심 가치 (Core Values)
User-Centric Direct Matching: 시터의 전문 이력과 자격증을 부모가 직접 검토하는 자율 선택권 보장 및 AI 추천을 통한 탐색 효율 극대화.
Active Warmth (활동의 온기): 수직적 티어제 대신 활동 횟수와 긍정 피드백 기반의 '성장형 불꽃' 효과를 부여하여 시터의 열정과 최신 활동성을 직관적으로 증명.
Seamless Reliability: 인기 시터 예약 집중 시 발생하는 데이터 충돌을 Java Redisson 기반 분산 락으로 해결하여 중복 없는 안정적인 예약 시스템 구축.
Proactive Insight: 서비스 종료 후 AI가 아이의 성장 지표와 활동 내역을 요약한 성장 리포트를 자동 발송하여 프리미엄 케어 서비스 가치 창출.

3. 주요 타겟 (Target Audience)
베이비시터: 신원이 불분명한 플랫폼에 불안감을 느끼며, 데이터로 검증된 고숙련 전문 시터를 찾는 맞벌이 부부.
부모: 전문성을 입증하여 정당한 처우를 받고자 하는 전문 베이비시터 및 보육 경력자.

4. 백엔드 차별화 포인트 (Backend Differentiation)
고성능 검색 및 위치 최적화: PostGIS와 다중 조건 복합 필터 인덱싱을 활용한 실시간 위치 기반 쿼리 최적화.
안정적인 동시성 제어: Redis Distributed Lock을 적용하여 예약/결제 시 레이스 컨디션(Race Condition)을 원천 차단하고 정합성 100% 확보.
비동기 AI 분석 파이프라인: Kafka/RabbitMQ 메시지 큐를 도입하여 리뷰 분석 및 리포트 생성을 비동기 처리함으로써 시스템 가용성 극대화.

5. 팀원별 백엔드 역할 (R&R)
🛡️ 이주영 (인프라 및 보안 Specialist)
핵심 기술: Spring Security, JWT, Docker, AWS, nGrinder
주요 작업: [Phase 1] 초기 환경 세팅, JWT 인증/인가 시스템 및 로그인 API / [Phase 4] AWS 배포 및 nGrinder 부하 테스트.

🔍 김준하 (데이터 설계 및 고속 검색 Specialist)
핵심 기술: JPA, QueryDSL, PostgreSQL, PostGIS
주요 작업: [Phase 1] 도메인 모델(Entity) 설계 및 CRUD API / [Phase 2] PostGIS 기반 위치 검색 및 QueryDSL 복합 필터 구현.

⚡ 임다솔 (비즈니스 로직 및 동시성 Specialist)
핵심 기술: Spring Transaction, Redis, Redisson
주요 작업: [Phase 2] 플랫폼 핵심 예약 워크플로우 구현 / [Phase 3] Redisson 분산 락을 통한 중복 예약 방지 및 정합성 보장.

🤖 서석현 (AI 및 메시징 파이프라인 Specialist)
핵심 기술: Kafka/RabbitMQ, LLM API, WebClient
주요 작업: [Phase 3] Kafka 메시지 브로커 구축, 비동기 파이프라인 설계 및 AI 기반 '활동 온도(불꽃)' 알고리즘 구현.

📢 왕서빈 (통찰 제공 및 실시간 알림 Specialist)
핵심 기술: SSE (Server-Sent Events), AI Prompting, JPA
주요 작업: [Phase 2] 리뷰 및 평점 시스템 / [Phase 3] 맞춤형 '아이 성장 리포트' 생성 / [Phase 4] SSE 기반 실시간 알림 고도화.


6. 개발 주요 일정 (15주차 계획)
[1단계] 핵심 기능 및 UI 골격 완성 (3주 ~ 8주)
3~4주차 (Phase 1): 백엔드 기초(Security, CRUD) 구축 및 프론트엔드 디자인 시스템(Soft Sky Blue, Warm Coral)·인증 페이지 구현.
5~7주차 (Phase 2): PostGIS 기반 지도 검색 및 예약 신청 로직 구현. 프론트엔드 지도 연동 및 애니메이션 효과 적용.
8주차 (중간 발표): 부모 로그인 → 지도 탐색 → 이력 확인 → 예약 신청으로 이어지는 풀 프로세스 시연.

[2단계] 기술 고도화 및 AI 지능형 서비스 (9주 ~ 15주)
9~11주차 (Phase 3): Redis 분산 락 적용 및 Kafka 기반 AI 비동기 파이프라인 구축. AI 추천 채팅 UI 구현.
12~13주차 (Phase 3): '역동적인 불꽃 애니메이션' 시각화 및 AI 성장 리포트 대시보드 완성.
14주차 (Phase 4): nGrinder 부하 테스트 및 SSE 실시간 알림 전송 최적화.
15주차 (최종 발표): 동시성 방지 테스트, AI 성장 리포트 및 활동 온도(불꽃) 변화 시연을 통한 기술적 완성도 증명.
