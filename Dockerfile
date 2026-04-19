 # ==============================================================================
# BUILD STAGE
# ==============================================================================
# Multi-stage build: This is the "builder" stage where we compile the application.
# The final image won't include any build tools, making it smaller and more secure.

# Use Eclipse Temurin JDK 21 on Ubuntu 22.04 (Jammy Jellyfish)
# Eclipse Temurin is a production-ready, TCK-certified JDK distribution from Adoptium
# We use JDK (not JRE) here because we need the Java compiler to build the project
# "AS builder" names this stage so we can reference it later in COPY --from=builder
FROM eclipse-temurin:21-jdk-jammy AS builder

# Set the working directory inside the container to /build
# All subsequent commands (COPY, RUN) will be relative to this directory
WORKDIR /build

# Install Apache Maven build tool
# Maven is required to compile the multi-module project and resolve dependencies
# We clean up apt cache afterwards to reduce layer size
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# -----------------------------------------------------------------------------
# LAYER CACHING OPTIMIZATION: Copy POM files first
# -----------------------------------------------------------------------------
# Docker caches each layer. If a layer hasn't changed, Docker reuses the cached version.
# By copying pom.xml files BEFORE source code, we can cache the dependency download
# layer. This means if you only change source code, Maven won't re-download dependencies.

# Copy the parent POM file that defines all modules and dependency versions
COPY pom.xml .

# Copy each module's pom.xml file
# These are needed for Maven to resolve the module structure
# Directory structure is created to match what Maven expects
COPY cliniq-shared-kernel/pom.xml cliniq-shared-kernel/
COPY cliniq-domain/pom.xml cliniq-domain/
COPY cliniq-application/pom.xml cliniq-application/
COPY cliniq-persistence/pom.xml cliniq-persistence/
COPY cliniq-web/pom.xml cliniq-web/
COPY cliniq-notification-adapter/pom.xml cliniq-notification-adapter/
COPY cliniq-external-adapter/pom.xml cliniq-external-adapter/
COPY cliniq-bootstrap/pom.xml cliniq-bootstrap/

# Download all project dependencies
# "dependency:go-offline" downloads everything needed to build the project
# This includes all Maven plugins and transitive dependencies
# "-B" runs Maven in batch mode (non-interactive) - important for CI/CD
# This layer will be cached as long as pom.xml files don't change
RUN mvn install -DskipTests -B -pl cliniq-shared-kernel

# -----------------------------------------------------------------------------
# COPY SOURCE CODE
# -----------------------------------------------------------------------------
# Now copy the actual Java source code for all modules
# This comes AFTER dependency download to maximize caching benefit
COPY cliniq-shared-kernel/src cliniq-shared-kernel/src
COPY cliniq-domain/src cliniq-domain/src
COPY cliniq-application/src cliniq-application/src
COPY cliniq-persistence/src cliniq-persistence/src
COPY cliniq-web/src cliniq-web/src
COPY cliniq-notification-adapter/src cliniq-notification-adapter/src
COPY cliniq-external-adapter/src cliniq-external-adapter/src
COPY cliniq-bootstrap/src cliniq-bootstrap/src

# Build the application
# "clean" - removes target/ directories from previous builds
# "package" - compiles code, runs tests (if enabled), creates JAR file
# "-DskipTests" - skips running tests to speed up the build (tests should run in CI)
# "-B" - batch mode for non-interactive execution
RUN mvn clean package -DskipTests -B

# -----------------------------------------------------------------------------
# LAYERED JAR EXTRACTION
# -----------------------------------------------------------------------------
# Spring Boot creates a "fat JAR" containing:
#   1. Application classes (your code - changes frequently)
#   2. Dependencies (third-party libraries - changes rarely)
#   3. Spring Boot loader (bootstrapping code - rarely changes)
#
# By extracting these into separate directories, we can create separate Docker layers.
# This is CRITICAL for build performance:
#   - If only your code changes, only the "application" layer is rebuilt
#   - Dependencies layer is cached (can be hundreds of MB)
#
# "layertools" mode extracts the JAR into 4 directories:
#   - dependencies/    : All third-party libraries
#   - spring-boot-loader/ : Classes needed to run the JAR
#   - snapshot-dependencies/ : SNAPSHOT versions of libraries (if any)
#   - application/     : Your compiled application classes
RUN java -Djarmode=layertools -jar cliniq-bootstrap/target/cliniq-bootstrap-0.0.1-SNAPSHOT.jar extract --destination extracted

# ==============================================================================
# RUNTIME STAGE
# ==============================================================================
# This is the final image that will be deployed to production.
# It's much smaller because it only contains:
#   - JRE (not full JDK) - we don't need a compiler at runtime
#   - Extracted application layers
#   - No build tools (Maven), no source code

# Use Eclipse Temurin JRE 21 on Ubuntu 22.04
# JRE (Java Runtime Environment) is smaller than JDK because it lacks development tools
# This is sufficient for running the application
FROM eclipse-temurin:21-jre-jammy

# Set working directory to /app where the application will live
WORKDIR /app

# -----------------------------------------------------------------------------
# SECURITY: Create non-root user
# -----------------------------------------------------------------------------
# Running containers as root is a security risk. If compromised, attacker has
# full control over the container. By creating a dedicated user:
#   - Limited permissions if attacker gains access
#   - Follows principle of least privilege
#   - Required by many security policies and Kubernetes Pod Security Standards
RUN groupadd --system cliniq && useradd --system -g cliniq cliniq

# -----------------------------------------------------------------------------
# COPY EXTRACTED LAYERS FROM BUILDER STAGE
# -----------------------------------------------------------------------------
# Each COPY command creates a new Docker layer.
# Order matters! Put layers that change least frequently first.
# This maximizes cache hits when rebuilding.
#
# "--from=builder" tells Docker to copy from the builder stage, not the host
# "--chown=cliniq:cliniq" sets file ownership to the non-root user
# We copy to root (.) of WORKDIR which is /app

# Layer 1: Third-party dependencies (changes rarely, biggest layer)
COPY --from=builder --chown=cliniq:cliniq /build/extracted/dependencies/ .

# Layer 2: Spring Boot loader classes (rarely changes)
COPY --from=builder --chown=cliniq:cliniq /build/extracted/spring-boot-loader/ .

# Layer 3: SNAPSHOT dependencies (only exists if you use snapshot versions)
COPY --from=builder --chown=cliniq:cliniq /build/extracted/snapshot-dependencies/ .

# Layer 4: Your application code (changes most frequently, smallest layer)
COPY --from=builder --chown=cliniq:cliniq /build/extracted/application/ .

# Switch to non-root user for all subsequent commands and at runtime
# This applies to ENTRYPOINT, CMD, and any commands run in the container
USER cliniq

# -----------------------------------------------------------------------------
# CONTAINER METADATA
# -----------------------------------------------------------------------------

# EXPOSE documents which port the application uses
# This is DOCUMENTATION ONLY - it doesn't actually publish the port
# The port still needs to be mapped with "-p" flag or docker-compose
# 8080 is the default port for Spring Boot applications
EXPOSE 8080

# -----------------------------------------------------------------------------
# JVM CONFIGURATION
# -----------------------------------------------------------------------------
# Environment variables for JVM tuning in containerized environments:
#
# -XX:+UseContainerSupport
#   Tells JVM to automatically detect container memory limits (cgroups)
#   Without this, JVM might see host's memory instead of container's limit
#
# -XX:MaxRAMPercentage=75.0
#   Limit heap to 75% of available container memory
#   Leaves 25% for non-heap memory (metaspace, thread stacks, native memory)
#   Example: 4GB container -> 3GB heap max
#
# -XX:+UseG1GC
#   Use G1 Garbage Collector - optimized for low pause times
#   Good default for most server applications running on multi-core machines
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

# -----------------------------------------------------------------------------
# STARTUP COMMAND
# -----------------------------------------------------------------------------
# ENTRYPOINT defines the command that runs when container starts
#
# "sh -c" - Run command via shell, allows $JAVA_OPTS variable expansion
# "java $JAVA_OPTS" - Start JVM with our configured options
# "org.springframework.boot.loader.JarLauncher" - Spring Boot's main class
#   that bootstraps the application from the extracted layers
#
# Alternative: Could use ENTRYPOINT ["java", "-XX:+UseContainerSupport", ...]
# but shell form allows easy override: docker run -e JAVA_OPTS="-Xmx512m" ...
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
