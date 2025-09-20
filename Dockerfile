FROM openjdk:11-jre-slim

# 작업 디렉토리 설정
WORKDIR /app

# JAR 파일만 복사 (target/*.jar)
COPY target/*.jar app.jar

# 포트 8080 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]