"""
Render the 1024x500 Play Store feature graphic. Same calm sage/cream
palette as the launcher icon — title set in a Georgia-like serif on
the left, cream ring mark on the right.

Output: feature-graphic-1024x500.png in the repo root.
"""
from __future__ import annotations
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

SAGE = "#5C7A65"
CREAM = "#FBF7EF"
SAGE_GLOW = "#7FA68A"  # subtle accent

WIDTH, HEIGHT = 1024, 500


def _try_font(candidates: list[tuple[str, int]]) -> ImageFont.FreeTypeFont | ImageFont.ImageFont:
    """First TrueType font that resolves wins; otherwise the bundled bitmap fallback."""
    for path, size in candidates:
        try:
            return ImageFont.truetype(path, size)
        except OSError:
            continue
    return ImageFont.load_default()


def render() -> Image.Image:
    img = Image.new("RGB", (WIDTH, HEIGHT), SAGE)
    draw = ImageDraw.Draw(img)

    # Subtle vignette accent at top-right — a soft glow ring behind the
    # icon mark to give the composition a little depth without breaking
    # the calm tone.
    glow_cx, glow_cy = 820, 250
    for r, alpha in [(220, 35), (180, 65), (150, 90)]:
        overlay = Image.new("RGBA", (WIDTH, HEIGHT), (0, 0, 0, 0))
        ImageDraw.Draw(overlay).ellipse(
            (glow_cx - r, glow_cy - r, glow_cx + r, glow_cy + r),
            fill=(127, 166, 138, alpha),  # SAGE_GLOW with alpha
        )
        img.paste(Image.alpha_composite(img.convert("RGBA"), overlay).convert("RGB"))

    draw = ImageDraw.Draw(img)

    # Cream ring mark on the right — the same shape as the launcher icon.
    outer_r, inner_r = 140, 78
    draw.ellipse(
        (glow_cx - outer_r, glow_cy - outer_r, glow_cx + outer_r, glow_cy + outer_r),
        fill=CREAM,
    )
    draw.ellipse(
        (glow_cx - inner_r, glow_cy - inner_r, glow_cx + inner_r, glow_cy + inner_r),
        fill=SAGE,
    )

    # Title + tagline on the left half.
    title_font = _try_font([
        ("C:/Windows/Fonts/georgiab.ttf", 60),
        ("C:/Windows/Fonts/arialbd.ttf", 60),
        ("/System/Library/Fonts/Supplemental/Georgia.ttf", 60),
        ("/usr/share/fonts/truetype/dejavu/DejaVuSerif-Bold.ttf", 60),
    ])
    tag_font = _try_font([
        ("C:/Windows/Fonts/georgia.ttf", 28),
        ("C:/Windows/Fonts/arial.ttf", 28),
        ("/System/Library/Fonts/Supplemental/Georgia.ttf", 28),
        ("/usr/share/fonts/truetype/dejavu/DejaVuSerif.ttf", 28),
    ])

    title_lines = ["Lower Back", "Stretching"]
    tagline = "Less back pain, day by day."

    # Two-line title, vertically centered around y=250.
    x = 80
    line_height = 78
    title_block_h = line_height * len(title_lines)
    y_start = (HEIGHT - title_block_h - 60) // 2  # leave room for tagline
    for i, line in enumerate(title_lines):
        draw.text((x, y_start + i * line_height), line, fill=CREAM, font=title_font)

    # Tagline below.
    draw.text((x, y_start + title_block_h + 14), tagline, fill=CREAM, font=tag_font)

    return img


if __name__ == "__main__":
    out = Path(__file__).resolve().parent.parent / "feature-graphic-1024x500.png"
    render().save(out, "PNG", optimize=True)
    print(f"Wrote {out} ({out.stat().st_size:,} bytes)")
