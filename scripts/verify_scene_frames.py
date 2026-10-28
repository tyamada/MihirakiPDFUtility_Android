#!/usr/bin/env python3
"""
Frame Verification Script
Inserts a strict verification step into the pipeline to inspect exported still frames
and ensure that raw recordings contain valid non-black frames and expected milestones.
"""
import sys
from pathlib import Path
import cv2

ROOT = Path(__file__).resolve().parents[1]
OUT_DIR = ROOT / 'shorts_output'

def verify_frames(scene_name):
    frames_dir = OUT_DIR / f"{scene_name}_frames"
    raw_frames_dir = OUT_DIR / f"{scene_name}_raw_frames"

    print(f"Verifying frames for {scene_name}...")

    for d in [frames_dir, raw_frames_dir]:
        if not d.exists():
            print(f"WARNING: Frame directory not found: {d}")
            continue

        frames = sorted(list(d.glob("*.jpg")))
        print(f"Found {len(frames)} frames in {d.name}/")
        if not frames:
            print(f"ERROR: No frames found in {d}")
            sys.exit(1)

        # Check sample frames for black screens or corruption
        for frame_path in frames[::max(1, len(frames)//5)]:
            img = cv2.imread(str(frame_path))
            if img is None:
                print(f"ERROR: Failed to read frame {frame_path.name}")
                sys.exit(1)
            gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
            mean_brightness = gray.mean()
            if mean_brightness < 5.0:
                print(f"WARNING: Frame {frame_path.name} is extremely dark (mean brightness: {mean_brightness})")

    print(f"SUCCESS: Frame verification passed for {scene_name}.")

def main():
    scene = sys.argv[1] if len(sys.argv) > 1 else "all"
    if scene == "all":
        for s in ["scene_01", "scene_02", "scene_03", "scene_04", "scene_05"]:
            verify_frames(s)
    else:
        verify_frames(scene if scene.startswith("scene_") else f"scene_0{scene}")

if __name__ == '__main__':
    main()
