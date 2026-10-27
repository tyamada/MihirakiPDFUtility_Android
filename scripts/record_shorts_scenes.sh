#!/usr/bin/env python3
"""
YouTube Shorts Tutorial Video Scene Recording Script (English Edition - Optimized & Pre-installed)
Usage:
  ./scripts/record_shorts_scenes.sh all
  ./scripts/record_shorts_scenes.sh 1
  ./scripts/record_shorts_scenes.sh 1 3 5
"""
import argparse
import os
import subprocess
import sys
import time

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..'))
ASSETS_DIR = os.path.join(ROOT, 'shorts_raw_assets')
TESTDATA_DIR = os.path.join(ROOT, 'testdata')

def run_adb(cmd, timeout=120):
    res = subprocess.run(["adb"] + cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True, timeout=timeout)
    return res.returncode, res.stdout.strip(), res.stderr.strip()

def check_device():
    code, out, _ = run_adb(["devices"])
    lines = out.splitlines()
    devices = [line.split()[0] for line in lines[1:] if "device" in line]
    if not devices:
        print("ERROR: No active Android emulator/device found. Please start an emulator.")
        sys.exit(1)
    return devices[0]

def pre_install_app():
    print("Pre-building and installing app and test APKs...")
    env = os.environ.copy()
    env.pop("ANDROID_PREFS_ROOT", None)
    build_cmd = ["./gradlew", "installDebug", "installDebugAndroidTest", "--no-configuration-cache"]
    res = subprocess.run(build_cmd, cwd=ROOT, env=env)
    if res.returncode != 0:
        print("ERROR: Failed to install app and test APKs.")
        sys.exit(1)
    print("SUCCESS: App and test APKs pre-installed successfully.")

def prepare_english_environment():
    print("Setting device locale to en-US...")
    run_adb(["shell", "setprop", "persist.sys.locale", "en-US"])
    run_adb(["shell", "settings", "put", "system", "system_locales", "en-US"])
    run_adb(["shell", "am", "broadcast", "-a", "android.intent.action.LOCALE_CHANGED"])
    run_adb(["shell", "wm", "dismiss-keyguard"])
    time.sleep(3)

    # Push English sample PDFs to /data/local/tmp/ (world-readable)
    remote_dir = "/data/local/tmp"
    pdf_files = ["tameshibu_episode1_2_en.pdf", "tameshibu_episode2_2_en.pdf"]
    for pdf in pdf_files:
        local_path = os.path.join(TESTDATA_DIR, pdf)
        if os.path.exists(local_path):
            print(f"Pushing {pdf} to {remote_dir}/...")
            run_adb(["push", local_path, f"{remote_dir}/{pdf}"])
            run_adb(["shell", f"chmod 644 {remote_dir}/{pdf}"])
        else:
            print(f"WARNING: Local PDF {local_path} not found.")

def record_scene(scene_num):
    scene_name = f"scene_0{scene_num}"
    remote_path = f"/sdcard/{scene_name}.mp4"
    local_dir = ASSETS_DIR
    os.makedirs(local_dir, exist_ok=True)
    local_path = os.path.join(local_dir, f"{scene_name}.mp4")

    print(f"\n==============================================")
    print(f" Recording English Scene {scene_num} ({scene_name}.mp4)...")
    print(f"==============================================")

    # Ensure clean remote file
    run_adb(["shell", f"rm -f {remote_path}"])

    # Start screenrecord in background with 16Mbps bitrate
    print("Starting adb screenrecord (bit-rate 16Mbps)...")
    rec_proc = subprocess.Popen(["adb", "shell", "screenrecord", "--bit-rate", "16000000", remote_path])

    # Wait 2 seconds for recording to stabilize
    time.sleep(2)

    # Run instrumentation test directly via adb am instrument for lightning-fast execution
    print(f"Running am instrument test for {scene_name}...")
    instrument_cmd = [
        "shell", "am", "instrument", "-w", "-r",
        "-e", "class", "com.takumayamada22.pdfutility.ShortsRecordingTest#recordScene",
        "-e", "scene", scene_name,
        "com.takumayamada22.pdfutility.test/androidx.test.runner.AndroidJUnitRunner"
    ]
    code, out, err = run_adb(instrument_cmd, timeout=120)
    print(out)

    if code != 0:
        print(f"ERROR: Instrumentation test failed for {scene_name}. Err: {err}")
        run_adb(["shell", "pkill", "-INT", "screenrecord"])
        rec_proc.terminate()
        sys.exit(1)

    # Wait an extra moment to capture final state
    time.sleep(3)

    # Stop screenrecord gracefully via SIGINT
    print("Stopping screenrecord...")
    run_adb(["shell", "pkill", "-INT", "screenrecord"])
    time.sleep(3)

    # Terminate record process if still running
    if rec_proc.poll() is None:
        rec_proc.terminate()
        try:
            rec_proc.wait(timeout=5)
        except subprocess.TimeoutExpired:
            rec_proc.kill()

    # Pull recorded MP4 file
    print(f"Pulling {remote_path} to {local_path}...")
    pull_code, _, pull_err = run_adb(["pull", remote_path, local_path])
    if pull_code != 0 or not os.path.exists(local_path) or os.path.getsize(local_path) < 1024:
        print(f"ERROR: Failed to pull or invalid video file for {scene_name}. Err: {pull_err}")
        sys.exit(1)

    print(f"SUCCESS: Saved English video {local_path} (Size: {os.path.getsize(local_path)} bytes)")

def main():
    parser = argparse.ArgumentParser(description="Record English YouTube Shorts scenes for MihirakiPDFUtility (Optimized)")
    parser.add_argument("scenes", nargs="+", help="Scene numbers (1-5) or 'all'")
    args = parser.parse_args()

    device = check_device()
    print(f"Using device: {device}")

    # 1. Pre-install app and test APKs once to minimize startup time
    pre_install_app()

    # 2. Enable touch visualization and prepare environment & test PDFs
    print("Enabling touch visualization (show_touches)...")
    run_adb(["shell", "settings", "put", "system", "show_touches", "1"])
    prepare_english_environment()

    requested_scenes = []
    if "all" in args.scenes:
        requested_scenes = [1, 2, 3, 4, 5]
    else:
        for s in args.scenes:
            try:
                num = int(s)
                if 1 <= num <= 5:
                    requested_scenes.append(num)
                else:
                    print(f"Invalid scene number: {s} (must be 1-5)")
            except ValueError:
                print(f"Invalid argument: {s}")

    for num in requested_scenes:
        record_scene(num)

    print("\nAll requested English YouTube Shorts scenes recorded successfully with optimized startup!")

if __name__ == "__main__":
    main()
