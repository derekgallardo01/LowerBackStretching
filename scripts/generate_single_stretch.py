"""Generate a single high-quality, 10s, seamlessly loopable stretch video."""

import argparse
import json
import os
import subprocess
import time
from pathlib import Path
import requests
import replicate

REPLICATE_API_TOKEN = os.environ.get("REPLICATE_API_TOKEN", "")
if REPLICATE_API_TOKEN:
    os.environ["REPLICATE_API_TOKEN"] = REPLICATE_API_TOKEN

PROJECT_ROOT = Path("I:/Projects/LowerBackStretching")
OUTPUT_DIR = PROJECT_ROOT / "assets" / "videos"
OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
TEMP_DIR = PROJECT_ROOT / "assets" / "temp"
TEMP_DIR.mkdir(parents=True, exist_ok=True)

# Master Instructor Visual Style Profile
INSTRUCTOR_PROFILE = (
    "A beautiful, fit 25-year-old female yoga instructor with athletic toned physique, "
    "blonde hair tied in a sleek high ponytail, wearing a matching matte sage green seamless sports bra "
    "and high-waisted gym leggings. On a luxury neutral beige yoga mat in a modern bright minimalist sunlit studio "
    "with light oak wood floors and soft natural morning window light. Photorealistic 8k, 35mm lens, sharp focus."
)

# Pose-Specific Setup & Action Mapping
STRETCH_CONFIGS = {
    "child-pose": {
        "start_pose_prompt": (
            f"Side profile view, full body shot at yoga mat height. {INSTRUCTOR_PROFILE} "
            "She is kneeling upright on the beige yoga mat, sitting upright on her shins and heels with hands resting on thighs, "
            "straight posture, preparing to fold into Child's Pose."
        ),
        "video_prompt": (
            "Static side view camera at mat level. The female instructor kneeling on the mat slowly and smoothly folds her torso forward over her thighs, "
            "extending both arms straight out in front on the mat, resting her forehead gently down onto the mat in a peaceful Child's Pose. "
            "She takes a deep slow breath, holding the relaxed stretch, then smoothly rises back upright. Perfect yoga form, calm motion, 4k."
        )
    },
    "knee-to-chest": {
        "start_pose_prompt": (
            f"Side profile view at yoga mat level. {INSTRUCTOR_PROFILE} "
            "She is lying flat on her back on the beige yoga mat, actively performing the Knee-to-Chest stretch: "
            "her right knee is bent and drawn up tightly against her chest, with BOTH hands firmly clasped around her shin "
            "pulling the knee gently toward her torso. Her left leg is extended straight on the mat. Head and back resting flat on the mat."
        ),
        "video_prompt": (
            "Static side view camera at mat level. The female instructor lying on her back performs the Knee-to-Chest stretch: "
            "both hands firmly clasped around her bent shin, gently hugging the knee into her chest for a deep lower back and glute stretch. "
            "She breathes deeply, easing slightly on the inhale and gently hugging the knee closer to her chest on the exhale. "
            "Calm, steady stretching hold, perfect anatomical form, photorealistic 4k."
        )
    },
    "cobra": {
        "start_pose_prompt": (
            f"Side profile view, full body shot at mat height. {INSTRUCTOR_PROFILE} "
            "She is lying flat on her stomach (prone) on the beige yoga mat, legs straight together behind her, palms placed flat on the mat under her shoulders."
        ),
        "video_prompt": (
            "Static side view camera at mat level. The female instructor lying on her stomach gently presses through her palms, "
            "smoothly lifting her chest and upper torso off the mat into Cobra Pose while keeping her hips and pelvis grounded. "
            "She opens her chest with a gentle gaze forward, holds calmly, then lowers down slowly to the mat. Fluid motion, 4k."
        )
    },
    "figure-four": {
        "start_pose_prompt": (
            f"Side profile 45-degree angle, full body shot at mat level. {INSTRUCTOR_PROFILE} "
            "She is lying on her back on the yoga mat with knees bent and feet flat on the floor."
        ),
        "video_prompt": (
            "Static side view camera at mat level. The female instructor lying on her back crosses her right ankle over her left knee, "
            "reaches her hands through to hold the back of her left thigh, and gently pulls both legs toward her chest in a Figure-Four glute stretch. "
            "She holds the stretch with calm breathing, then gently lowers her feet back to the mat. Smooth controlled motion, 4k."
        )
    },
    "supine-twist": {
        "start_pose_prompt": (
            f"Side profile 45-degree angle, full body shot at mat level. {INSTRUCTOR_PROFILE} "
            "She is lying on her back on the yoga mat, knees bent together at 90 degrees with arms extended out wide in a T-shape."
        ),
        "video_prompt": (
            "Static side view camera at mat level. The female instructor lying on her back with arms extended wide slowly lowers both bent knees together "
            "to one side toward the floor into a gentle supine spinal twist, keeping both shoulders grounded on the mat. "
            "She holds calmly, then smoothly brings her knees back to center. 4k photorealistic."
        )
    }
}


def generate_stretch(stretch_id: str):
    config = STRETCH_CONFIGS.get(stretch_id)
    if not config:
        print(f"No config found for {stretch_id}")
        return

    print(f"\n=======================================================")
    print(f"Generating 10s Seamless Video for: {stretch_id}")
    print(f"=======================================================")

    # Step 1: Flux Starter Pose
    print("\n[Step 1/3] Generating mat-level starting pose image via Flux 1.1 Pro...")
    flux_output = replicate.run(
        "black-forest-labs/flux-1.1-pro",
        input={
            "prompt": config["start_pose_prompt"],
            "aspect_ratio": "16:9",
            "output_format": "jpg",
            "output_quality": 95
        }
    )
    starter_image_url = str(flux_output)
    print(f"Starter Image URL: {starter_image_url}")

    # Save starter image
    start_img_path = TEMP_DIR / f"{stretch_id}_start.jpg"
    resp = requests.get(starter_image_url)
    with open(start_img_path, "wb") as f:
        f.write(resp.content)
    print(f"Saved starting image: {start_img_path}")

    # Step 2: Kling 10s Video
    print("\n[Step 2/3] Generating 10s video with Kling 1.6 Standard...")
    video_output = replicate.run(
        "kwaivgi/kling-v1.6-standard",
        input={
            "prompt": config["video_prompt"],
            "start_image": starter_image_url,
            "duration": 10,
            "aspect_ratio": "16:9"
        }
    )
    raw_video_url = str(video_output)
    print(f"Raw 10s Video URL: {raw_video_url}")

    # Download raw video
    raw_video_path = TEMP_DIR / f"{stretch_id}_raw.mp4"
    v_resp = requests.get(raw_video_url)
    with open(raw_video_path, "wb") as f:
        f.write(v_resp.content)
    print(f"Saved raw video: {raw_video_path}")

    # Step 3: FFmpeg Seamless Crossfade
    print("\n[Step 3/3] Processing seamless loop with FFmpeg...")
    final_output_path = OUTPUT_DIR / f"{stretch_id}.mp4"

    cmd = [
        "ffmpeg", "-y",
        "-i", str(raw_video_path),
        "-filter_complex",
        "[0:v]split[v1][v2];[v1]trim=0:9.25,setpts=PTS-STARTPTS[v1t];[v2]trim=9.25:10,setpts=PTS-STARTPTS[v2t];[v2t][v1t]xfade=transition=fade:duration=0.75:offset=0[outv]",
        "-map", "[outv]",
        "-c:v", "libx264",
        "-pix_fmt", "yuv420p",
        "-crf", "18",
        str(final_output_path)
    ]

    subprocess.run(cmd, check=True)
    print(f"\nSUCCESS! Seamless loop saved to: {final_output_path}")
    print(f"Video URL on Replicate: {raw_video_url}")


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--stretch", type=str, default="child-pose")
    args = parser.parse_args()
    generate_stretch(args.stretch)
