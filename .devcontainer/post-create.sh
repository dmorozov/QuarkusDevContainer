#!/usr/bin/env bash
set -eo pipefail

echo "=== Quarkus DevContainer Post-Create Setup ==="

# Fix ownership of volume-mounted directories (may be root-owned on first create)
sudo chown -R "$(id -u):$(id -g)" "${HOME}/.m2" "${HOME}/.gradle" 2>/dev/null || true

# -------------------------------------------------------
# 1. Install SDKMAN
# -------------------------------------------------------
echo "[1/4] Installing SDKMAN..."
export SDKMAN_DIR="${HOME}/.sdkman"
if [ -d "${SDKMAN_DIR}" ] && [ ! -f "${SDKMAN_DIR}/bin/sdkman-init.sh" ]; then
    rm -rf "${SDKMAN_DIR}"
fi
curl -s "https://get.sdkman.io?rcupdate=false" | bash > /dev/null
set +eu
source "${SDKMAN_DIR}/bin/sdkman-init.sh"
set -eu

# -------------------------------------------------------
# 2. Install Quarkus CLI via SDKMAN
# -------------------------------------------------------
echo "[2/4] Installing Quarkus CLI 3.34.1..."
set +eu
sdk install quarkus 3.34.1 2>&1 | grep -E "^(Installing|Done|Setting)" || true
set -eu

# -------------------------------------------------------
# 3. Install JBang via SDKMAN (required by quarkus-agent MCP)
# -------------------------------------------------------
echo "[3/4] Installing JBang..."
set +eu
sdk install jbang 2>&1 | grep -E "^(Installing|Done|Setting)" || true
set -eu

# -------------------------------------------------------
# 3. Install Claude Code CLI
# -------------------------------------------------------
echo "[4/4] Installing Claude Code CLI..."
curl -fsSL https://claude.ai/install.sh | bash > /dev/null 2>&1 || true
export PATH="${HOME}/.local/bin:${PATH}"

# -------------------------------------------------------
# 4. Configure Testcontainers for Docker socket sharing
# -------------------------------------------------------
TESTCONTAINERS_PROPS="${HOME}/.testcontainers.properties"
if [ ! -f "${TESTCONTAINERS_PROPS}" ]; then
    cat > "${TESTCONTAINERS_PROPS}" <<'TC_EOF'
docker.client.strategy=org.testcontainers.dockerclient.EnvironmentAndSystemPropertyClientProviderStrategy
TC_EOF
fi

# -------------------------------------------------------
# Summary
# -------------------------------------------------------
echo ""
echo "=== Setup Complete ==="
set +eu
source "${SDKMAN_DIR}/bin/sdkman-init.sh" 2>/dev/null
set -eu
echo "  Java:    $(java --version 2>&1 | head -1)"
echo "  Quarkus: $(quarkus --version 2>&1 || echo 'not installed')"
echo "  JBang:   $(jbang --version 2>&1 || echo 'not installed')"
echo "  Docker:  $(docker --version 2>&1 || echo 'not available')"
echo ""
