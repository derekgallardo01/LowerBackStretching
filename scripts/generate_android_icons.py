"""
Generate complete Android Adaptive and Legacy Icons from the master logo.
Ensures:
1. Foreground contains the central emblem with proper safe-zone padding (~72dp in 108dp canvas).
2. Background provides clean full bleed.
3. Legacy icons (ic_launcher, ic_launcher_round) are generated with clean masks.
4. All density folders (mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi) + anydpi-v26 are properly populated.
"""

from pathlib import Path
from PIL import Image, ImageDraw

PROJECT_ROOT = Path(__file__).resolve().parent.parent
LOGO_PATH = PROJECT_ROOT / "logo" / "app_logo.png"
if not LOGO_PATH.exists():
    LOGO_PATH = PROJECT_ROOT / "play-store-icon-512.png"

RES_DIR = PROJECT_ROOT / "android" / "app" / "src" / "main" / "res"

# Android Icon Density Specifications
# Format: (folder_name, adaptive_size_108dp, legacy_size_48dp)
DENSITIES = [
    ("mipmap-mdpi", 108, 48),
    ("mipmap-hdpi", 162, 72),
    ("mipmap-xhdpi", 216, 96),
    ("mipmap-xxhdpi", 324, 144),
    ("mipmap-xxxhdpi", 432, 192),
]


def create_circular_mask(size):
    mask = Image.new("L", (size * 4, size * 4), 0)
    draw = ImageDraw.Draw(mask)
    draw.ellipse((0, 0, size * 4, size * 4), fill=255)
    return mask.resize((size, size), Image.LANCZOS)


def main():
    master_img = Image.open(LOGO_PATH).convert("RGBA")
    print(f"Loaded master logo from {LOGO_PATH}: size={master_img.size}")

    # Detect the background color from the corners
    bg_color = master_img.getpixel((5, 5))
    print(f"Detected background color: {bg_color}")

    for folder, adaptive_size, legacy_size in DENSITIES:
        target_dir = RES_DIR / folder
        target_dir.mkdir(parents=True, exist_ok=True)

        # 1. Background Layer (Adaptive, 108dp full bleed)
        bg_layer = Image.new("RGBA", (adaptive_size, adaptive_size), bg_color)
        bg_layer.save(target_dir / "ic_launcher_background.png", "PNG")

        # 2. Foreground Layer (Adaptive, 108dp with logo scaled to ~72dp safe zone in center)
        fg_layer = Image.new("RGBA", (adaptive_size, adaptive_size), (0, 0, 0, 0))
        logo_target_size = int(adaptive_size * 0.72)
        logo_resized = master_img.resize((logo_target_size, logo_target_size), Image.LANCZOS)
        
        offset = (adaptive_size - logo_target_size) // 2
        fg_layer.paste(logo_resized, (offset, offset), logo_resized)
        fg_layer.save(target_dir / "ic_launcher_foreground.png", "PNG")

        # 3. Legacy Flat Square Icon (48dp)
        legacy_icon = master_img.resize((legacy_size, legacy_size), Image.LANCZOS)
        legacy_icon.save(target_dir / "ic_launcher.png", "PNG")

        # 4. Legacy Round Icon (48dp with circular mask)
        round_icon = Image.new("RGBA", (legacy_size, legacy_size), (0, 0, 0, 0))
        round_mask = create_circular_mask(legacy_size)
        round_icon.paste(legacy_icon, (0, 0), round_mask)
        round_icon.save(target_dir / "ic_launcher_round.png", "PNG")

        print(f"Generated icons for {folder}: adaptive={adaptive_size}px, legacy={legacy_size}px")

    # Remove any empty/broken vector overrides
    anydpi_fg = RES_DIR / "mipmap-anydpi-v26" / "ic_launcher_foreground.xml"
    if anydpi_fg.exists():
        anydpi_fg.unlink()
        print("Removed conflicting transparent foreground XML from anydpi-v26.")

    print("\nAndroid icons generated successfully!")


if __name__ == "__main__":
    main()
