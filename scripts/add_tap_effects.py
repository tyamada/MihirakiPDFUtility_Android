#!/usr/bin/env python3
"""
Post-processing script to overlay animated tap ripple effects on YouTube Shorts scene videos.
"""
import os
import cv2
import numpy as np
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS_DIR = ROOT / 'shorts_raw_assets'

# Define tap events for each scene: (timestamp_seconds, x, y)
TAP_EVENTS = {
    "scene_01": [
        (4.0, 1010, 120),  # 3 dots menu
        (5.2, 540, 300),   # Toggle view menu item
    ],
    "scene_02": [
        (3.0, 270, 400),   # Select page 0
        (3.5, 540, 400),   # Select page 1
        (6.0, 1010, 120),  # Open 3 dots menu to show editing accessibility
    ],
    "scene_03": [
        (3.0, 270, 400),   # Select page
        (6.0, 540, 120),   # Rotate
        (10.0, 700, 120),  # Insert Blank
        (15.0, 850, 120),  # Delete
    ],
    "scene_04": [
        (3.0, 1010, 120),  # 3 dots menu
        (4.0, 540, 300),   # Toggle view
        (10.0, 1010, 120), # 3 dots menu
        (11.0, 540, 450),  # Options
    ],
    "scene_05": [
        (3.0, 400, 120),   # Export / Save
        (6.0, 540, 1300),  # Confirm dialog
    ]
}

def add_taps_to_video(video_path, taps):
    print(f"Processing {video_path.name}...")
    cap = cv2.VideoCapture(str(video_path))
    if not cap.isOpened():
        print(f"Error opening video {video_path}")
        return

    width = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
    height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
    fps = cap.get(cv2.CAP_PROP_FPS)
    if fps <= 0 or fps > 120:
        fps = 30.0 # fallback

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
                progress = elapsed / 0.6 # 0.0 to 1.0
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
    print(f"Re-encoding {final_output.name} with ffmpeg...")
    cmd = [
        "ffmpeg", "-y", "-i", str(temp_output),
        "-c:v", "libx264", "-b:v", "16M", "-pix_fmt", "yuv420p",
        "-c:a", "aac", str(final_output)
    ]
    subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    if temp_output.exists():
        temp_output.unlink()
    print(f"Finished processing {final_output.name}")

def main():
    for scene_name, taps in TAP_EVENTS.items():
        video_path = ASSETS_DIR / f"{scene_name}.mp4"
        if video_path.exists():
            add_taps_to_video(video_path, taps)
        else:
            print(f"Video not found: {video_path}")

if __name__ == '__main__':
    main()
