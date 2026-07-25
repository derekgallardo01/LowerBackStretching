#!/usr/bin/env python3
"""
Authoring tool for the 26 stretch animations. Writes the `animation` field
into all three stretches.json copies (content, android assets, iOS resources)
in place, preserving each file's existing fields.

How it works:
- Each stretch is authored as a list of poses defined by JOINT ANGLES rather
  than raw positions. `pose_from_angles` walks the bone topology from the
  hip and computes each child joint as `parent + length * (cos θ, sin θ)`.
- Because all poses for one stretch share the same skeleton lengths, the
  bone lengths are mathematically identical between keyframes — eliminating
  the limb-stretches-mid-loop bug that plagued the raw-position version.
- The verifier double-checks at write time (tolerance 3%); any future
  raw-position regression aborts the script before any JSON is written.

Run from repo root: `python scripts/inject_animations.py`
Emits `scripts/preview.html` — open in any browser to visually QA all 26.

Angle convention: degrees, 0 = +x (right), 90 = +y (down because origin is
top-left), 180 = left, 270 = up. So for a person STANDING straight, the
spine angles "up" = -90, arms "down" = 90, etc.
"""
from __future__ import annotations

import json
import math
from copy import deepcopy
from pathlib import Path
from typing import Dict, List, Tuple

# ---------------------------------------------------------------------------
# Skeleton.
# ---------------------------------------------------------------------------

PARENT_OF: Dict[str, str] = {
    "spineMid": "hip",
    "neck": "spineMid",
    "head": "neck",
    "shoulder": "neck",
    "elbow": "shoulder",
    "hand": "elbow",
    "knee": "hip",
    "foot": "knee",
    # Optional left-side limbs for front-view stretches (butterfly etc).
    # Side-view stretches simply omit these and the renderer skips them.
    "shoulderL": "neck",
    "elbowL": "shoulderL",
    "handL": "elbowL",
    "kneeL": "hip",
    "footL": "kneeL",
}
# Parents must be walked before children. Optional joints come after the
# core set so an author can include them only when needed.
FK_ORDER: List[str] = [
    "spineMid", "neck", "head", "shoulder", "elbow", "hand", "knee", "foot",
    "shoulderL", "elbowL", "handL", "kneeL", "footL",
]
SKELETON_BONES: List[Tuple[str, str]] = [(PARENT_OF[c], c) for c in FK_ORDER]

# Canonical humanoid proportions. Total standing height from foot to head =
# 0.16+0.16+0.13+0.13+0.08 = 0.66, plus head radius ~0.05 = 0.71 — fits a
# 0..1 canvas with margin. Arm = 0.26, leg = 0.32, torso = 0.26 + head.
# Left side mirrors the right (same lengths).
SKELETON_LENGTHS: Dict[str, float] = {
    "hip->spineMid": 0.13,
    "spineMid->neck": 0.13,
    "neck->head": 0.08,
    "neck->shoulder": 0.05,
    "shoulder->elbow": 0.13,
    "elbow->hand": 0.13,
    "hip->knee": 0.16,
    "knee->foot": 0.16,
    "neck->shoulderL": 0.05,
    "shoulderL->elbowL": 0.13,
    "elbowL->handL": 0.13,
    "hip->kneeL": 0.16,
    "kneeL->footL": 0.16,
}


def pose_from_angles(
    hip: Tuple[float, float],
    angles: Dict[str, float],
    lengths: Dict[str, float] | None = None,
    name: str | None = None,
) -> Dict:
    """Forward kinematics. Returns {name, joints}. Angles in degrees per the
    convention in the module docstring; keys are `"parent->child"` strings
    (note ASCII `->` not `→` so the source stays portable on cp1252 shells)."""
    lens = lengths if lengths is not None else SKELETON_LENGTHS
    joints: Dict[str, List[float]] = {"hip": [float(hip[0]), float(hip[1])]}
    for child in FK_ORDER:
        parent = PARENT_OF[child]
        bone_key = f"{parent}->{child}"
        # Optional joints (e.g. left-side limbs for side-view poses) are
        # simply omitted when no angle is provided. The renderer skips
        # any bone whose endpoints aren't present.
        if bone_key not in angles:
            continue
        length = lens[bone_key]
        theta = math.radians(angles[bone_key])
        px, py = joints[parent]
        joints[child] = [
            round(px + length * math.cos(theta), 4),
            round(py + length * math.sin(theta), 4),
        ]
    result: Dict = {"joints": joints}
    if name is not None:
        result = {"name": name, "joints": joints}
    return result


def merge(*angle_dicts: Dict[str, float]) -> Dict[str, float]:
    """Right-to-left override merge: `merge(BASE, {"hip->knee": 45})` returns
    a new dict with BASE's angles, overridden where specified."""
    out: Dict[str, float] = {}
    for d in angle_dicts:
        out.update(d)
    return out


# ---------------------------------------------------------------------------
# Base poses — each is a complete set of 8 bone angles for a common body
# position. Author per-stretch poses by `merge(BASE, {<changes>})`.
# ---------------------------------------------------------------------------

# Standing tall, arms hanging at sides, head up, legs straight down.
STAND = {
    "hip->spineMid": -90, "spineMid->neck": -90, "neck->head": -90,
    "neck->shoulder": 90,                       # shoulder hangs BELOW neck — arm attaches to the trunk, not the head
    "shoulder->elbow": 90, "elbow->hand": 90,   # arm hangs straight down
    "hip->knee": 90, "knee->foot": 90,          # leg straight down
}

# Lying on back (supine), head to the RIGHT (+x), arms at sides on the floor.
# Body is horizontal so all spine angles are 0 (extending +x toward head).
SUPINE = {
    "hip->spineMid": 0, "spineMid->neck": 0, "neck->head": 0,
    "neck->shoulder": 180,                      # shoulder is BEHIND neck along body
    "shoulder->elbow": 90, "elbow->hand": 90,   # arm flat at side along belly
    "hip->knee": 180, "knee->foot": 180,        # leg extends to the LEFT (away from head)
}

# Lying face down (prone), head to the RIGHT, forearms tucked in.
PRONE = {
    "hip->spineMid": 0, "spineMid->neck": 0, "neck->head": 0,
    "neck->shoulder": 180,
    "shoulder->elbow": 100, "elbow->hand": 10,  # forearm: elbow down, hand forward (under chest)
    "hip->knee": 180, "knee->foot": 180,
}

# Hands-and-knees / quadruped, facing RIGHT. Hips and shoulders at the
# "table" level; arms drop down to hands on floor, legs drop down to knees
# with shins folded back behind. The arm is BENT (elbow forward, forearm
# back) so the hand lands at knee-level y rather than overshooting past
# the floor — the shoulder is only 0.11 above the knee line so a straight
# arm (90/90, length 0.26) would punch through.
QUADRUPED = {
    "hip->spineMid": 0, "spineMid->neck": 0, "neck->head": 0,
    "neck->shoulder": 90,                       # shoulder hangs DOWN from neck (table-top topology)
    "shoulder->elbow": 25, "elbow->hand": 155,  # bent: elbow fwd-down then back-down, hand under shoulder at floor
    "hip->knee": 90, "knee->foot": 180,         # thigh down, shin back (left)
}

# Seated on the floor, legs extended forward (right). Torso upright.
SEATED = {
    "hip->spineMid": -90, "spineMid->neck": -90, "neck->head": -90,
    "neck->shoulder": 90,                       # shoulder below neck
    "shoulder->elbow": 90, "elbow->hand": 90,
    "hip->knee": 0, "knee->foot": 0,            # legs straight forward (right)
}

# Kneeling lunge: back knee on ground, front knee bent. Torso upright.
# We draw the BACK leg in side view: thigh straight down so the knee
# sits on the floor under the hip, shin extends straight back to the
# foot. This puts knee_y = foot_y = hip_y + 0.16 so there's a single
# consistent "floor" line rather than the previous Z-shaped leg.
LUNGE = {
    "hip->spineMid": -90, "spineMid->neck": -90, "neck->head": -90,
    "neck->shoulder": 90,                       # shoulder below neck
    "shoulder->elbow": 90, "elbow->hand": 90,
    "hip->knee": 90, "knee->foot": 180,         # back knee on floor, shin straight back
}


# ---------------------------------------------------------------------------
# ANIMATIONS dict — built via the _fk helper which records each stretch as
# (loop_seconds, [(pose_name, angles_dict), ...]) for a given hip.
# ---------------------------------------------------------------------------

ANIMATIONS: Dict = {}


def _fk(
    sid: str,
    loop_seconds: float,
    hip: Tuple[float, float],
    poses: List[Tuple[str, Dict[str, float]]],
    lengths: Dict[str, float] = SKELETON_LENGTHS,
) -> None:
    ANIMATIONS[sid] = {
        "loopSeconds": loop_seconds,
        "poses": [pose_from_angles(hip, ang, lengths=lengths, name=n) for n, ang in poses],
    }


# Compact aliases — saves vertical space when overriding only a few angles.
def m(*dicts) -> Dict[str, float]:
    return merge(*dicts)


# =============================================================================
# The 26 stretches.
# =============================================================================

# Cat-Cow — quadruped, alternating spine sag (cow) and spine arch (cat).
# Spine arches via spineMid angle; head looks up for cow, tucks for cat.
_fk("cat-cow", 4.5, hip=(0.30, 0.50), poses=[
    # Cow: spine sags down, head looks up. spineMid angle slightly down then
    # back up; head angle continues "up and forward" past the neck.
    ("cow", m(QUADRUPED, {
        "hip->spineMid": 30, "spineMid->neck": -30,    # spine bows DOWN
        "neck->head": -30,                              # head looks UP
    })),
    ("cat", m(QUADRUPED, {
        "hip->spineMid": -30, "spineMid->neck": 30,    # spine arches UP
        "neck->head": 60,                               # head TUCKS down
    })),
])

# Child's Pose — kneeling, fold forward over thighs with arms extended to
# the mat. Spine angles cascade forward-and-down so the head lands at the
# floor (y≈0.94) instead of floating mid-canvas. Subtle inhale/exhale
# variation: exhale settles a few degrees deeper.
_fk("child-pose", 5.5, hip=(0.20, 0.78), poses=[
    ("inhale", m(QUADRUPED, {
        "hip->spineMid": 20, "spineMid->neck": 30,     # body folds forward-and-down toward mat
        "neck->head": 40,                               # head reaches the floor
        "shoulder->elbow": 0, "elbow->hand": 0,        # arms stretch forward along the mat
        "hip->knee": 90, "knee->foot": 180,
    })),
    ("exhale", m(QUADRUPED, {
        "hip->spineMid": 22, "spineMid->neck": 32,
        "neck->head": 42,
        "shoulder->elbow": 0, "elbow->hand": 0,
        "hip->knee": 90, "knee->foot": 180,
    })),
])

# Knee to Chest — supine, leg flat then drawn up to chest while arm grabs knee.
_fk("knee-to-chest", 4.5, hip=(0.55, 0.55), poses=[
    ("extended", m(SUPINE, {
        "shoulder->elbow": 180, "elbow->hand": 180,    # arm flat at side
        "hip->knee": 180, "knee->foot": 180,           # leg extended back-left
    })),
    ("pulled", m(SUPINE, {
        "shoulder->elbow": -150, "elbow->hand": -120,  # arm reaches up to grab knee
        "hip->knee": -70, "knee->foot": 110,           # thigh up-fwd, shin folds toward butt
    })),
])

# Supine Twist — supine, knees fall to one side. Side view, knees rotate.
_fk("supine-twist", 6.0, hip=(0.40, 0.55), poses=[
    ("knees-up", m(SUPINE, {
        "shoulder->elbow": 100, "elbow->hand": 90,
        "hip->knee": -60, "knee->foot": 60,            # knees up, shins back
    })),
    ("dropped", m(SUPINE, {
        "shoulder->elbow": 100, "elbow->hand": 90,
        "hip->knee": 150, "knee->foot": 70,            # knees fallen low to opposite side
    })),
])

# Pelvic Tilt — supine, knees bent, pelvis rotates (subtle motion).
_fk("pelvic-tilt", 4.5, hip=(0.55, 0.58), poses=[
    ("neutral", m(SUPINE, {
        "shoulder->elbow": 100, "elbow->hand": 100,
        "hip->knee": -110, "knee->foot": 80,           # knees bent up
    })),
    ("tucked", m(SUPINE, {
        "hip->spineMid": 5,                             # spine flattens, pelvis tucks
        "shoulder->elbow": 100, "elbow->hand": 100,
        "hip->knee": -100, "knee->foot": 90,
    })),
])

# Sphinx — prone, forearms on floor, chest lifts off the ground. Hip is
# pulled in from x=0.22 to x=0.35 so the back foot (hip_x − 0.32) lands
# at x=0.03 instead of off the left edge.
_fk("sphinx", 5.5, hip=(0.35, 0.82), poses=[
    ("lower", m(PRONE, {
        "hip->spineMid": -15, "spineMid->neck": -20,   # gentle upward curve
        "neck->head": -10,
        "shoulder->elbow": 143, "elbow->hand": 0,      # upper arm back-and-down to elbow on floor, forearm fwd
    })),
    ("lifted", m(PRONE, {
        "hip->spineMid": -40, "spineMid->neck": -50,   # chest higher
        "neck->head": -40,                              # head looks way up
        "shoulder->elbow": 120, "elbow->hand": 30,     # forearm anchored — angles shift to stay put
    })),
])

# Seated Forward Fold — sitting with legs extended, fold torso over legs.
# "fold" now actually folds: spine angles cascade forward-and-slightly-down
# so the head ends at foot-y level (≈0.66) — previously head sat at y=0.46,
# *above* the feet, which looked like sitting up rather than folding.
_fk("seated-forward-fold", 6.0, hip=(0.20, 0.60), poses=[
    ("upright", m(SEATED, {
        "hip->spineMid": -90, "spineMid->neck": -90, "neck->head": -90,
        "shoulder->elbow": 90, "elbow->hand": 90,      # arms hang down
    })),
    ("fold", m(SEATED, {
        "hip->spineMid": -15, "spineMid->neck": 10,    # torso folds forward over legs
        "neck->head": 30,                               # head drops toward shins
        "shoulder->elbow": 20, "elbow->hand": 20,      # arms reach forward past feet
    })),
])

# Standing Forward Fold — standing, fold at the hips.
_fk("standing-forward-fold", 6.0, hip=(0.50, 0.55), poses=[
    ("tall", m(STAND, {})),
    ("fold", m(STAND, {
        "hip->spineMid": -10, "spineMid->neck": 20,    # spine folds forward and down
        "neck->head": 60,                               # head drops down
        "shoulder->elbow": 90, "elbow->hand": 90,      # arms still hanging (now reaching toward floor)
    })),
])

# Figure Four — supine, one ankle crossed over the opposite knee.
# In "crossed" the hands clasp behind the lifted thigh: arm angles aim
# UP-AND-LEFT (toward the thigh midpoint between hip and knee) rather
# than straight up where they'd float free of the body.
_fk("figure-four", 5.0, hip=(0.45, 0.60), poses=[
    ("extended", m(SUPINE, {
        "shoulder->elbow": 100, "elbow->hand": 100,
        "hip->knee": 180, "knee->foot": 180,           # leg flat
    })),
    ("crossed", m(SUPINE, {
        "shoulder->elbow": -160, "elbow->hand": -160,  # arms reach forward-up to clasp behind thigh
        "hip->knee": -110, "knee->foot": -30,          # thigh up-back, shin crosses over
    })),
])

# Pigeon Pose — front leg bent under, back leg extended, torso folds forward.
_fk("pigeon", 6.0, hip=(0.40, 0.62), poses=[
    ("upright", m(SEATED, {
        "shoulder->elbow": 80, "elbow->hand": 80,      # hands rest near hips
        "hip->knee": 30, "knee->foot": -30,            # front leg bent under
    })),
    ("fold", {
        "hip->spineMid": -10, "spineMid->neck": 20,    # torso folds forward and down
        "neck->head": 40,
        "neck->shoulder": -90,
        "shoulder->elbow": 60, "elbow->hand": 60,
        "hip->knee": 30, "knee->foot": -30,
    }),
])

# Low Lunge — back knee down, front knee bent. Hands on floor or thigh.
# "shallow" inherits LUNGE's knee-on-floor leg; "deep" shifts the knee a
# few degrees back (hip→knee=100) so the back leg looks more extended
# while keeping the same horizontal shin.
_fk("low-lunge", 5.0, hip=(0.50, 0.62), poses=[
    ("shallow", m(LUNGE, {
        "shoulder->elbow": 90, "elbow->hand": 90,      # hands rest on front thigh
    })),
    ("deep", m(LUNGE, {
        "hip->spineMid": -75,                           # torso leans forward slightly
        "shoulder->elbow": 60, "elbow->hand": 60,      # hands move forward
        "hip->knee": 100,                               # back knee shifts behind hip
    })),
])

# Butterfly — seated, soles together, knees flap. FRONT view — both legs +
# both arms visible. Hands hold both feet from outside; knees splay
# symmetrically and drop deeper on the "knees-down" half of the loop.
_fk("butterfly", 4.0, hip=(0.50, 0.65), poses=[
    ("knees-up", {
        "hip->spineMid": -90, "spineMid->neck": -90, "neck->head": -90,
        # Right side (anatomical right, viewer's left).
        "neck->shoulder": 30, "shoulder->elbow": 100, "elbow->hand": 110,
        "hip->knee": 35, "knee->foot": 150,
        # Left side (mirror).
        "neck->shoulderL": 150, "shoulderL->elbowL": 80, "elbowL->handL": 70,
        "hip->kneeL": 145, "kneeL->footL": 30,
    }),
    ("knees-down", {
        "hip->spineMid": -90, "spineMid->neck": -90, "neck->head": -90,
        "neck->shoulder": 30, "shoulder->elbow": 110, "elbow->hand": 120,
        "hip->knee": 55, "knee->foot": 156,
        "neck->shoulderL": 150, "shoulderL->elbowL": 70, "elbowL->handL": 60,
        "hip->kneeL": 125, "kneeL->footL": 24,
    }),
])

# Happy Baby — supine, knees pulled up, hands hold feet from outside.
# Upper arm goes straight up (-90) and the forearm angles back IN-AND-UP
# (-120..-125) so the hand meets the lifted foot above the chest, rather
# than reaching up-and-AWAY where the original pose left it.
_fk("happy-baby", 4.5, hip=(0.45, 0.55), poses=[
    ("rest", m(SUPINE, {
        "shoulder->elbow": -90, "elbow->hand": -120,   # forearm bends back-IN toward the lifted foot
        "hip->knee": -80, "knee->foot": -45,           # knees up, feet up
    })),
    ("rock", m(SUPINE, {
        "shoulder->elbow": -95, "elbow->hand": -125,
        "hip->knee": -85, "knee->foot": -40,           # subtle rock motion
    })),
])

# Downward Dog — inverted V. Side view, hips up, hands and feet on floor.
_fk("downward-dog", 5.0, hip=(0.30, 0.20), poses=[
    ("settled", {
        "hip->spineMid": 30, "spineMid->neck": 30, "neck->head": 50,
        "neck->shoulder": 30,
        "shoulder->elbow": 50, "elbow->hand": 60,      # arms straight to hands on floor
        "hip->knee": 130, "knee->foot": 100,           # legs straight to feet on floor
    }),
    ("pedal", {
        "hip->spineMid": 30, "spineMid->neck": 30, "neck->head": 50,
        "neck->shoulder": 30,
        "shoulder->elbow": 50, "elbow->hand": 60,
        "hip->knee": 130, "knee->foot": 95,            # subtle calf pedaling
    }),
])

# Cobra — prone, push up onto hands, chest lifts higher than sphinx. Same
# hip-x shift as sphinx so the back foot stays on canvas. "Low" depicts a
# sphinx-style hold (forearm on floor — elbow back-and-down at 122°, hand
# extends forward at 0°); "high" pushes up to straight arms.
_fk("cobra", 5.5, hip=(0.35, 0.82), poses=[
    ("low", m(PRONE, {
        "hip->spineMid": -20, "spineMid->neck": -30,
        "neck->head": -20,
        "shoulder->elbow": 122, "elbow->hand": 0,      # forearm planted: elbow back-and-down, hand fwd
    })),
    ("high", m(PRONE, {
        "hip->spineMid": -60, "spineMid->neck": -65,
        "neck->head": -60,                              # head way up
        "shoulder->elbow": 90, "elbow->hand": 90,      # arms more vertical
    })),
])

# Wall Hamstring — supine with one leg pivoting up against an imaginary wall.
# Arm rests along the body toward the feet (170° = left-and-slight-down) so
# it reads as "arm on the floor" rather than punching through the canvas
# bottom — the body is at y=0.78, leaving only 0.22 of vertical headroom.
_fk("wall-hamstring", 5.0, hip=(0.40, 0.78), poses=[
    ("bent", m(SUPINE, {
        "shoulder->elbow": 170, "elbow->hand": 170,
        "hip->knee": -70, "knee->foot": -100,          # leg up with slight bend at knee
    })),
    ("straight", m(SUPINE, {
        "shoulder->elbow": 170, "elbow->hand": 170,
        "hip->knee": -90, "knee->foot": -90,           # leg fully extended straight up
    })),
])

# Quad Stretch — standing, hold one foot behind toward butt. Figure faces
# RIGHT, so "behind" is LEFT in screen coords; the foot rises up-and-left
# from the knee and the arm drops down-and-back to meet it.
_fk("quad-stretch", 5.5, hip=(0.45, 0.58), poses=[
    ("stand", m(STAND, {})),
    ("hold-foot", m(STAND, {
        "shoulder->elbow": 120, "elbow->hand": 130,    # arm down-and-back toward foot near butt
        "hip->knee": 100, "knee->foot": -120,           # thigh slightly behind vertical, shin folds up-back
    })),
])

# Calf Stretch — forward lunge against wall. Side view.
_fk("calf-stretch", 5.0, hip=(0.40, 0.62), poses=[
    ("ready", m(STAND, {
        "hip->spineMid": -85, "spineMid->neck": -85,   # torso slight lean forward
        "shoulder->elbow": 20, "elbow->hand": 20,      # arms reach forward to wall
    })),
    ("lunge", m(STAND, {
        "hip->spineMid": -75, "spineMid->neck": -75,   # torso leans more
        "shoulder->elbow": 10, "elbow->hand": 10,      # arms reach further out
        "hip->knee": 90, "knee->foot": 90,             # back leg straight
    })),
])

# Thread the Needle — quadruped with one arm threaded under the other.
_fk("thread-the-needle", 5.5, hip=(0.25, 0.50), poses=[
    ("baseline", m(QUADRUPED, {})),
    ("threaded", m(QUADRUPED, {
        "hip->spineMid": 10, "spineMid->neck": 30,     # spine twists, body curves
        "neck->head": 80,                               # head drops toward floor
        "neck->shoulder": 150,                          # shoulder dips down-fwd
        "shoulder->elbow": 170, "elbow->hand": 170,    # arm threads back along floor
    })),
])

# Glute Bridge — supine, knees bent, hips lift up. Because hip is the FK
# origin (fixed in canvas), the "lift" is depicted by everything else
# sloping AWAY from the hip: spine drops down-and-right toward head on
# floor, legs drop down-and-left toward foot on floor — making hip the
# clear high point of a wide arch in "up". Arms run along the body (170°)
# so they read as "at sides on the floor" without exceeding the canvas.
_fk("glute-bridge", 4.0, hip=(0.55, 0.62), poses=[
    ("down", m(SUPINE, {
        "shoulder->elbow": 170, "elbow->hand": 170,
        "hip->knee": -110, "knee->foot": 110,          # knees up, feet flat on floor next to body
    })),
    ("up", {
        "hip->spineMid": 45, "spineMid->neck": 45, "neck->head": 45,  # body slopes down to head on floor
        "neck->shoulder": 180,
        "shoulder->elbow": 170, "elbow->hand": 170,
        "hip->knee": 130, "knee->foot": 80,            # legs slope down-fwd, knee bent, foot on floor
    }),
])

# Hip Flexor Lunge — kneeling lunge, hips push forward. Same knee-on-floor
# leg as low-lunge; "forward" depicts the hip-push as a slight back-arch
# (spine angle past vertical) and a knee that drifts behind the hip as
# the body shifts weight forward.
_fk("hip-flexor-lunge", 5.0, hip=(0.45, 0.62), poses=[
    ("back", m(LUNGE, {
        "shoulder->elbow": 90, "elbow->hand": 90,      # hands rest on front thigh
    })),
    ("forward", m(LUNGE, {
        "hip->spineMid": -100,                          # hips push forward, slight back-bend
        "shoulder->elbow": 90, "elbow->hand": 90,
        "hip->knee": 110,                               # knee drifts behind hip
    })),
])

# Reclined Hand-to-Toe — supine, leg lifted straight, arm reaches to foot.
_fk("reclined-hand-to-toe", 5.0, hip=(0.40, 0.78), poses=[
    ("low", m(SUPINE, {
        "shoulder->elbow": -90, "elbow->hand": -85,    # arm reaches up
        "hip->knee": -75, "knee->foot": -85,           # leg partially raised
    })),
    ("high", m(SUPINE, {
        "shoulder->elbow": -90, "elbow->hand": -90,
        "hip->knee": -90, "knee->foot": -90,           # leg straight up
    })),
])

# Standing Side Bend — FRONT view, both arms overhead, body sways right.
# Right arm extends up; left arm grips the right wrist overhead. Body bends
# at the spine so the head and torso tilt to the right of the canvas.
_fk("standing-side-bend", 5.5, hip=(0.50, 0.62), poses=[
    ("center", {
        "hip->spineMid": -90, "spineMid->neck": -90, "neck->head": -90,
        "neck->shoulder": 30, "shoulder->elbow": -90, "elbow->hand": -90,
        "neck->shoulderL": 150, "shoulderL->elbowL": -90, "elbowL->handL": -90,
        "hip->knee": 80, "knee->foot": 90,
        "hip->kneeL": 100, "kneeL->footL": 90,
    }),
    ("right", {
        "hip->spineMid": -70, "spineMid->neck": -60, "neck->head": -50,
        "neck->shoulder": 40, "shoulder->elbow": -70, "elbow->hand": -70,
        "neck->shoulderL": 160, "shoulderL->elbowL": -80, "elbowL->handL": -75,
        "hip->knee": 80, "knee->foot": 90,
        "hip->kneeL": 100, "kneeL->footL": 90,
    }),
])

# Crocodile — prone, head rests on stacked hands. Subtle breathing motion.
# Hip shifted in (x=0.32) so the back foot doesn't run off the left edge.
_fk("crocodile", 6.0, hip=(0.32, 0.80), poses=[
    ("inhale", m(PRONE, {
        "hip->spineMid": -3, "spineMid->neck": -8,     # gentle rise
        "neck->head": -20,
        "shoulder->elbow": 169, "elbow->hand": 0,      # forearms folded under chest on floor
    })),
    ("exhale", m(PRONE, {
        "hip->spineMid": 0, "spineMid->neck": -5,      # settled
        "neck->head": -15,
        "shoulder->elbow": 169, "elbow->hand": 0,
    })),
])

# IT Band Cross — FRONT view, both legs visible. Standing tall → one leg
# crosses behind the other and body leans to the opposite side, stretching
# the outer hip / IT band of the back leg.
_fk("it-band-cross", 5.5, hip=(0.50, 0.62), poses=[
    ("neutral", {
        "hip->spineMid": -90, "spineMid->neck": -90, "neck->head": -90,
        "neck->shoulder": 30, "shoulder->elbow": -90, "elbow->hand": -90,
        "neck->shoulderL": 150, "shoulderL->elbowL": 90, "elbowL->handL": 90,
        "hip->knee": 80, "knee->foot": 90,
        "hip->kneeL": 100, "kneeL->footL": 90,
    }),
    ("leaned", {
        "hip->spineMid": -65, "spineMid->neck": -55, "neck->head": -45,
        "neck->shoulder": 40, "shoulder->elbow": -70, "elbow->hand": -65,
        "neck->shoulderL": 160, "shoulderL->elbowL": 100, "elbowL->handL": 100,
        # Right (front) leg stays standing; left leg crosses behind to the right.
        "hip->knee": 85, "knee->foot": 90,
        "hip->kneeL": 60, "kneeL->footL": 90,
    }),
])

# Seated Twist — FRONT view. Seated cross-legged, twist torso to the right.
# Right arm wraps behind for support; left arm crosses the body to hold
# the right knee (the lever that drives the rotation).
_fk("seated-twist", 6.0, hip=(0.50, 0.68), poses=[
    ("center", {
        "hip->spineMid": -90, "spineMid->neck": -90, "neck->head": -90,
        "neck->shoulder": 30, "shoulder->elbow": 75, "elbow->hand": 70,
        "neck->shoulderL": 150, "shoulderL->elbowL": 105, "elbowL->handL": 110,
        "hip->knee": 30, "knee->foot": 160,
        "hip->kneeL": 150, "kneeL->footL": 20,
    }),
    ("twist", {
        "hip->spineMid": -90, "spineMid->neck": -80, "neck->head": -50,
        # Right shoulder rotates back behind body; right hand plants on floor behind.
        "neck->shoulder": 90, "shoulder->elbow": 140, "elbow->hand": 150,
        # Left arm crosses body to the right, hand to the right knee.
        "neck->shoulderL": 90, "shoulderL->elbowL": 20, "elbowL->handL": 30,
        # Legs unchanged.
        "hip->knee": 30, "knee->foot": 160,
        "hip->kneeL": 150, "kneeL->footL": 20,
    }),
])


# ---------------------------------------------------------------------------
# Verifier — refuses to write if any bone drifts more than `tolerance`.
# ---------------------------------------------------------------------------

def _bone_len(joints: Dict, a: str, b: str) -> float:
    pa, pb = joints[a], joints[b]
    return math.hypot(pa[0] - pb[0], pa[1] - pb[1])


def verify_all(animations: Dict, tolerance: float = 0.03) -> float:
    failures: List[str] = []
    max_drift = 0.0
    max_drift_stretch = ""
    for sid, spec in animations.items():
        poses = spec["poses"]
        n = len(poses)
        for i in range(n):
            j = (i + 1) % n
            ja, jb = poses[i]["joints"], poses[j]["joints"]
            for parent, child in SKELETON_BONES:
                if parent not in ja or child not in ja or parent not in jb or child not in jb:
                    continue
                la = _bone_len(ja, parent, child)
                lb = _bone_len(jb, parent, child)
                if la == 0 or lb == 0:
                    continue
                drift = abs(la - lb) / max(la, lb)
                if drift > max_drift:
                    max_drift, max_drift_stretch = drift, sid
                if drift > tolerance:
                    failures.append(
                        f"  {sid} pose {i}<->{j} {parent}->{child}: "
                        f"{la:.3f} vs {lb:.3f} ({drift*100:.1f}%)"
                    )
    if failures:
        print("Bone-length verification FAILED:")
        for line in failures:
            print(line)
        raise SystemExit(1)
    print(
        f"OK: verified {len(animations)} stretches, "
        f"max drift {max_drift*100:.2f}% ({max_drift_stretch})"
    )
    return max_drift


# ---------------------------------------------------------------------------
# JSON patcher.
# ---------------------------------------------------------------------------

def patch(file_path: Path) -> int:
    data = json.loads(file_path.read_text(encoding="utf-8"))
    n = 0
    for stretch in data:
        spec = ANIMATIONS.get(stretch.get("id"))
        if spec is None:
            continue
        stretch["animation"] = spec
        n += 1
    file_path.write_text(
        json.dumps(data, indent=2, ensure_ascii=False) + "\n",
        encoding="utf-8",
    )
    return n


# ---------------------------------------------------------------------------
# Preview HTML generator.
# ---------------------------------------------------------------------------

# Stretches authored with bilateral (both-sides) skeleton joints. Tagged
# in the preview so it reads as "front-view" rather than the default
# side-view single-side rendering.
FRONT_VIEW_STRETCHES = {
    "butterfly", "standing-side-bend", "it-band-cross", "seated-twist",
}


def _svg_card_2d(sid: str, spec: Dict, size: int = 220) -> str:
    """Inline SVG with native <animate> tags — no JS needed for the 2D side."""
    poses = spec["poses"]
    loop = spec["loopSeconds"]
    cycle = list(poses) + [poses[0]]
    splines = ";".join(["0.42 0 0.58 1"] * (len(cycle) - 1))
    keyTimes = ";".join(f"{i/(len(cycle)-1):.3f}" for i in range(len(cycle)))
    lines: List[str] = []
    for parent, child in SKELETON_BONES:
        if any(parent not in p["joints"] or child not in p["joints"] for p in cycle):
            continue
        x1s = [f"{p['joints'][parent][0]*size:.1f}" for p in cycle]
        y1s = [f"{p['joints'][parent][1]*size:.1f}" for p in cycle]
        x2s = [f"{p['joints'][child][0]*size:.1f}" for p in cycle]
        y2s = [f"{p['joints'][child][1]*size:.1f}" for p in cycle]
        lines.append(
            f'<line x1="{x1s[0]}" y1="{y1s[0]}" x2="{x2s[0]}" y2="{y2s[0]}" '
            f'stroke="#2D5DAA" stroke-width="3" stroke-linecap="round">'
            f'<animate attributeName="x1" values="{";".join(x1s)}" dur="{loop}s" '
            f'repeatCount="indefinite" calcMode="spline" keySplines="{splines}" keyTimes="{keyTimes}"/>'
            f'<animate attributeName="y1" values="{";".join(y1s)}" dur="{loop}s" '
            f'repeatCount="indefinite" calcMode="spline" keySplines="{splines}" keyTimes="{keyTimes}"/>'
            f'<animate attributeName="x2" values="{";".join(x2s)}" dur="{loop}s" '
            f'repeatCount="indefinite" calcMode="spline" keySplines="{splines}" keyTimes="{keyTimes}"/>'
            f'<animate attributeName="y2" values="{";".join(y2s)}" dur="{loop}s" '
            f'repeatCount="indefinite" calcMode="spline" keySplines="{splines}" keyTimes="{keyTimes}"/>'
            f'</line>'
        )
    head_x = [f"{p['joints']['head'][0]*size:.1f}" for p in cycle]
    head_y = [f"{p['joints']['head'][1]*size:.1f}" for p in cycle]
    head_svg = (
        f'<circle cx="{head_x[0]}" cy="{head_y[0]}" r="{size*0.05:.1f}" '
        f'fill="none" stroke="#2D5DAA" stroke-width="3">'
        f'<animate attributeName="cx" values="{";".join(head_x)}" dur="{loop}s" '
        f'repeatCount="indefinite" calcMode="spline" keySplines="{splines}" keyTimes="{keyTimes}"/>'
        f'<animate attributeName="cy" values="{";".join(head_y)}" dur="{loop}s" '
        f'repeatCount="indefinite" calcMode="spline" keySplines="{splines}" keyTimes="{keyTimes}"/>'
        f'</circle>'
    )
    return f'<svg viewBox="0 0 {size} {size}">{"".join(lines)}{head_svg}</svg>'


def _card_html(sid: str, spec: Dict, size: int = 220) -> str:
    """One card containing the 2D SVG on the left and a 3D <canvas> on the
    right. The canvas is populated by the requestAnimationFrame loop that the
    HTML page boots once on load — see `_render_js` for the projection that
    mirrors `StretchAnimation3DView` on iOS and Android."""
    poses = spec["poses"]
    loop = spec["loopSeconds"]
    svg = _svg_card_2d(sid, spec, size=size)
    canvas = (
        f'<canvas class="three-d" data-stretch="{sid}" '
        f'width="{size*2}" height="{size*2}"></canvas>'
    )
    # Drift indicator.
    max_drift = 0.0
    for i in range(len(poses)):
        j = (i + 1) % len(poses)
        ja, jb = poses[i]["joints"], poses[j]["joints"]
        for parent, child in SKELETON_BONES:
            if parent not in ja or child not in ja or parent not in jb or child not in jb:
                continue
            la = _bone_len(ja, parent, child)
            lb = _bone_len(jb, parent, child)
            if la > 0 and lb > 0:
                d = abs(la - lb) / max(la, lb)
                if d > max_drift:
                    max_drift = d
    drift_class = "ok" if max_drift < 0.03 else ("warn" if max_drift < 0.08 else "bad")
    tag = ' · <span class="tag">front-view</span>' if sid in FRONT_VIEW_STRETCHES else ""
    return (
        f'<div class="card">'
        f'<div class="views"><div class="view"><div class="badge">2D</div>{svg}</div>'
        f'<div class="view"><div class="badge">3D</div>{canvas}</div></div>'
        f'<div class="label">{sid}</div>'
        f'<div class="meta">{loop}s · {len(poses)} poses · '
        f'<span class="drift {drift_class}">drift {max_drift*100:.1f}%</span>{tag}</div>'
        f'</div>'
    )


# JS that mirrors `drawMannequin` from StretchAnimation3DView (iOS + Android).
# It receives the same pose data we just verified, projects each joint through
# a Y-axis rotation + perspective divide, z-sorts the bones, and paints them
# as shaded capsules with a radial-gradient sphere for the head.
_RENDER_JS = r"""
const CAMERA_Y_ROTATION = 0.35;
const CAMERA_DISTANCE = 4;
const SKELETON_BONES = __BONES__;
const ANIMATIONS = __ANIMATIONS__;
const BODY_COLOR = [45, 93, 170];

function interpolatedPose(poses, t) {
    const n = poses.length;
    const wrapped = ((t % n) + n) % n;
    const i = Math.min(Math.max(Math.floor(wrapped), 0), n - 1);
    const j = (i + 1) % n;
    const raw = wrapped - i;
    const eased = Math.max(0, Math.min(1, 0.5 - 0.5 * Math.cos(raw * Math.PI)));
    const a = poses[i].joints;
    const b = poses[j].joints;
    const out = {};
    for (const key in a) {
        const pa = a[key];
        if (!pa || pa.length < 2) continue;
        const pb = b[key] || pa;
        if (pb.length < 2) continue;
        out[key] = [
            pa[0] + (pb[0] - pa[0]) * eased,
            pa[1] + (pb[1] - pa[1]) * eased,
        ];
    }
    return out;
}

function lerpColor(a, b, t) {
    const s = Math.max(0, Math.min(1, t));
    return [
        a[0] + (b[0] - a[0]) * s,
        a[1] + (b[1] - a[1]) * s,
        a[2] + (b[2] - a[2]) * s,
    ];
}

function rgbStr(c, alpha) {
    const r = Math.round(c[0]), g = Math.round(c[1]), bl = Math.round(c[2]);
    if (alpha === undefined) return `rgb(${r}, ${g}, ${bl})`;
    return `rgba(${r}, ${g}, ${bl}, ${alpha})`;
}

function drawMannequin(ctx, joints, w, h) {
    if (!joints || !Object.keys(joints).length) return;
    const cx = w / 2, cy = h / 2;
    const minDim = Math.min(w, h);

    const cosR = Math.cos(CAMERA_Y_ROTATION);
    const sinR = Math.sin(CAMERA_Y_ROTATION);

    const projected = {};
    for (const name in joints) {
        const nx = joints[name][0], ny = joints[name][1];
        const x = nx - 0.5;
        const y = -(ny - 0.5);
        const z = 0;
        const rotX = x * cosR + z * sinR;
        const rotZ = -x * sinR + z * cosR;
        const scale = CAMERA_DISTANCE / (CAMERA_DISTANCE + rotZ);
        const px = cx + rotX * scale * minDim;
        const py = cy + (-y) * scale * minDim;
        projected[name] = { point: [px, py], depth: rotZ, scale };
    }

    const baseStroke = Math.max(8, minDim * 0.045);
    const headRadius = minDim * 0.06;

    const bones = [];
    for (const pair of SKELETON_BONES) {
        const pa = projected[pair[0]], pb = projected[pair[1]];
        if (!pa || !pb) continue;
        bones.push({ pa, pb, avgDepth: (pa.depth + pb.depth) / 2 });
    }
    bones.sort((u, v) => v.avgDepth - u.avgDepth);

    for (const b of bones) {
        const avgScale = (b.pa.scale + b.pb.scale) / 2;
        const stroke = baseStroke * avgScale;
        const depthBlend = Math.max(0, Math.min(1, (b.pa.depth + b.pb.depth) / 2 + 0.5));
        const tint = lerpColor(BODY_COLOR, [27, 58, 110], depthBlend * 0.5);

        ctx.strokeStyle = rgbStr(tint);
        ctx.lineWidth = stroke;
        ctx.lineCap = 'round';
        ctx.beginPath();
        ctx.moveTo(b.pa.point[0], b.pa.point[1]);
        ctx.lineTo(b.pb.point[0], b.pb.point[1]);
        ctx.stroke();

        const highlight = lerpColor(tint, [255, 255, 255], 0.35);
        ctx.strokeStyle = rgbStr(highlight, 0.45);
        ctx.lineWidth = stroke * 0.35;
        ctx.beginPath();
        ctx.moveTo(b.pa.point[0], b.pa.point[1]);
        ctx.lineTo(b.pb.point[0], b.pb.point[1]);
        ctx.stroke();
    }

    const head = projected.head;
    if (head) {
        const r = headRadius * head.scale;
        const c = head.point;
        const gx = c[0] - r * 0.3;
        const gy = c[1] - r * 0.4;
        const gradient = ctx.createRadialGradient(gx, gy, 0, gx, gy, r * 1.6);
        gradient.addColorStop(0, rgbStr(lerpColor(BODY_COLOR, [255, 255, 255], 0.4)));
        gradient.addColorStop(0.5, rgbStr(BODY_COLOR));
        gradient.addColorStop(1, rgbStr(lerpColor(BODY_COLOR, [13, 37, 69], 0.6)));
        ctx.fillStyle = gradient;
        ctx.beginPath();
        ctx.arc(c[0], c[1], r, 0, Math.PI * 2);
        ctx.fill();
    }
}

const canvases = document.querySelectorAll('canvas.three-d');
const start = performance.now();

function frame(now) {
    const elapsed = (now - start) / 1000;
    for (const canvas of canvases) {
        const sid = canvas.dataset.stretch;
        const spec = ANIMATIONS[sid];
        if (!spec) continue;
        const loop = Math.max(spec.loopSeconds, 0.25);
        const phase = (elapsed % loop) / loop;
        const t = phase * spec.poses.length;
        const joints = interpolatedPose(spec.poses, t);
        const ctx = canvas.getContext('2d');
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        drawMannequin(ctx, joints, canvas.width, canvas.height);
    }
    requestAnimationFrame(frame);
}
requestAnimationFrame(frame);
"""


def write_preview_html(out_path: Path, animations: Dict) -> None:
    cards = "\n".join(_card_html(sid, spec) for sid, spec in animations.items())
    bones_json = json.dumps([list(p) for p in SKELETON_BONES])
    animations_json = json.dumps(animations)
    script = (
        _RENDER_JS
        .replace("__BONES__", bones_json)
        .replace("__ANIMATIONS__", animations_json)
    )
    html = f"""<!DOCTYPE html>
<html><head><title>Stretch animation preview</title><style>
body {{ font: 13px -apple-system, BlinkMacSystemFont, sans-serif;
        background: #f7f5f0; margin: 0; padding: 16px; color: #222; }}
h1 {{ margin: 0 0 16px; font-size: 18px; }}
.grid {{ display: grid; grid-template-columns: repeat(auto-fill, minmax(360px, 1fr)); gap: 14px; }}
.card {{ background: white; border-radius: 12px; padding: 12px;
         box-shadow: 0 1px 3px rgba(0,0,0,0.08); }}
.views {{ display: grid; grid-template-columns: 1fr 1fr; gap: 8px; }}
.view {{ position: relative; }}
.view svg, .view canvas {{ width: 100%; aspect-ratio: 1/1;
        background: #f0eee6; border-radius: 8px; display: block; }}
.badge {{ position: absolute; top: 6px; left: 6px; background: rgba(0,0,0,0.55);
          color: white; font-size: 10px; font-family: ui-monospace, monospace;
          padding: 1px 6px; border-radius: 3px; letter-spacing: 0.5px; }}
.label {{ font-weight: 600; margin-top: 8px; }}
.meta {{ font-size: 11px; color: #888; margin-top: 2px; font-family: ui-monospace, monospace; }}
.drift.ok {{ color: #1a7a3a; }}
.drift.warn {{ color: #b88c00; }}
.drift.bad {{ color: #b80000; }}
.tag {{ background: #eef; color: #339; padding: 1px 5px; border-radius: 3px; }}
</style></head><body>
<h1>Stretch animations ({len(animations)}) — 2D stick figure (left) vs 3D mannequin (right)</h1>
<div class="grid">{cards}</div>
<script>{script}</script>
</body></html>
"""
    out_path.write_text(html, encoding="utf-8")


# ---------------------------------------------------------------------------
# Entry point.
# ---------------------------------------------------------------------------

def main() -> None:
    repo = Path(__file__).resolve().parent.parent
    targets = [
        repo / "content" / "stretches.json",
        repo / "android" / "app" / "src" / "main" / "assets" / "stretches.json",
        repo / "ios" / "LowerBackStretching" / "Resources" / "stretches.json",
    ]
    for path in targets:
        if not path.exists():
            raise SystemExit(f"Missing target: {path}")

    verify_all(ANIMATIONS, tolerance=0.03)

    for path in targets:
        n = patch(path)
        print(f"{path.relative_to(repo)}: patched {n} stretches")

    preview = repo / "scripts" / "preview.html"
    write_preview_html(preview, ANIMATIONS)
    print(f"\nOpen for visual QA: {preview}")


if __name__ == "__main__":
    main()
