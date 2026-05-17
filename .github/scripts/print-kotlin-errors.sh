#!/usr/bin/env bash
# Extracts Kotlin compiler / Gradle error lines from a captured gradle
# log and prints them three ways so a CI failure is visible no matter
# where the user is looking:
#
# 1. Plain echo  → bottom of the failing step's log
# 2. ::error::   → red annotation pinned to the run page
# 3. $GITHUB_STEP_SUMMARY → top-of-run summary panel
#
# Usage: print-kotlin-errors.sh <log-file>
set -u
log="${1:-}"
if [ -z "$log" ] || [ ! -f "$log" ]; then
  echo "print-kotlin-errors: no log file at '$log'"
  exit 0
fi

# Kotlin compile errors start with "e: " or "error:"; Gradle marks
# failed tasks with "> Task ... FAILED" and "FAILURE: Build failed".
errors=$(grep -E "^e: |^error:| error:|FAILURE: |> Task .* FAILED" "$log" || true)

echo ""
echo "==================================================================="
echo "  Kotlin compile errors extracted from $log"
echo "==================================================================="
if [ -z "$errors" ]; then
  echo "(no e:/error:/FAILED lines matched — dumping last 80 lines instead)"
  tail -80 "$log"
else
  echo "$errors" | head -80
fi
echo "==================================================================="

# As ::error:: annotations on the run page.
echo "$errors" | head -20 | while IFS= read -r line; do
  [ -n "$line" ] && echo "::error::$line"
done

# To the run-summary panel at the top of the page.
if [ -n "${GITHUB_STEP_SUMMARY:-}" ]; then
  {
    echo ""
    echo "## Kotlin errors from $(basename "$log")"
    echo ''
    echo '```'
    if [ -n "$errors" ]; then
      echo "$errors" | head -80
    else
      tail -80 "$log"
    fi
    echo '```'
  } >> "$GITHUB_STEP_SUMMARY"
fi
