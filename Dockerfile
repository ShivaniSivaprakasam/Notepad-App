# ---- Stage 1: Build the application with Maven ----
# Using a Maven image that includes JDK 17, matching the project's
# configured Java version. This stage compiles the app and produces
# the runnable .jar — it is NOT part of the final image, keeping the
# final image small (no Maven, no source code, no build cache).
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy only the pom.xml first and download dependencies — this layer
# gets cached by Docker and is only re-run when pom.xml actually
# changes, meaning code-only changes rebuild much faster.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Now copy the actual source code and build the jar.
COPY src ./src
RUN mvn clean package -DskipTests

# ---- Stage 2: Run the application ----
# A minimal JRE-only image (no JDK, no Maven) — smaller and reduces
# the attack surface for the image that actually runs in production.
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy only the built jar from the previous stage.
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]