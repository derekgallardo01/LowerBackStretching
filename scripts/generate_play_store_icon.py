"""
Render the Play Store 512x512 icon by replaying the adaptive-icon
foreground/background defined in
`android/app/src/main/res/mipmap-anydpi-v26/ic_launcher_foreground.xml`
into a flat PNG. The vector path approximates a circle (outer) cut by
an inner circle (the hole), so we can faithfully reproduce it with two
filled ellipses centered in the icon canvas.

The vector viewport is 108x108. Outer ring spans roughly 32..76 on both
axes (radius ~22 around center 54,54); inner hole spans 42..66 (radius
~12). Both centered. Background is the solid sage `#5C7A65`.

Output: play-store-icon-512.png in the repo root.
"""
from __future__ import annotations
from pathlib import Path
from PIL import Image, ImageDraw

SAGE = "#5C7A65"
CREAM = "#FBF7EF"
SIZE = 512
SCALE = SIZE / 108.0
CENTER = (SIZE / 2, SIZE / 2)
OUTER_R = 22 * SCALE  # ~104px
INNER_R = 12 * SCALE  # ~57px


def render() -> Image.Image:
    # Solid sage background — Play wants a 32-bit PNG, no transparency.
    img = Image.new("RGB", (SIZE, SIZE), SAGE)
    draw = ImageDraw.Draw(img)
    # Render at 4x then downsample for clean anti-aliasing.
    super_size = SIZE * 4
    big = Image.new("RGB", (super_size, super_size), SAGE)
    big_draw = ImageDraw.Draw(big)
    cx, cy = super_size / 2, super_size / 2
    big_draw.ellipse(
        (cx - OUTER_R * 4, cy - OUTER_R * 4, cx + OUTER_R * 4, cy + OUTER_R * 4),
        fill=CREAM,
    )
    big_draw.ellipse(
        (cx - INNER_R * 4, cy - INNER_R * 4, cx + INNER_R * 4, cy + INNER_R * 4),
        fill=SAGE,
    )
    return big.resize((SIZE, SIZE), Image.LANCZOS)


if __name__ == "__main__":
    out = Path(__file__).resolve().parent.parent / "play-store-icon-512.png"
    render().save(out, "PNG", optimize=True)
    print(f"Wrote {out} ({out.stat().st_size:,} bytes)")
