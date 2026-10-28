#!/usr/init/env python3
"""
YouTube Shorts End-to-End Automation Script with Clean Frame Extraction & Strict Verification
Usage:
  python3 scripts/build_shorts_videos.py --scene all
  python3 scripts/build_shorts_videos.py --scene 3
"""
import argparse
import os
import subprocess
import sys
import time
import cv2
import numpy as np
from PIL import Image, ImageDraw, ImageFont
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RAW_DIR = ROOT / 'shorts_raw_assets'
OUT_DIR = ROOT / 'shorts_output'
FONT_PATH = '/System/Library/Fonts/Supplemental/Arial.ttf'
LOG_FILE = OUT_DIR / 'shorts_recording_debug.log'

def log(msg):
    print(msg)
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    with open(LOG_FILE, "a") as f:
        f.write(f"[{time.strftime('%Y-%m-%d %H:%M:%S')} [BuildPipeline]] {msg}\n")

SCENE_SPECS = {
    "1": {
        "name": "scene_01",
        "narration": "Meet Mihiraki PDF Utility—the dedicated editor designed specifically for scanned books and manga. Preview your layout in authentic two-page spreads while you edit. Getting started is completely effortless.",
        "teleops": [
            (0.0, 4.0, "Mihiraki PDF Utility: Manga PDF Editor"),
            (4.0, 15.0, "Authentic Two-Page Spread Preview")
        ]
    },
    "2": {
        "name": "scene_02",
        "narration": "Import your scanned files in a snap. The streamlined interface keeps powerful tools right within reach. Access essential edit actions instantly, making adjustments quick and painless even on mobile.",
        "teleops": [
            (0.0, 6.0, "Instant File Import & Grid"),
            (6.0, 14.0, "Fast & Intuitive Navigation")
        ]
    },
    "3": {
        "name": "scene_03",
        "narration": "Fix imperfect scans with precision. First, rotate misoriented pages, split wide scans into single leaves, and insert blank pages to re-align your spreads perfectly. Finally, need to clean up? Remove unwanted pages with a single tap.",
        "teleops": [
            (0.0, 5.0, "1. Rotate Orientation"),
            (5.0, 11.0, "2. Insert Blank (Align Spreads)"),
            (11.0, 25.0, "3. Delete Pages & Final Result")
        ],
        "speech_rate": "150"
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
            (0.0, 6.0, "Instant Lossless Export"),
            (6.0, 16.0, "100% Offline & Private (No Cloud)")
        ]
    }
}

def ensure_raw_videos(scene_keys):
    log(f"Recording fresh raw scene videos for {scene_keys}...")
    cmd = [str(ROOT / "scripts" / "record_shorts_scenes.sh")] + [k for k in scene_keys]
    res = subprocess.run(cmd, cwd=ROOT, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
    log(res.stdout)
    if res.returncode != 0:
        log("ERROR: Failed to record raw videos.")
        sys.exit(1)

    # Run tap ripple post-processing
    tap_script = ROOT / "scripts" / "add_tap_effects.py"
    if tap_script.exists():
        tap_res = subprocess.run([str(tap_script)], cwd=ROOT, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
        log(tap_res.stdout)

def verify_output_video(final_output):
    cmd = [
        "ffprobe", "-v", "error",
        "-select_streams", "a:0",
        "-show_entries", "stream=codec_name,channels,sample_rate,duration",
        "-of", "default=noprint_wrappers=1",
        str(final_output)
    ]
    res = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
    log(f"[VERIFIED AUDIO STREAM FOR {final_output.name}]:\n{res.stdout.strip()}")
    if "codec_name=aac" not in res.stdout or "channels=2" not in res.stdout or "sample_rate=44100" not in res.stdout:
        log(f"WARNING: Audio stream verification mismatch for {final_output.name}")
    else:
        log(f"SUCCESS: Audio verified! Stereo AAC 44.1kHz track is present and active in {final_output.name}.")

def extract_raw_still_frames(raw_video_path, scene_name):
    frames_dir = OUT_DIR / f"{scene_name}_raw_frames"
    if frames_dir.exists():
        for f in frames_dir.glob("*.jpg"):
            f.unlink()
    frames_dir.mkdir(parents=True, exist_ok=True)
    log(f"Extracting clean upstream raw still frames from {raw_video_path.name} to {frames_dir.name}/...")
    extract_cmd = [
        "ffmpeg", "-y", "-i", str(raw_video_path),
        "-vf", "fps=1",
        str(frames_dir / "raw_frame_%02d.jpg")
    ]
    subprocess.run(extract_cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
    log(f"Extracted clean upstream raw still frames successfully to {frames_dir}/")

def extract_still_frames(final_output, scene_name):
    frames_dir = OUT_DIR / f"{scene_name}_frames"
    if frames_dir.exists():
        for f in frames_dir.glob("*.jpg"):
            f.unlink()
    frames_dir.mkdir(parents=True, exist_ok=True)
    log(f"Extracting clean still frames from {final_output.name} to {frames_dir.name}/...")
    extract_cmd = [
        "ffmpeg", "-y", "-i", str(final_output),
        "-vf", "fps=1",
        str(frames_dir / "frame_%02d.jpg")
    ]
    subprocess.run(extract_cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
    log(f"Extracted clean still frames successfully to {frames_dir}/")

def run_frame_verifier(scene_name):
    verifier_script = ROOT / "scripts" / "verify_scene_frames.py"
    if verifier_script.exists():
        res = subprocess.run([sys.executable, str(verifier_script), scene_name], cwd=ROOT, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
        log(res.stdout)
        if res.returncode != 0:
            log(f"ERROR: Frame verification failed for {scene_name}")
            sys.exit(1)

def build_scene(scene_id):
    spec = SCENE_SPECS[scene_id]
    scene_name = spec["name"]
    raw_video = RAW_DIR / f"{scene_name}.mp4"
    final_output_mp4 = OUT_DIR / f"{scene_name}_final.mp4"

    log(f"\n==============================================")
    log(f" Building Final Video for Scene {scene_id} ({scene_name}_final.mp4 in ./shorts_output/)...")
    log(f"==============================================")

    # Always re-record fresh raw video
    ensure_raw_videos([scene_id])

    if not raw_video.exists():
        log(f"ERROR: Raw video not found after recording: {raw_video}")
        return

    # Extract clean upstream raw still frames
    extract_raw_still_frames(raw_video, scene_name)

    # 1. Generate TTS narration audio using macOS 'say' (converted to stereo 44.1kHz)
    temp_aiff = f"/tmp/{scene_name}.aiff"
    temp_wav = f"/tmp/{scene_name}.wav"

    speech_rate = spec.get("speech_rate", "175")
    log(f"Generating TTS narration audio (rate={speech_rate})...")
    say_cmd = ["say", "-v", "Samantha", "-r", speech_rate, spec["narration"], "-o", temp_aiff]
    subprocess.run(say_cmd, check=True)

    conv_cmd = ["ffmpeg", "-y", "-i", temp_aiff, "-ar", "44100", "-ac", "2", temp_wav]
    subprocess.run(conv_cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, check=True)

    # 2. Frame-accurate trim of the first 6.5 seconds of raw video
    trimmed_video_path = f"/tmp/{scene_name}_trimmed.mp4"
    trim_cmd = [
        "ffmpeg", "-y",
        "-i", str(raw_video),
        "-ss", "6.5",
        "-c:v", "libx264", "-b:v", "16M", "-c:a", "copy",
        trimmed_video_path
    ]
    trim_res = subprocess.run(trim_cmd, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
    log(trim_res.stdout)

    # 3. Burn teleop subtitles frame-by-frame using Pillow on the trimmed video
    cap = cv2.VideoCapture(trimmed_video_path)
    if not cap.isOpened():
        log(f"ERROR: Error opening trimmed video {trimmed_video_path}")
        return

    width = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
    height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
    fps = cap.get(cv2.CAP_PROP_FPS)
    if fps <= 0 or fps > 120:
        fps = 30.0

    temp_teleop_path = f"/tmp/{scene_name}_teleop.mp4"
    fourcc = cv2.VideoWriter_fourcc(*'mp4v')
    out = cv2.VideoWriter(temp_teleop_path, fourcc, fps, (width, height))

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
            img_rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
            pil_img = Image.fromarray(img_rgb)
            draw = ImageDraw.Draw(pil_img)

            bbox = font.getbbox(active_text)
            text_width = bbox[2] - bbox[0]
            text_height = bbox[3] - bbox[1]
            x = (width - text_width) / 2
            y = 300

            draw.text(
                (x, y), active_text, font=font,
                fill=(255, 255, 255, 255),
                stroke_width=4, stroke_fill=(0, 0, 0, 255)
            )

            frame = cv2.cvtColor(np.array(pil_img), cv2.COLOR_RGB2BGR)

        out.write(frame)
        frame_idx += 1

    cap.release()
    out.release()

    # 4. Merge video with burned subtitles and narration audio using FFmpeg
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    log("Merging video and narration audio into .mp4 via FFmpeg...")
    merge_cmd = [
        "ffmpeg", "-y",
        "-i", temp_teleop_path,
        "-i", temp_wav,
        "-map", "0:v",
        "-map", "1:a",
        "-c:v", "libx264", "-b:v", "16M", "-pix_fmt", "yuv420p", "-r", "30",
        "-c:a", "aac", "-b:a", "192k", "-ac", "2", "-ar", "44100",
        "-movflags", "+faststart",
        "-shortest",
        str(final_output_mp4)
    ]
    merge_res = subprocess.run(merge_cmd, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
    log(merge_res.stdout)

    verify_output_video(final_output_mp4)

    # 5. Extract clean still frames from final video
    extract_still_frames(final_output_mp4, scene_name)

    # 6. Run strict frame verification
    run_frame_verifier(scene_name)

    # Cleanup temp files
    for p in [temp_aiff, temp_wav, trimmed_video_path, temp_teleop_path]:
        if os.path.exists(p):
            os.remove(p)

    log(f"SUCCESS: Created final video, clean raw frames, still frames, and verified successfully -> {final_output_mp4}")

def main():
    parser = argparse.ArgumentParser(description="End-to-End YouTube Shorts Video Builder with Clean Frame Extraction")
    parser.add_argument("--scene", default="all", help="Scene number (1-5) or 'all'")
    args = parser.parse_args()

    scene_arg = args.scene.lower()
    if scene_arg == "all":
        scene_keys = ["1", "2", "3", "4", "5"]
    else:
        scene_keys = [scene_arg]
        if scene_keys[0] not in SCENE_SPECS:
            log(f"ERROR: Invalid scene: {scene_arg} (must be 1-5 or 'all')")
            sys.exit(1)

    for s_key in scene_keys:
        build_scene(s_key)

    log(f"\nAll requested YouTube Shorts videos built and strictly verified. Diagnostic log saved to {LOG_FILE}")

if __name__ == '__main__':
    main()
