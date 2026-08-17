"""Create an MP4 video slideshow from app screenshots for YouTube upload."""
import os
import sys
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFont
import imageio

# Paths
PROJECT_ROOT = Path("I:/Projects/LowerBackStretching")
SCREENSHOTS_DIR = PROJECT_ROOT / "store-screenshots" / "phone"
OUTPUT_PATH = PROJECT_ROOT / "LowerBackStretching_Demo.mp4"

# Video settings
WIDTH, HEIGHT = 1920, 1080  # 1080p for YouTube
FPS = 30
HOLD_FRAMES = int(3.0 * FPS)      # 3 seconds per screenshot
TRANSITION_FRAMES = int(0.5 * FPS)  # 0.5s crossfade
BG_COLOR = (18, 18, 22)  # Dark slate background
ACCENT_COLOR = (59, 130, 246)  # Blue accent


def load_screenshots():
    """Load and sort all phone screenshots."""
    files = sorted(SCREENSHOTS_DIR.glob("*.png"))
    images = []
    for f in files:
        img = Image.open(f).convert("RGBA")
        images.append((f.stem, img))
    return images


def create_background():
    """Create a dark background with subtle gradient."""
    bg = Image.new("RGB", (WIDTH, HEIGHT), BG_COLOR)
    draw = ImageDraw.Draw(bg)
    # Subtle radial-ish gradient at center
    for r in range(max(WIDTH, HEIGHT) // 2, 0, -10):
        alpha = int(8 * (1 - r / (max(WIDTH, HEIGHT) // 2)))
        color = (min(255, BG_COLOR[0] + alpha), min(255, BG_COLOR[1] + alpha), min(255, BG_COLOR[2] + alpha + 5))
        draw.ellipse([WIDTH//2 - r, HEIGHT//2 - r, WIDTH//2 + r, HEIGHT//2 + r], fill=color)
    return bg


def add_phone_frame(screenshot):
    """Center screenshot on a dark phone frame, place on background."""
    bg = create_background().copy()
    
    # Resize screenshot to fit nicely (max height ~75% of canvas)
    max_h = int(HEIGHT * 0.75)
    ss_w, ss_h = screenshot.size
    scale = max_h / ss_h
    new_w, new_h = int(ss_w * scale), int(ss_h * scale)
    screenshot = screenshot.resize((new_w, new_h), Image.LANCZOS)
    
    # Phone frame dimensions
    pad = 16
    frame_w, frame_h = new_w + pad * 2, new_h + pad * 2
    
    # Create phone frame
    frame = Image.new("RGBA", (frame_w, frame_h), (0, 0, 0, 0))
    frame_draw = ImageDraw.Draw(frame)
    # Rounded rectangle for phone body
    radius = 24
    frame_draw.rounded_rectangle([0, 0, frame_w - 1, frame_h - 1], radius=radius, fill=(30, 30, 35), outline=(60, 60, 70), width=2)
    
    # Paste screenshot into frame
    frame.paste(screenshot, (pad, pad), screenshot)
    
    # Center on background
    x = (WIDTH - frame_w) // 2
    y = (HEIGHT - frame_h) // 2 - 30  # slight upward offset for text below
    bg.paste(frame, (x, y), frame)
    
    return bg


def draw_text(bg, label):
    """Draw app name and screenshot label at bottom."""
    draw = ImageDraw.Draw(bg)
    
    # Try to load a font, fallback to default
    try:
        title_font = ImageFont.truetype("arial.ttf", 48)
        label_font = ImageFont.truetype("arial.ttf", 32)
    except Exception:
        title_font = ImageFont.load_default()
        label_font = ImageFont.load_default()
    
    # App title
    title = "Lower Back Stretching"
    bbox = draw.textbbox((0, 0), title, font=title_font)
    tw = bbox[2] - bbox[0]
    th = bbox[3] - bbox[1]
    tx = (WIDTH - tw) // 2
    ty = HEIGHT - 160
    draw.text((tx, ty), title, fill=(255, 255, 255), font=title_font)
    
    # Screenshot label
    label_clean = label.replace("-", " ").replace("_", " ").title()
    bbox = draw.textbbox((0, 0), label_clean, font=label_font)
    lw = bbox[2] - bbox[0]
    lx = (WIDTH - lw) // 2
    ly = HEIGHT - 100
    draw.text((lx, ly), label_clean, fill=(150, 150, 160), font=label_font)
    
    return bg


def crossfade(frame_a, frame_b, alpha):
    """Blend two frames with alpha (0=frame_a, 1=frame_b)."""
    a = np.array(frame_a).astype(np.float32)
    b = np.array(frame_b).astype(np.float32)
    blended = a * (1 - alpha) + b * alpha
    return Image.fromarray(blended.astype(np.uint8))


def main():
    screenshots = load_screenshots()
    print(f"Loaded {len(screenshots)} screenshots")
    
    # Pre-render all framed screenshots
    frames = []
    for label, img in screenshots:
        framed = add_phone_frame(img)
        framed = draw_text(framed, label)
        frames.append(framed)
        print(f"  Framed: {label}")
    
    # Build video frames
    video_frames = []
    
    for i, frame in enumerate(frames):
        # Hold on current frame
        for _ in range(HOLD_FRAMES):
            video_frames.append(np.array(frame))
        
        # Transition to next frame
        if i < len(frames) - 1:
            next_frame = frames[i + 1]
            for t in range(TRANSITION_FRAMES):
                alpha = (t + 1) / TRANSITION_FRAMES
                blended = crossfade(frame, next_frame, alpha)
                video_frames.append(np.array(blended))
    
    # Add final hold on last frame
    for _ in range(HOLD_FRAMES):
        video_frames.append(np.array(frames[-1]))
    
    total_duration = len(video_frames) / FPS
    print(f"Total frames: {len(video_frames)} @ {FPS}fps = {total_duration:.1f}s")
    
    # Write MP4
    writer = imageio.get_writer(str(OUTPUT_PATH), fps=FPS, codec='libx264', quality=8)
    for f in video_frames:
        writer.append_data(f)
    writer.close()
    
    file_size_mb = OUTPUT_PATH.stat().st_size / (1024 * 1024)
    print(f"Saved: {OUTPUT_PATH}")
    print(f"Size:  {file_size_mb:.1f} MB")
    print(f"Ready for YouTube upload!")


if __name__ == "__main__":
    main()
