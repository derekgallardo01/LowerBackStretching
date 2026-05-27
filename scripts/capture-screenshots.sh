#!/usr/bin/env bash
# Drive the iOS app via XCUITest and capture App Store-sized screenshots
# for both the 6.9" iPhone tier and the 13" iPad tier.
#
# Output: ios/AppStoreScreenshots/{iPhone-6.9,iPad-13}/*.png
#
# Status bar is overridden to canonical demo values (9:41, full bars,
# 100% battery). Run again any time the UI changes.
set -euo pipefail

readonly REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
readonly IOS_SRC="$REPO_ROOT/ios"
readonly BUILD_DIR="${BUILD_DIR:-/tmp/lbs-build}"
readonly SHOTS_DIR="$IOS_SRC/AppStoreScreenshots"

readonly IPHONE_SIM="${IPHONE_SIM:-iPhone 17 Pro Max}"
readonly IPAD_SIM="${IPAD_SIM:-iPad Pro 13-inch (M5)}"

export DEVELOPER_DIR="${DEVELOPER_DIR:-/Applications/Xcode.app/Contents/Developer}"

step() { printf "\n\033[1;36m▸ %s\033[0m\n" "$*"; }
die()  { printf "\033[1;31m✗ %s\033[0m\n" "$*" >&2; exit 1; }

command -v xcodegen >/dev/null || die "xcodegen not on PATH (brew install xcodegen)"

step "Syncing sources to $BUILD_DIR"
mkdir -p "$BUILD_DIR"
rsync -a --delete \
  --exclude '*.xcodeproj' --exclude 'DerivedData' --exclude 'Export' \
  --exclude '*.xcarchive' --exclude 'ExportOptions.plist' --exclude '*.xcresult' \
  "$IOS_SRC/" "$BUILD_DIR/"

step "Regenerating Xcode project"
( cd "$BUILD_DIR" && xcodegen generate --spec project.yml >/dev/null )

run_device() {
  local sim="$1" out_subdir="$2" tier_label="$3"
  step "Booting simulator: $sim"
  xcrun simctl boot "$sim" 2>/dev/null || true
  xcrun simctl status_bar "$sim" override \
    --time "9:41" \
    --dataNetwork wifi \
    --wifiMode active --wifiBars 3 \
    --cellularMode active --cellularBars 4 \
    --batteryState charged --batteryLevel 100 || true

  local result="$BUILD_DIR/Shots-$out_subdir.xcresult"
  rm -rf "$result"

  step "Running screenshot UITest on $sim"
  xcodebuild \
    -project "$BUILD_DIR/LowerBackStretching.xcodeproj" \
    -scheme LowerBackStretching \
    -sdk iphonesimulator \
    -destination "platform=iOS Simulator,name=$sim" \
    -derivedDataPath "$BUILD_DIR/DerivedData" \
    CODE_SIGNING_ALLOWED=YES CODE_SIGN_IDENTITY=- CODE_SIGNING_REQUIRED=NO \
    -only-testing:LowerBackStretchingUITests/AppStoreScreenshotsUITest \
    -resultBundlePath "$result" \
    test >/dev/null || die "UITest failed on $sim — see $result"

  step "Extracting attachments"
  local raw="$BUILD_DIR/Raw-$out_subdir"
  rm -rf "$raw"
  xcrun xcresulttool export attachments \
    --path "$result" --output-path "$raw" >/dev/null

  local dest="$SHOTS_DIR/$out_subdir"
  mkdir -p "$dest"
  rm -f "$dest"/*.png
  python3 - "$raw" "$dest" <<'PY'
import json, os, shutil, sys
raw, dest = sys.argv[1], sys.argv[2]
m = json.load(open(os.path.join(raw, "manifest.json")))
n = 0
for t in m:
    for att in t.get("attachments", []):
        name = att.get("suggestedHumanReadableName", "")
        clean = name.split("_")[0] + ".png" if "_" in name else name
        src = os.path.join(raw, att.get("exportedFileName", ""))
        if os.path.exists(src):
            shutil.copy(src, os.path.join(dest, clean))
            n += 1
print(f"  {n} screenshots → {dest}")
PY
  local dims
  dims=$(sips -g pixelWidth -g pixelHeight "$dest/01-home.png" 2>/dev/null | awk '/pixel/{print $2}' | xargs)
  echo "  $tier_label: $dims"
}

run_device "$IPHONE_SIM" "iPhone-6.9" "iPhone 6.9\" tier"
run_device "$IPAD_SIM" "iPad-13"      "iPad 13\" tier"

step "Done"
echo "Upload to App Store Connect → Lower Back Stretching → 1.0 → Screenshots."
echo "  iPhone 6.9\": $SHOTS_DIR/iPhone-6.9/"
echo "  iPad 13\":    $SHOTS_DIR/iPad-13/"
