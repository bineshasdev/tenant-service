# Build stage
FROM eclipse-temurin:17-jdk-jammy as builder
WORKDIR /workspace/app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN ./mvnw package -DskipTests
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

# Runtime stage
FROM eclipse-temurin:17-jre-jammy
VOLUME /tmp

ARG DEPENDENCY=/workspace/app/target/dependency
COPY --from=builder ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=builder ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=builder ${DEPENDENCY}/BOOT-INF/classes /app

# Add Grafana Agent
ADD https://github.com/grafana/agent/releases/download/v0.24.2/agent-linux-amd64.zip /tmp/agent.zip
RUN apt-get update && apt-get install -y unzip && \
    unzip /tmp/agent.zip -d /app && \
    rm /tmp/agent.zip

COPY agent-config.yaml /app/agent-config.yaml

ENTRYPOINT ["sh", "-c", \
"java ${JAVA_OPTS} -cp app:app/lib/* com.example.account.AccountServiceApplication && \
/app/agent-linux-amd64 -config.file=/app/agent-config.yaml"]