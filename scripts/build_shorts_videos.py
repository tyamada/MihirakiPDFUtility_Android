#!/usr/bin/env python3
"""
YouTube Shorts End-to-End Automation Script
Handles:
1. Emulator screen recording (Scenes 1-5) with touch visualization.
2. TTS Narration generation using macOS 'say' command.
3. Teleop (On-Screen Text) burning using Python Pillow (PIL) for robust rendering.
4. Audio-video merging & final export to ./shorts_output/scene_0X_final.mp4.

Usage:
  python3 scripts/build_shorts_videos.py --scene all
  python3 scripts/build_shorts_videos.py --scene 3
"""
import argparse
import os
import subprocess
import sys
import cv2
import numpy as np
from PIL import Image, ImageDraw, ImageFont
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RAW_DIR = ROOT / 'shorts_raw_assets'
OUT_DIR = ROOT / 'shorts_output'
FONT_PATH = '/System/Library/Fonts/Supplemental/Arial.ttf'

SCENE_SPECS = {
    "1": {
        "name": "scene_01",
        "narration": "Meet Mihiraki PDF Utility—the dedicated editor designed specifically for scanned books and manga. Preview your layout in authentic two-page spreads while you edit. Getting started is completely effortless.",
        "teleops": [
            (0.0, 6.0, "Mihiraki PDF Utility: Manga PDF Editor"),
            (6.0, 15.0, "Authentic Two-Page Spread Preview")
        ]
    },
    "2": {
        "name": "scene_02",
        "narration": "Import your scanned files in a snap. The streamlined interface keeps powerful tools right within reach. Access essential edit actions instantly, making adjustments quick and painless even on mobile.",
        "teleops": [
            (0.0, 6.0, "Instant File Import"),
            (6.0, 15.0, "Fast & Intuitive Menu Controls")
        ]
    },
    "3": {
        "name": "scene_03",
        "narration": "Fix imperfect scans with precision. Rotate misoriented pages, split wide scans into single leaves, and insert blank pages to re-align your spreads perfectly. Need to clean up? Remove unwanted pages with a single tap.",
        "teleops": [
            (0.0, 4.5, "1. Rotate Orientation"),
            (4.5, 9.0, "2. Split Wide Spreads"),
            (9.0, 14.5, "3. Insert Blank (Fix Spread Alignment!)"),
            (14.5, 20.0, "4. Delete Pages")
        ]
    },
    "4": {
        "name": "scene_04",
        "narration": "Switch between a comprehensive grid view to manage whole chapters and spread preview to inspect every detail. You can even customize your toolbar to keep your most-used editing features exactly where you want them.",
        "teleops": [
            (0.0, 7.0, "Grid View vs Spread Preview"),
            (7.0, 15.0, "Fully Customizable Toolbar")
        ]
    },
    "5": {
        "name": "scene_05",
        "narration": "Export your newly organized PDF instantly with zero quality loss. Best of all, all processing happens entirely on your device—no cloud uploads, no account required, complete privacy. Download Mihiraki PDF Utility on Google Play today!",
        "teleops": [
            (0.0, 7.0, "Instant Lossless Export"),
            (7.0, 12.0, "100% Offline & Private (No Cloud)"),
            (12.0, 17.0, "Get It on Google Play!")
        ]
    }
}

def ensure_raw_videos(scene_keys):
    print("Ensuring raw scene videos are recorded and processed...")
    cmd = [str(ROOT / "scripts" / "record_shorts_scenes.sh")] + [k for k in scene_keys]
    res = subprocess.run(cmd, cwd=ROOT)
    if res.returncode != 0:
        print("ERROR: Failed to record raw videos.")
        sys.exit(1)

    # Run tap ripple post-processing
    tap_script = ROOT / "scripts" / "add_tap_effects.py"
    if tap_script.exists():
        subprocess.run([str(tap_script)], cwd=ROOT)

def build_scene(scene_id):
    spec = SCENE_SPECS[scene_id]
    scene_name = spec["name"]
    raw_video = RAW_DIR / f"{scene_name}.mp4"
    final_output = OUT_DIR / f"{scene_name}_final.mp4"

    if not raw_video.exists():
        print(f"Raw video missing for {scene_name}, recording...")
        ensure_raw_videos([scene_id])

    print(f"\n==============================================")
    print(f" Building Final Video for Scene {scene_id} ({scene_name}_final.mp4)...")
    print(f"==============================================")

    # 1. Generate TTS narration audio using macOS 'say'
    temp_aiff = f"/tmp/{scene_name}.aiff"
    temp_wav = f"/tmp/{scene_name}.wav"

    print("Generating TTS narration audio...")
    say_cmd = ["say", "-v", "Samantha", "-r", "175", spec["narration"], "-o", temp_aiff]
    subprocess.run(say_cmd, check=True)

    conv_cmd = ["ffmpeg", "-y", "-i", temp_aiff, "-ar", "44100", temp_wav]
    subprocess.run(conv_cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, check=True)

    # 2. Burn teleop subtitles frame-by-frame using Pillow
    cap = cv2.VideoCapture(str(raw_video))
    if not cap.isOpened():
        print(f"Error opening video {raw_video}")
        return

    width = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
    height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
    fps = cap.get(cv2.CAP_PROP_FPS)
    if fps <= 0 or fps > 120:
        fps = 30.0

    temp_video_path = f"/tmp/{scene_name}_teleop.mp4"
    fourcc = cv2.VideoWriter_fourcc(*'mp4v')
    out = cv2.VideoWriter(temp_video_path, fourcc, fps, (width, height))

    try:
        font = ImageFont.truetype(FONT_PATH, size=46)
    except Exception:
        font = ImageFont.load_default()

    frame_idx = 0
    while cap.isOpened():
        ret, frame = cap.read()
        if not ret:
            break

        current_time = frame_idx / fps
        active_text = None
        for (start, end, text) in spec["teleops"]:
            if start <= current_time <= end:
                active_text = text
                break

        if active_text:
            # Convert OpenCV frame (BGR) to PIL Image (RGB)
            img_rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
            pil_img = Image.fromarray(img_rgb)
            draw = ImageDraw.Draw(pil_img)

            # Get text bounding box for centering
            bbox = font.getbbox(active_text)
            text_width = bbox[2] - bbox[0]
            text_height = bbox[3] - bbox[1]
            x = (width - text_width) / 2
            y = 300 # Top-center position

            # Draw text with thick black outline/stroke and white fill
            draw.text(
                (x, y), active_text, font=font,
                fill=(255, 255, 255, 255),
                stroke_width=4, stroke_fill=(0, 0, 0, 255)
            )

            # Convert back to OpenCV frame (BGR)
            frame = cv2.cvtColor(np.array(pil_img), cv2.COLOR_RGB2BGR)

        out.write(frame)
        frame_idx += 1

    cap.release()
    out.release()

    # 3. Merge video with burned subtitles and narration audio using FFmpeg
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    print("Merging video and narration audio via FFmpeg...")
    merge_cmd = [
        "ffmpeg", "-y",
        "-i", temp_video_path,
        "-i", temp_wav,
        "-c:v", "libx264", "-b:v", "16M", "-pix_fmt", "yuv420p",
        "-c:a", "aac", "-b:a", "192k",
        "-shortest",
        str(final_output)
    ]
    subprocess.run(merge_cmd, check=True)

    # Cleanup temp files
    for p in [temp_aiff, temp_wav, temp_video_path]:
        if os.path.exists(p):
            os.remove(p)

    print(f"SUCCESS: Created final video -> {final_output}")

def main():
    parser = argparse.ArgumentParser(description="End-to-End YouTube Shorts Video Builder (Recording + TTS + Teleop + Merge)")
    parser.add_argument("--scene", default="all", help="Scene number (1-5) or 'all'")
    args = parser.parse_args()

    scene_arg = args.scene.lower()
    if scene_arg == "all":
        scene_keys = ["1", "2", "3", "4", "5"]
    else:
        scene_keys = [scene_arg]
        if scene_keys[0] not in SCENE_SPECS:
            print(f"Invalid scene: {scene_arg} (must be 1-5 or 'all')")
            sys.exit(1)

    # Ensure raw videos for requested scenes are recorded first
    ensure_raw_videos(scene_keys)

    # Build final videos with narration and teleop
    for s_key in scene_keys:
        build_scene(s_key)

    print("\nAll requested YouTube Shorts videos built successfully with narration and teleop!")

if __name__ == '__main__':
    main()
