"""Batch video generator for Lower Back Stretching using Replicate AI models."""

import argparse
import json
import os
import sys
import time
from pathlib import Path
import requests
import replicate

REPLICATE_API_TOKEN = os.environ.get("REPLICATE_API_TOKEN", "")
if REPLICATE_API_TOKEN:
    os.environ["REPLICATE_API_TOKEN"] = REPLICATE_API_TOKEN

PROJECT_ROOT = Path(__file__).resolve().parent.parent
STRETCHES_FILE = PROJECT_ROOT / "content" / "stretches.json"
OUTPUT_DIR = PROJECT_ROOT / "assets" / "videos"
OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

# Master Instructor Reference Image (Hosted URL)
DEFAULT_IMAGE_URL = "https://replicate.delivery/xezq/vMdkvJsNQpaBG1QI9Yb7eWmbKLTenzryG7eMoe9XFdpx2gPcB/tmp8ulyufsg.jpg"

# Exercise Action Prompt Templates
PROMPT_TEMPLATES = {
    "cat-cow": "A female yoga instructor on hands and knees performing Cat-Cow stretch, gently arching her back into cow and rounding her spine upward into cat pose, slow controlled motion, side camera angle.",
    "child-pose": "A female yoga instructor kneeling on a yoga mat, sitting back on her heels and folding forward with arms extended long on the mat into Child's Pose, relaxing posture, side view.",
    "knee-to-chest": "A female fitness model lying on her back on a yoga mat, gently pulling one knee to her chest while holding the shin with both hands, slow relaxation, side angle.",
    "supine-twist": "A female fitness model lying on her back on a mat, arms stretched out wide in a T-shape, gently lowering both bent knees together to the side into a spinal twist.",
    "pelvic-tilt": "A female fitness instructor lying on her back with knees bent and feet flat, performing gentle pelvic tilts by flattening her lower back against the mat.",
    "sphinx": "A female yoga instructor lying on her stomach, propping herself up on her forearms with elbows under shoulders, gently lifting her chest into Sphinx Pose, side view.",
    "seated-forward-fold": "A female yoga instructor sitting on a mat with straight legs, hinging forward from her hips and reaching toward her toes with a long spine.",
    "standing-forward-fold": "A female instructor standing tall, then hinging forward from her hips with soft knees, letting her upper body hang in a standing forward fold.",
    "figure-four": "A female fitness instructor lying on her back, crossing one ankle over the opposite knee and gently pulling her bottom thigh toward her chest in a figure-four glute stretch.",
    "pigeon": "A female yoga instructor on a mat in Pigeon Pose, one bent shin forward and the other leg extended straight behind, folding her upper body forward gently.",
    "low-lunge": "A female instructor in a deep low lunge on a mat, front knee bent 90 degrees, back knee grounded, torso upright, sinking hips into a hip flexor stretch.",
    "butterfly": "A female yoga instructor seated on a mat, pressing the soles of her feet together with knees open wide, gently holding her feet and leaning forward.",
    "happy-baby": "A female instructor lying on her back, holding the outside edges of her feet with knees bent wide toward the armpits, gently rocking in Happy Baby pose.",
    "downward-dog": "A female yoga instructor holding a stable Downward-Facing Dog pose in an inverted V shape, hands and feet on the mat, lengthening spine and hamstrings.",
    "cobra": "A female instructor lying on her stomach, hands under shoulders, smoothly pressing her upper body up with hips on the mat into Cobra Pose.",
    "wall-hamstring": "A female instructor lying on her back near a wall with legs extended vertically up against the wall in a relaxing hamstring stretch.",
    "quad-stretch": "A female instructor standing tall on one leg, holding the ankle of the other leg behind her to stretch her quadriceps, clean posture.",
    "calf-stretch": "A female instructor standing with hands against a wall, one foot stepped back with heel pressed flat into the floor stretching the calf.",
    "thread-the-needle": "A female instructor on hands and knees, sliding one arm underneath her torso to rest her shoulder and head on the mat in Thread the Needle stretch.",
    "glute-bridge": "A female instructor lying on her back with knees bent, pressing through her heels to lift her hips into a straight glute bridge, then lowering with control.",
    "hip-flexor-lunge": "A female instructor in a half-kneeling hip flexor lunge, pelvis tucked slightly, leaning gently forward into the front leg.",
    "reclined-hand-to-toe": "A female instructor lying on her back, holding a yoga strap around the ball of one foot and extending that leg straight upward.",
    "standing-side-bend": "A female instructor standing tall with feet together, reaching one arm up and overhead, gently leaning to the side in a lateral stretch.",
    "crocodile": "A female instructor lying flat on her stomach, forearms stacked under her forehead, breathing deeply and relaxing in Crocodile Pose.",
    "it-band-cross": "A female instructor standing, crossing one leg behind the other and reaching the same-side arm overhead into a standing IT band stretch.",
    "seated-twist": "A female instructor sitting with one knee bent across the other, gently twisting her torso toward the bent knee with an upright spine."
}


def generate_stretch_video(stretch_id: str, image_url: str = DEFAULT_IMAGE_URL, model: str = "kwaivgi/kling-v1.6-standard"):
    """Generate a single video for a stretch ID."""
    prompt = PROMPT_TEMPLATES.get(
        stretch_id,
        f"A female yoga instructor performing {stretch_id.replace('-', ' ')} stretch smoothly on a yoga mat in a sunlit studio, 4k photorealistic."
    )
    
    print(f"\n==========================================")
    print(f"Generating video for: {stretch_id}")
    print(f"Prompt: {prompt}")
    print(f"Model: {model}")
    print(f"==========================================")

    output_file = OUTPUT_DIR / f"{stretch_id}.mp4"
    if output_file.exists():
        print(f"File {output_file} already exists. Skipping.")
        return str(output_file)

    try:
        if "kling" in model:
            output = replicate.run(
                model,
                input={
                    "prompt": prompt,
                    "start_image": image_url,
                    "duration": 5,
                    "aspect_ratio": "16:9"
                }
            )
        elif "minimax" in model:
            output = replicate.run(
                model,
                input={
                    "prompt": prompt,
                    "first_frame_image": image_url
                }
            )
        else:
            output = replicate.run(
                model,
                input={
                    "prompt": prompt,
                    "image": image_url
                }
            )

        video_url = str(output)
        print(f"Replicate Result URL: {video_url}")

        # Download video
        resp = requests.get(video_url, stream=True)
        resp.raise_for_status()
        with open(output_file, "wb") as f:
            for chunk in resp.iter_content(chunk_size=8192):
                f.write(chunk)

        print(f"Successfully saved to: {output_file}")
        return str(output_file)

    except Exception as e:
        print(f"Error generating video for {stretch_id}: {e}")
        return None


def main():
    parser = argparse.ArgumentParser(description="Generate stretch demonstration videos via Replicate")
    parser.add_argument("--stretch", type=str, help="Specific stretch ID to generate (e.g. cat-cow)")
    parser.add_argument("--all", action="store_true", help="Generate all 26 stretches")
    parser.add_argument("--model", type=str, default="kwaivgi/kling-v1.6-standard", help="Replicate model identifier")
    parser.add_argument("--image", type=str, default=DEFAULT_IMAGE_URL, help="Instructor base image URL")
    args = parser.parse_args()

    with open(STRETCHES_FILE, "r", encoding="utf-8") as f:
        stretches = json.load(f)

    if args.stretch:
        generate_stretch_video(args.stretch, args.image, args.model)
    elif args.all:
        for s in stretches:
            s_id = s["id"]
            generate_stretch_video(s_id, args.image, args.model)
            time.sleep(2)
    else:
        print(f"Loaded {len(stretches)} stretches from {STRETCHES_FILE}.")
        print("Usage:")
        print("  python scripts/generate_all_stretch_videos.py --stretch cat-cow")
        print("  python scripts/generate_all_stretch_videos.py --all")


if __name__ == "__main__":
    main()
