#!/usr/bin/env bash
set -euo pipefail

APP_HOME="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
JAR="$APP_HOME/build/libs/kr-economy-mon.jar"
LOG_DIR="/var/log/kr-economy-mon"
PID_FILE="/var/run/kr-economy-mon.pid"

mkdir -p "$LOG_DIR"

if [[ ! -f "$JAR" ]]; then
    echo "ERROR: JAR not found at $JAR. Run './gradlew build' first." >&2
    exit 1
fi

required_vars=(ECOS_API_KEY GEMINI_API_KEY POSTGRES_HOST POSTGRES_PORT POSTGRES_DB POSTGRES_USER POSTGRES_PASSWORD)
for var in "${required_vars[@]}"; do
    if [[ -z "${!var:-}" ]]; then
        echo "ERROR: Required environment variable $var is not set." >&2
        exit 1
    fi
done

echo "Starting kr-economy-mon..."
java -jar "$JAR" \
    --spring.profiles.active=prod \
    >> "$LOG_DIR/app.log" 2>&1 &

echo $! > "$PID_FILE"
echo "Started with PID $(cat "$PID_FILE"). Logs: $LOG_DIR/app.log"
