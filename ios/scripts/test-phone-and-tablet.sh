#!/usr/bin/env bash
# Run the iOS test bundle on both an iPhone and an iPad simulator in a
# single xcodebuild invocation. Useful for confirming layout works on
# both form factors.
#
# Usage:
#   ./ios/scripts/test-phone-and-tablet.sh
#
# Requires Xcode 15+ with the iOS simulator runtimes installed. The named
# simulators must exist — `xcrun simctl list devices` shows what you have.
# Edit IPHONE / IPAD below if your environment uses different names.

set -euo pipefail

SCHEME="${SCHEME:-LowerBackStretching}"
IPHONE="${IPHONE:-iPhone 15}"
IPAD="${IPAD:-iPad Pro (11-inch) (M4)}"

# Resolve the .xcodeproj path. Adjust if you keep it elsewhere.
PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$PROJECT_DIR"

if [ ! -d "LowerBackStretching.xcodeproj" ] && [ ! -d "LowerBackStretching.xcworkspace" ]; then
  echo "No .xcodeproj or .xcworkspace found in $PROJECT_DIR" >&2
  echo "Create the Xcode project first (see ios/README.md)." >&2
  exit 1
fi

xcodebuild test \
  -scheme "$SCHEME" \
  -destination "platform=iOS Simulator,name=$IPHONE" \
  -destination "platform=iOS Simulator,name=$IPAD" \
  -parallel-testing-enabled YES \
  -resultBundlePath "build/PhoneAndTabletTestResults.xcresult"
