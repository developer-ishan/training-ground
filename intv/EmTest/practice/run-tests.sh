#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT"

# Usage:
#   ./run-tests.sh        macOS: JDK 21 if present, else highest from java_home
#   ./run-tests.sh 25     macOS: JDK matching major version 25
# Other OS: set JAVA_HOME before running; optional arg is ignored unless macOS java_home exists

JDK_VER="${1:-}"

if [[ "$(uname -s)" == "Darwin" ]] && [[ -x /usr/libexec/java_home ]]; then
  if [[ -n "$JDK_VER" ]]; then
    export JAVA_HOME="$(/usr/libexec/java_home -v "$JDK_VER")"
  else
    export JAVA_HOME="$(/usr/libexec/java_home -v 21 2>/dev/null || /usr/libexec/java_home)"
  fi
elif [[ -n "${JAVA_HOME:-}" ]]; then
  true
else
  echo "Set JAVA_HOME to a JDK 21+ install, or run this script on macOS." >&2
  exit 1
fi

if [[ ! -x "${JAVA_HOME}/bin/java" ]]; then
  echo "Invalid JAVA_HOME: ${JAVA_HOME}" >&2
  exit 1
fi

echo "JAVA_HOME=${JAVA_HOME}"
"${JAVA_HOME}/bin/java" -version 2>&1 | head -1
echo

exec mvn test
