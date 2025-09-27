# ===== Stage 1: Build =====
FROM gradle:8.9-jdk17 AS builder
WORKDIR /src
COPY build.gradle settings.gradle gradlew gradlew.bat ./
COPY gradle gradle
COPY src src
COPY lib lib
COPY data data
COPY Config.properties maintenanceConfig.txt manifest.mf ./
RUN chmod +x gradlew && ./gradlew --no-daemon shadowJar

# ===== Stage 2: Runtime =====
FROM openjdk:17-jdk-slim
WORKDIR /app

# Sao chép artifact và gradle wrapper
COPY --from=builder /src/build/libs/NgocRongOnline-all.jar /app/app.jar
COPY --from=builder /src/gradlew /app/gradlew
COPY --from=builder /src/gradle /app/gradle
COPY --from=builder /src/build.gradle /src/settings.gradle /app/

# Tạo user 1000:1000 có home để chạy non-root
RUN useradd -u 1000 -m appuser && \
    chown -R appuser:appuser /app && \
    chmod +x /app/gradlew
USER appuser:appuser

ENV JAVA_OPTS="-server -Dfile.encoding=UTF-8"
EXPOSE 14445
CMD sh -c "java $JAVA_OPTS -jar /app/app.jar"
