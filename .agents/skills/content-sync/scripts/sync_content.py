#!/usr/bin/env python3
"""
Content Synchronization & Validation Utility for Lower Back Stretching.
Validates schemas and synchronizes shared JSON assets across Android and iOS targets.
"""

import sys
import os
import json
import shutil
import argparse
from pathlib import Path

def validate_json_file(file_path):
    """Validate that a file is valid JSON and not empty."""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            data = json.load(f)
        if not data:
            print(f"[ERROR] {file_path.name} is empty.", file=sys.stderr)
            return False, None
        return True, data
    except Exception as e:
        print(f"[ERROR] Failed parsing {file_path}: {e}", file=sys.stderr)
        return False, None

def main():
    parser = argparse.ArgumentParser(description="Sync and validate shared content JSON across platforms.")
    parser.add_argument("--check", action="store_true", help="Only check for differences without modifying files.")
    parser.add_argument("--apply", action="store_true", help="Copy source content to all target directories.")
    args = parser.parse_args()

    # Find repo root (.agents/skills/content-sync/scripts -> repo root)
    script_dir = Path(__file__).resolve().parent
    repo_root = script_dir.parents[3]

    content_dir = repo_root / "content"
    targets = [
        ("Android Phone Assets", repo_root / "android" / "app" / "src" / "main" / "assets"),
        ("iOS Resources", repo_root / "ios" / "LowerBackStretching" / "Resources"),
    ]

    json_files = ["stretches.json", "programs.json", "glossary.json"]

    print("==================================================")
    print(" Lower Back Stretching — Content Sync & Validator")
    print("==================================================")
    print(f"Source Directory: {content_dir}\n")

    # 1. Validate Source Files
    all_valid = True
    for filename in json_files:
        src_path = content_dir / filename
        if not src_path.exists():
            print(f"[ERROR] Source file missing: {src_path}", file=sys.stderr)
            all_valid = False
            continue
        valid, data = validate_json_file(src_path)
        if not valid:
            all_valid = False
        else:
            if isinstance(data, list):
                print(f"[OK] {filename}: {len(data)} items loaded successfully.")
            elif isinstance(data, dict):
                print(f"[OK] {filename}: {len(data.keys())} keys loaded successfully.")

    if not all_valid:
        print("\n[FAIL] Validation failed on source content files.", file=sys.stderr)
        sys.exit(1)

    print("\n---------------- Platform Parity Check ----------------")

    # 2. Check or Sync Targets
    has_drift = False
    for target_label, target_dir in targets:
        print(f"\nTarget: {target_label} ({target_dir.relative_to(repo_root)})")
        if not target_dir.exists():
            print(f"  [WARN] Target directory does not exist: {target_dir}")
            if args.apply:
                target_dir.mkdir(parents=True, exist_ok=True)
                print(f"  [CREATED] Created {target_dir}")

        for filename in json_files:
            src_path = content_dir / filename
            tgt_path = target_dir / filename

            if not tgt_path.exists():
                print(f"  [MISSING] {filename} is missing in target.")
                has_drift = True
                if args.apply:
                    shutil.copy2(src_path, tgt_path)
                    print(f"  [COPIED] Copied {filename} -> {target_label}")
            else:
                src_bytes = src_path.read_bytes()
                tgt_bytes = tgt_path.read_bytes()
                if src_bytes != tgt_bytes:
                    print(f"  [DRIFT] {filename} differs from source ({len(src_bytes)} vs {len(tgt_bytes)} bytes).")
                    has_drift = True
                    if args.apply:
                        shutil.copy2(src_path, tgt_path)
                        print(f"  [UPDATED] Synced {filename} -> {target_label}")
                else:
                    print(f"  [IN-SYNC] {filename} is in sync.")

    print("\n==================================================")
    if has_drift and not args.apply:
        print("Status: Content drift detected between source and targets.")
        print("Run with '--apply' to sync all target files.")
        sys.exit(2)
    else:
        print("Status: All content files are verified and in sync!")
        sys.exit(0)

if __name__ == "__main__":
    main()
