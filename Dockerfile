# One image: the Angular build is served as static files by the Spring Boot app.

# ---- 1. Build the UI ----
FROM node:22-alpine AS ui
WORKDIR /ui
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npx ng build --configuration production

# ---- 2. Build the API (with the UI inside) ----
FROM eclipse-temurin:21-jdk AS api
WORKDIR /api
COPY backend/.mvn .mvn
COPY backend/mvnw backend/pom.xml ./
RUN ./mvnw -q -B dependency:go-offline
COPY backend/src src
COPY --from=ui /ui/dist/frontend/browser src/main/resources/static
RUN ./mvnw -q -B package -DskipTests

# ---- 3. Runtime ----
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 1001 app && mkdir /data && chown app /data
COPY --from=api /api/target/salary-management-*.jar app.jar
USER app
ENV DB_PATH=/data/salary.db \
    PORT=8080 \
    JAVA_OPTS="-XX:MaxRAMPercentage=75"
VOLUME /data
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s \
  CMD wget -qO- "http://localhost:${PORT}/actuator/health" || exit 1
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
