#!/usr/bin/env bash
# End-to-end iOS release pipeline for LowerBackStretching.
#
# - Regenerates the .xcodeproj from ios/project.yml via xcodegen
# - Archives + exports an App Store IPA with auto-managed signing
# - Validates and uploads to TestFlight via xcrun altool
#
# Prereqs (one-time):
#   * Xcode installed at /Applications/Xcode.app
#   * `xcodegen` on PATH (brew install xcodegen)
#   * App Store Connect API key file at ASC_KEY_PATH
#   * App record already created in App Store Connect (Apple's API doesn't
#     allow creating apps — see https://appstoreconnect.apple.com/apps)
#
# Bumping build number: edit ios/project.yml `CURRENT_PROJECT_VERSION` before
# running, or pass BUILD_NUMBER=42 to override. ASC rejects duplicate builds.
#
# Usage:
#   ./scripts/release-ios.sh                  # full pipeline
#   SKIP_UPLOAD=1 ./scripts/release-ios.sh    # archive + export only
#   VALIDATE_ONLY=1 ./scripts/release-ios.sh  # validate the existing IPA
#
set -euo pipefail

readonly REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
readonly IOS_SRC="$REPO_ROOT/ios"
readonly BUILD_DIR="${BUILD_DIR:-/tmp/lbs-build}"

# ASC API key — required for both auto-provisioning at archive time and
# the final upload. Override via env if your setup differs.
readonly ASC_KEY_ID="${ASC_KEY_ID:-2NWJ77276X}"
readonly ASC_ISSUER_ID="${ASC_ISSUER_ID:-7080ef6c-0e05-48e7-b508-72b9259dff45}"
readonly ASC_KEY_PATH="${ASC_KEY_PATH:-$HOME/.appstoreconnect/private_keys/AuthKey_${ASC_KEY_ID}.p8}"

readonly SCHEME="LowerBackStretching"
readonly ARCHIVE_PATH="$BUILD_DIR/$SCHEME.xcarchive"
readonly EXPORT_DIR="$BUILD_DIR/Export"
readonly IPA_PATH="$EXPORT_DIR/$SCHEME.ipa"
readonly EXPORT_OPTS="$BUILD_DIR/ExportOptions.plist"

export DEVELOPER_DIR="${DEVELOPER_DIR:-/Applications/Xcode.app/Contents/Developer}"

step() { printf "\n\033[1;36m▸ %s\033[0m\n" "$*"; }
die()  { printf "\033[1;31m✗ %s\033[0m\n" "$*" >&2; exit 1; }

[[ -f "$ASC_KEY_PATH" ]] || die "ASC API key not found at $ASC_KEY_PATH"
command -v xcodegen >/dev/null || die "xcodegen not on PATH (brew install xcodegen)"

if [[ "${VALIDATE_ONLY:-0}" == "1" ]]; then
  step "Validating existing IPA at $IPA_PATH"
  [[ -f "$IPA_PATH" ]] || die "No IPA at $IPA_PATH — run a full build first"
  xcrun altool --validate-app -f "$IPA_PATH" -t ios \
    --apiKey "$ASC_KEY_ID" --apiIssuer "$ASC_ISSUER_ID"
  exit 0
fi

step "Syncing sources to $BUILD_DIR"
mkdir -p "$BUILD_DIR"
rsync -a --delete \
  --exclude '*.xcodeproj' --exclude 'DerivedData' --exclude 'Export' \
  --exclude '*.xcarchive' --exclude 'ExportOptions.plist' \
  "$IOS_SRC/" "$BUILD_DIR/"

step "Generating Xcode project"
( cd "$BUILD_DIR" && xcodegen generate --spec project.yml )

step "Building archive (Release, generic iOS device)"
xcodebuild \
  -project "$BUILD_DIR/$SCHEME.xcodeproj" \
  -scheme "$SCHEME" \
  -configuration Release \
  -destination 'generic/platform=iOS' \
  -archivePath "$ARCHIVE_PATH" \
  -allowProvisioningUpdates \
  -authenticationKeyPath "$ASC_KEY_PATH" \
  -authenticationKeyID "$ASC_KEY_ID" \
  -authenticationKeyIssuerID "$ASC_ISSUER_ID" \
  ${BUILD_NUMBER:+CURRENT_PROJECT_VERSION=$BUILD_NUMBER} \
  archive

step "Writing ExportOptions.plist"
cat > "$EXPORT_OPTS" <<'PLIST'
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>method</key>
    <string>app-store-connect</string>
    <key>teamID</key>
    <string>9U3ZSABZG7</string>
    <key>signingStyle</key>
    <string>automatic</string>
    <key>stripSwiftSymbols</key>
    <true/>
    <key>uploadSymbols</key>
    <true/>
    <key>destination</key>
    <string>export</string>
</dict>
</plist>
PLIST

step "Exporting IPA"
rm -rf "$EXPORT_DIR"
xcodebuild \
  -exportArchive \
  -archivePath "$ARCHIVE_PATH" \
  -exportPath "$EXPORT_DIR" \
  -exportOptionsPlist "$EXPORT_OPTS" \
  -allowProvisioningUpdates \
  -authenticationKeyPath "$ASC_KEY_PATH" \
  -authenticationKeyID "$ASC_KEY_ID" \
  -authenticationKeyIssuerID "$ASC_ISSUER_ID"

step "Validating IPA against App Store Connect"
xcrun altool --validate-app -f "$IPA_PATH" -t ios \
  --apiKey "$ASC_KEY_ID" --apiIssuer "$ASC_ISSUER_ID"

if [[ "${SKIP_UPLOAD:-0}" == "1" ]]; then
  step "SKIP_UPLOAD=1 — stopping after validation. IPA at $IPA_PATH"
  exit 0
fi

step "Uploading IPA to TestFlight"
xcrun altool --upload-app -f "$IPA_PATH" -t ios \
  --apiKey "$ASC_KEY_ID" --apiIssuer "$ASC_ISSUER_ID"

step "Done — build is processing in App Store Connect"
echo "  IPA: $IPA_PATH"
echo "  Watch for the build under TestFlight → Builds (5–15 min)."
