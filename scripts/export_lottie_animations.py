"""
Export high-accuracy 26 stretch animations from kinematic specs into valid Lottie JSON files.
Embeds animationData directly into preview_lottie.html so it loads instantly without file:// CORS blocks.
"""

import json
import math
from pathlib import Path

PROJECT_ROOT = Path("I:/Projects/LowerBackStretching")
STRETCHES_FILE = PROJECT_ROOT / "content" / "stretches.json"
LOTTIE_DIR = PROJECT_ROOT / "assets" / "lottie"
LOTTIE_DIR.mkdir(parents=True, exist_ok=True)

# Color Palette (Calm Sage, Indigo & Cream Palette)
PRIMARY_COLOR = [0.18, 0.36, 0.67, 1]     # #2D5DAA Calm Indigo/Blue
SAGE_COLOR = [0.36, 0.48, 0.40, 1]        # #5C7A65 Sage
ACCENT_COLOR = [0.42, 0.68, 0.58, 1]      # #6BAE94 Soft Teal
CREAM_MAT = [0.96, 0.94, 0.90, 1]         # #F5F0E6 Beige Mat


def build_lottie_spec(stretch_id, stretch_name, animation_data):
    loop_seconds = animation_data.get("loopSeconds", 4.5)
    poses = animation_data.get("poses", [])
    if len(poses) < 2:
        return None

    fps = 60
    total_frames = int(loop_seconds * fps)
    
    # 500x500 Canvas
    w, h = 500, 500
    scale = 360
    offset_x = 70
    offset_y = 70

    # Skeleton connections (bones)
    bones = [
        ("hip", "spineMid"),
        ("spineMid", "neck"),
        ("neck", "shoulder"),
        ("shoulder", "elbow"),
        ("elbow", "hand"),
        ("hip", "knee"),
        ("knee", "foot"),
    ]

    # Handle optional left limbs if present
    if "shoulderL" in poses[0]["joints"]:
        bones.extend([
            ("neck", "shoulderL"),
            ("shoulderL", "elbowL"),
            ("elbowL", "handL"),
            ("hip", "kneeL"),
            ("kneeL", "footL"),
        ])

    layers = []
    layer_idx = 1

    # 1. Head Layer
    head_keyframes = []
    num_poses = len(poses)
    pose_frames = [int(i * total_frames / num_poses) for i in range(num_poses)]
    pose_frames.append(total_frames)

    for i in range(num_poses):
        cur_p = poses[i]["joints"]["head"]
        next_p = poses[(i + 1) % num_poses]["joints"]["head"]
        t_start = pose_frames[i]
        t_end = pose_frames[i + 1]

        x1 = cur_p[0] * scale + offset_x
        y1 = cur_p[1] * scale + offset_y
        x2 = next_p[0] * scale + offset_x
        y2 = next_p[1] * scale + offset_y

        head_keyframes.append({
            "t": t_start,
            "s": [x1, y1, 0],
            "e": [x2, y2, 0],
            "i": {"x": [0.42, 0.42, 0.42], "y": [1, 1, 1]},
            "o": {"x": [0.58, 0.58, 0.58], "y": [0, 0, 0]}
        })

    head_layer = {
        "ddd": 0, "ind": layer_idx, "ty": 4, "nm": "Head",
        "sr": 1, "ks": {
            "o": {"k": 100},
            "r": {"k": 0},
            "p": {"a": 1, "k": head_keyframes},
            "a": {"k": [0, 0, 0]},
            "s": {"k": [100, 100, 100]}
        },
        "shapes": [
            {
                "ty": "gr", "nm": "HeadGroup",
                "it": [
                    {
                        "ty": "el", "nm": "HeadCircle",
                        "p": {"k": [0, 0]},
                        "s": {"k": [36, 36]}
                    },
                    {
                        "ty": "fl", "nm": "HeadFill",
                        "c": {"k": ACCENT_COLOR},
                        "o": {"k": 100}
                    },
                    {
                        "ty": "st", "nm": "HeadStroke",
                        "c": {"k": PRIMARY_COLOR},
                        "o": {"k": 100},
                        "w": {"k": 8},
                        "lc": 2, "lj": 2
                    },
                    {"ty": "tr", "p": {"k": [0, 0]}, "a": {"k": [0, 0]}, "s": {"k": [100, 100]}, "r": {"k": 0}, "o": {"k": 100}}
                ]
            }
        ],
        "ip": 0, "op": total_frames, "st": 0, "bm": 0
    }
    layers.append(head_layer)
    layer_idx += 1

    # 2. Bone Layers (Limbs & Spine)
    for b_idx, (j1, j2) in enumerate(bones):
        path_keyframes = []
        for i in range(num_poses):
            p1_a = poses[i]["joints"][j1]
            p2_a = poses[i]["joints"][j2]
            p1_b = poses[(i + 1) % num_poses]["joints"][j1]
            p2_b = poses[(i + 1) % num_poses]["joints"][j2]

            t_start = pose_frames[i]
            t_end = pose_frames[i + 1]

            x1_a, y1_a = p1_a[0] * scale + offset_x, p1_a[1] * scale + offset_y
            x2_a, y2_a = p2_a[0] * scale + offset_x, p2_a[1] * scale + offset_y

            x1_b, y1_b = p1_b[0] * scale + offset_x, p1_b[1] * scale + offset_y
            x2_b, y2_b = p2_b[0] * scale + offset_x, p2_b[1] * scale + offset_y

            path_keyframes.append({
                "t": t_start,
                "s": [{"i": [[0, 0], [0, 0]], "o": [[0, 0], [0, 0]], "v": [[x1_a, y1_a], [x2_a, y2_a]], "c": False}],
                "e": [{"i": [[0, 0], [0, 0]], "o": [[0, 0], [0, 0]], "v": [[x1_b, y1_b], [x2_b, y2_b]], "c": False}],
                "i": {"x": 0.42, "y": 1},
                "o": {"x": 0.58, "y": 0}
            })

        stroke_width = 14 if "spine" in j1 or "neck" in j1 else 10
        bone_color = PRIMARY_COLOR if "spine" in j1 or "neck" in j1 else SAGE_COLOR

        bone_layer = {
            "ddd": 0, "ind": layer_idx, "ty": 4, "nm": f"Bone_{j1}_{j2}",
            "sr": 1, "ks": {
                "o": {"k": 100},
                "r": {"k": 0},
                "p": {"k": [0, 0, 0]},
                "a": {"k": [0, 0, 0]},
                "s": {"k": [100, 100, 100]}
            },
            "shapes": [
                {
                    "ty": "gr", "nm": "Limb",
                    "it": [
                        {
                            "ty": "sh", "nm": "Path",
                            "ks": {"a": 1, "k": path_keyframes}
                        },
                        {
                            "ty": "st", "nm": "Stroke",
                            "c": {"k": bone_color},
                            "o": {"k": 100},
                            "w": {"k": stroke_width},
                            "lc": 2, "lj": 2
                        },
                        {"ty": "tr", "p": {"k": [0, 0]}, "a": {"k": [0, 0]}, "s": {"k": [100, 100]}, "r": {"k": 0}, "o": {"k": 100}}
                    ]
                }
            ],
            "ip": 0, "op": total_frames, "st": 0, "bm": 0
        }
        layers.append(bone_layer)
        layer_idx += 1

    # 3. Yoga Mat Layer
    mat_layer = {
        "ddd": 0, "ind": layer_idx, "ty": 4, "nm": "YogaMat",
        "sr": 1, "ks": {
            "o": {"k": 100},
            "r": {"k": 0},
            "p": {"k": [250, 440, 0]},
            "a": {"k": [0, 0, 0]},
            "s": {"k": [100, 100, 100]}
        },
        "shapes": [
            {
                "ty": "gr", "nm": "MatGroup",
                "it": [
                    {
                        "ty": "rc", "nm": "MatRect",
                        "p": {"k": [0, 0]},
                        "s": {"k": [400, 10]},
                        "r": {"k": 5}
                    },
                    {
                        "ty": "fl", "nm": "MatFill",
                        "c": {"k": CREAM_MAT},
                        "o": {"k": 100}
                    },
                    {"ty": "tr", "p": {"k": [0, 0]}, "a": {"k": [0, 0]}, "s": {"k": [100, 100]}, "r": {"k": 0}, "o": {"k": 100}}
                ]
            }
        ],
        "ip": 0, "op": total_frames, "st": 0, "bm": 0
    }
    layers.append(mat_layer)

    lottie_json = {
        "v": "5.7.4",
        "fr": fps,
        "ip": 0,
        "op": total_frames,
        "w": w,
        "h": h,
        "nm": f"{stretch_name} Animation",
        "ddd": 0,
        "assets": [],
        "layers": layers
    }
    return lottie_json


def main():
    with open(STRETCHES_FILE, "r", encoding="utf-8") as f:
        stretches = json.load(f)

    exported_count = 0
    stretch_ids = []
    lottie_dict = {}

    for s in stretches:
        s_id = s.get("id")
        name = s.get("name")
        anim = s.get("animation")
        if not anim:
            continue

        lottie_obj = build_lottie_spec(s_id, name, anim)
        if lottie_obj:
            out_file = LOTTIE_DIR / f"{s_id}.json"
            with open(out_file, "w", encoding="utf-8") as f_out:
                json.dump(lottie_obj, f_out, indent=2)
            exported_count += 1
            stretch_ids.append((s_id, name))
            lottie_dict[s_id] = lottie_obj
            print(f"Exported Lottie: {s_id}.json ({out_file.stat().st_size / 1024:.1f} KB)")

    # Inline JSON directly to avoid local file:// CORS restrictions in Chrome/Edge
    all_lottie_json_str = json.dumps(lottie_dict)

    html_content = f"""<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>26 Lottie Stretch Animations Preview</title>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/bodymovin/5.7.4/lottie.min.js"></script>
    <style>
        body {{
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
            background: #F4F3EE;
            margin: 0;
            padding: 24px;
            color: #2D3748;
        }}
        h1 {{
            font-size: 24px;
            margin-bottom: 8px;
            font-weight: 700;
        }}
        p {{
            color: #718096;
            margin-bottom: 24px;
        }}
        .grid {{
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
            gap: 20px;
        }}
        .card {{
            background: #FFFFFF;
            border-radius: 16px;
            padding: 16px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.05);
            display: flex;
            flex-direction: column;
            align-items: center;
        }}
        .anim-container {{
            width: 240px;
            height: 240px;
            background: #FAFAF8;
            border-radius: 12px;
        }}
        .title {{
            font-weight: 600;
            font-size: 16px;
            margin-top: 12px;
        }}
        .badge {{
            font-size: 12px;
            color: #4A5568;
            background: #EDF2F7;
            padding: 2px 8px;
            border-radius: 99px;
            margin-top: 4px;
        }}
    </style>
</head>
<body>
    <h1>Lower Back Stretching — 26 Lottie Animations Preview</h1>
    <p>High-accuracy forward kinematics • 60 FPS Vector Animations • 100% Seamless Loops</p>
    <div class="grid">
"""

    for s_id, s_name in stretch_ids:
        html_content += f"""
        <div class="card">
            <div class="anim-container" id="lottie_{s_id}"></div>
            <div class="title">{s_name}</div>
            <div class="badge">{s_id}.json</div>
        </div>
        """

    html_content += f"""
    </div>
    <script>
        const allAnimations = {all_lottie_json_str};
        Object.keys(allAnimations).forEach(id => {{
            const el = document.getElementById('lottie_' + id);
            if (el) {{
                lottie.loadAnimation({{
                    container: el,
                    renderer: 'svg',
                    loop: true,
                    autoplay: true,
                    animationData: allAnimations[id]
                }});
            }}
        }});
    </script>
</body>
</html>
"""
    preview_file = LOTTIE_DIR / "preview_lottie.html"
    with open(preview_file, "w", encoding="utf-8") as f:
        f.write(html_content)

    print(f"\nSuccessfully generated {exported_count} Lottie animations with inline preview!")
    print(f"Interactive preview updated at: {preview_file}")


if __name__ == "__main__":
    main()
