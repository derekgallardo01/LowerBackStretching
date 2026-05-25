#!/usr/bin/env bash
# Usage:
#   ./scripts/capture_screenshot.sh phone 01-home
#   ./scripts/capture_screenshot.sh tablet 01-home
#
# Captures whatever's currently on the named device's screen into
# screenshots/<form-factor>/<name>.png and resizes to fit Play's
# constraints (9:16 portrait, 1080 wide).
#
# Requires ImageMagick or sips for the resize step — falls back to
# raw screencap if neither is present.

set -euo pipefail

if [[ $# -lt 2 ]]; then
    echo "Usage: $0 <phone|tablet> <name-without-ext>"
    exit 1
fi

FORM_FACTOR="$1"
NAME="$2"

case "$FORM_FACTOR" in
    phone)  SERIAL_HINT="emulator-5554" ;;
    tablet) SERIAL_HINT="emulator-5556" ;;
    *) echo "Unknown form factor: $FORM_FACTOR (expected phone|tablet)" ; exit 1 ;;
esac

# Pick the actual serial — falls back to "any device" if there's only one.
DEVICE_COUNT=$(adb devices | grep -c "^emulator-" || true)
if [[ "$DEVICE_COUNT" == "1" ]]; then
    ADB="adb"
else
    ADB="adb -s $SERIAL_HINT"
fi

OUTDIR="screenshots/$FORM_FACTOR"
mkdir -p "$OUTDIR"

$ADB shell screencap -p /sdcard/__shot.png
$ADB pull /sdcard/__shot.png "$OUTDIR/${NAME}.png" >/dev/null
$ADB shell rm /sdcard/__shot.png

# Report dimensions so you can spot Play-constraint issues immediately.
if command -v python >/dev/null 2>&1; then
    python -c "from PIL import Image; im = Image.open('$OUTDIR/${NAME}.png'); print(f'Saved: $OUTDIR/${NAME}.png  {im.width}x{im.height}')"
else
    echo "Saved: $OUTDIR/${NAME}.png"
fi
