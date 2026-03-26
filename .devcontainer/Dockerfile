FROM eclipse-temurin:25-alpine

# Install Alpine packages for development
RUN apk add --no-cache \
    bash \
    zsh \
    git \
    curl \
    wget \
    tar \
    unzip \
    openssh-client \
    sudo \
    shadow \
    gcc \
    g++ \
    musl-dev \
    zlib-dev \
    jq \
    procps \
    findutils \
    coreutils \
    sed \
    grep \
    fontconfig \
    freetype \
    zip \
    docker-cli \
    docker-cli-compose \
    github-cli

# Create non-root user with sudo + docker access
# Use dynamic GID/UID to avoid conflicts with base image groups
RUN addgroup -S docker 2>/dev/null || true \
    && addgroup vscode \
    && adduser -G vscode -s /bin/zsh -D vscode \
    && addgroup vscode docker \
    && echo "vscode ALL=(ALL) NOPASSWD:ALL" > /etc/sudoers.d/vscode \
    && chmod 0440 /etc/sudoers.d/vscode

# Create directories for SDKMAN and caches (owned by vscode)
RUN mkdir -p /home/vscode/.m2 /home/vscode/.gradle \
    && chown -R vscode:vscode /home/vscode

# Environment variables
ENV JAVA_HOME=/opt/java/openjdk
ENV MAVEN_OPTS="-Xmx2g -XX:+UseG1GC"
ENV GRADLE_OPTS="-Xmx2g -Dorg.gradle.daemon=true"

USER vscode
WORKDIR /workspaces
