FROM eclipse-temurin:25-alpine

# Install Alpine packages for development
RUN apk add --no-cache \
    bash \
    zsh \
    oh-my-zsh \
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

# Create directories for caches (owned by vscode)
RUN mkdir -p /home/vscode/.m2 /home/vscode/.gradle \
    && chown -R vscode:vscode /home/vscode

# Install devcontainers oh-my-zsh theme
RUN { \
    echo '# devcontainers theme'; \
    echo 'PROMPT="%F{green}%n%f@%F{blue}devcontainer%f %F{yellow}%~%f $ "'; \
    echo 'ZSH_THEME_GIT_PROMPT_PREFIX="%F{cyan}git:(%F{red}"'; \
    echo 'ZSH_THEME_GIT_PROMPT_SUFFIX="%f "'; \
    echo 'ZSH_THEME_GIT_PROMPT_DIRTY="%F{cyan}) %F{yellow}✗"'; \
    echo 'ZSH_THEME_GIT_PROMPT_CLEAN="%F{cyan})"'; \
    echo 'RPROMPT="$(git_prompt_info)"'; \
    } > /usr/share/oh-my-zsh/custom/themes/devcontainers.zsh-theme

# Configure Oh My Zsh for vscode user
RUN cp /usr/share/oh-my-zsh/templates/zshrc.zsh-template /home/vscode/.zshrc \
    && sed -i 's|^export ZSH=.*|export ZSH="/usr/share/oh-my-zsh"|' /home/vscode/.zshrc \
    && sed -i 's|^ZSH_THEME=.*|ZSH_THEME="bira"|' /home/vscode/.zshrc \
    && { \
    echo ''; \
    echo '# Java / Dev environment'; \
    echo 'export JAVA_HOME=/opt/java/openjdk'; \
    echo 'export PATH="${HOME}/.claude/bin:${HOME}/.sdkman/candidates/maven/current/bin:${HOME}/.sdkman/candidates/quarkus/current/bin:${PATH}"'; \
    echo '[ -s "${HOME}/.sdkman/bin/sdkman-init.sh" ] && source "${HOME}/.sdkman/bin/sdkman-init.sh"'; \
    } >> /home/vscode/.zshrc \
    && chown vscode:vscode /home/vscode/.zshrc

# Environment variables
ENV JAVA_HOME=/opt/java/openjdk
ENV MAVEN_OPTS="-Xmx2g -XX:+UseG1GC"
ENV GRADLE_OPTS="-Xmx2g -Dorg.gradle.daemon=true"

USER vscode
WORKDIR /workspaces
