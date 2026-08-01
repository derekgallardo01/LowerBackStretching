#!/usr/bin/env bash
# Runs a gradle invocation, captures stdout+stderr to a log file, and
# on failure routes through print-kotlin-errors.sh so the actual e:/
# error: lines appear in the failing step's log + ::error:: annotations
# + the GitHub Actions run summary. Single source of truth for the
# tee/PIPESTATUS/diagnostic dance that every CI step used to repeat.
#
# Usage: gradle-with-diag.sh <log-path> <gradle-args...>
set -u
log="${1:?log path required}"
shift

# Use the checked-in wrapper, not a system `gradle`, so CI and local builds
# resolve the same Gradle version (pinned in gradle/wrapper/).
./gradlew "$@" --stacktrace --console=plain 2>&1 | tee "$log"
rc=${PIPESTATUS[0]}
if [ "$rc" -ne 0 ]; then
  bash "$(dirname "${BASH_SOURCE[0]}")/print-kotlin-errors.sh" "$log"
fi
exit "$rc"
