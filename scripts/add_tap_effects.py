#!/usr/bin/env python3
"""
Post-processing script to overlay animated tap ripple effects on YouTube Shorts scene videos,
with comprehensive logging and diagnostics.
"""
import os
import cv2
import numpy as np
import subprocess
import time
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS_DIR = ROOT / 'shorts_raw_assets'
OUT_DIR = ROOT / 'shorts_output'
LOG_FILE = OUT_DIR / 'shorts_recording_debug.log'

def log(msg):
    print(msg)
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    with open(LOG_FILE, "a") as f:
        f.write(f"[{time.strftime('%Y-%m-%d %H:%M:%S')} [TapEffects]] {msg}\n")

# Precisely synchronized tap events for each scene based on test automation timeline: (timestamp_seconds, x, y)
TAP_EVENTS = {
    "scene_01": [
        (3.5, 1010, 120),  # 3 dots overflow menu
        (4.8, 540, 320),   # Toggle view menu item
    ],
    "scene_02": [
        (2.5, 540, 500),   # Empty state / Dismiss dialogs / Import
        (6.5, 1010, 120),  # 3 dots settings / menu controls
    ],
    "scene_03": [
        (3.0, 270, 400),   # Select page 0
        (6.0, 650, 120),   # Rotate button
        (10.0, 750, 120),  # Insert Blank button
        (14.5, 900, 120),  # Delete button
        (16.5, 700, 1300), # Confirm delete dialog button
    ],
    "scene_04": [
        (2.5, 1010, 120),  # 3 dots menu
        (3.5, 540, 320),   # Toggle view to spread
        (5.5, 1010, 120),  # 3 dots menu
        (6.5, 540, 320),   # Toggle view back to grid
        (8.5, 1010, 120),  # 3 dots menu
        (9.5, 540, 420),   # Options dialog
    ],
    "scene_05": [
        (2.5, 800, 120),   # Save / Export action button
    ]
}

def add_taps_to_video(video_path, taps):
    log(f"Processing tap effects for {video_path.name}...")
    cap = cv2.VideoCapture(str(video_path))
    if not cap.isOpened():
        log(f"ERROR: Error opening video {video_path}")
        return

    width = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
    height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
    fps = cap.get(cv2.CAP_PROP_FPS)
    if fps <= 0 or fps > 120:
        fps = 30.0

    temp_output = video_path.with_name(f"temp_{video_path.name}")
    fourcc = cv2.VideoWriter_fourcc(*'mp4v')
    out = cv2.VideoWriter(str(temp_output), fourcc, fps, (width, height))

    frame_idx = 0
    while cap.isOpened():
        ret, frame = cap.read()
        if not ret:
            break

        current_time = frame_idx / fps

        # Draw active tap ripple effects
        for (tap_time, x, y) in taps:
            elapsed = current_time - tap_time
            if 0 <= elapsed <= 0.6: # ripple duration 600ms
                progress = elapsed / 0.6
                radius = int(20 + 60 * progress)
                alpha = max(0.0, 1.0 - progress)

                overlay = frame.copy()
                cv2.circle(overlay, (x, y), radius, (255, 255, 255), 3, cv2.LINE_AA)
                cv2.circle(overlay, (x, y), int(radius * 0.6), (200, 240, 255), 2, cv2.LINE_AA)
                cv2.circle(overlay, (x, y), 8, (255, 255, 255), -1, cv2.LINE_AA)

                cv2.addWeighted(overlay, alpha, frame, 1 - alpha, 0, frame)

        out.write(frame)
        frame_idx += 1

    cap.release()
    out.release()

    final_output = video_path
    log(f"Re-encoding {final_output.name} with ffmpeg...")
    cmd = [
        "ffmpeg", "-y", "-i", str(temp_output),
        "-c:v", "libx264", "-b:v", "16M", "-pix_fmt", "yuv420p",
        "-c:a", "aac", str(final_output)
    ]
    res = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
    log(res.stdout)
    if temp_output.exists():
        temp_output.unlink()
    log(f"Finished tap effects for {final_output.name}")

def main():
    for scene_name, taps in TAP_EVENTS.items():
        video_path = ASSETS_DIR / f"{scene_name}.mp4"
        if video_path.exists():
            add_taps_to_video(video_path, taps)
        else:
            log(f"WARNING: Video not found: {video_path}")

if __name__ == '__main__':
    main()
