# syntax=docker/dockerfile:1
# 1단계: Gradle + JDK17 로 JAR 빌드
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /workspace

# Gradle Wrapper와 빌드 스크립트만 먼저 복사해 캐시 활용
COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true

# 나머지 소스 복사 후 bootJar 빌드
COPY src src
RUN ./gradlew --no-daemon clean bootJar -x test

# 2단계: JRE만 담은 가벼운 런타임 이미지
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# 빌드된 단일 JAR을 복사
COPY --from=build /workspace/build/libs/*.jar /app/app.jar

# Render가 주입하는 PORT 환경변수를 그대로 사용 (application-h2.yml의 ${PORT:8080})
ENV JAVA_OPTS=""
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
