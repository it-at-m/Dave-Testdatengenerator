#!/usr/bin/env bash
set -euo pipefail

# Determine backend suffix from SPRING_PROFILES_ACTIVE, default to "dev"
BACKEND_SUFFIX="${SPRING_PROFILES_ACTIVE:-dev}"
export BACKEND_SUFFIX

# Render proxy config from template if template exists
TEMPLATE="/opt/app-root/src/nginx-templates/proxy_backend.conf.template"
OUTCONF="${NGINX_DEFAULT_CONF_PATH}/10-proxy-backend.conf"
if [ -f "$TEMPLATE" ]; then
  echo "Rendering proxy config with BACKEND_SUFFIX=${BACKEND_SUFFIX} -> $OUTCONF"
  envsubst '${BACKEND_SUFFIX}' < "$TEMPLATE" > "$OUTCONF"
fi

echo "Starting nginx..."
exec nginx -g 'daemon off;'
