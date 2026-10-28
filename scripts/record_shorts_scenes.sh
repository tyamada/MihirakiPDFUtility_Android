#!/usr/bin/env python3
"""
YouTube Shorts Tutorial Video Scene Recording Script (Using ShortsRecordingTest Instrumentation)
Usage:
  ./scripts/record_shorts_scenes.sh all
  ./scripts/record_shorts_scenes.sh 3
"""
import argparse
import os
import subprocess
import sys
import time
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS_DIR = ROOT / 'shorts_raw_assets'
OUT_DIR = ROOT / 'shorts_output'
TESTDATA_DIR = ROOT / 'testdata'
LOG_FILE = OUT_DIR / 'shorts_recording_debug.log'

def log(msg):
    print(msg)
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    with open(LOG_FILE, "a") as f:
        f.write(f"[{time.strftime('%Y-%m-%d %H:%M:%S')} [RecordingScript]] {msg}\n")

def run_adb(cmd, timeout=120):
    res = subprocess.run(["adb"] + cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True, timeout=timeout)
    return res.returncode, res.stdout.strip(), res.stderr.strip()

def check_device():
    code, out, _ = run_adb(["devices"])
    lines = out.splitlines()
    devices = [line.split()[0] for line in lines[1:] if "device" in line]
    if not devices:
        log("ERROR: No active Android emulator/device found. Please start an emulator.")
        sys.exit(1)
    return devices[0]

def pre_install_app():
    log("Pre-building and installing app and test APKs...")
    env = os.environ.copy()
    env.pop("ANDROID_PREFS_ROOT", None)
    build_cmd = ["./gradlew", "clean", "installDebug", "installDebugAndroidTest", "--no-configuration-cache"]
    res = subprocess.run(build_cmd, cwd=ROOT, env=env, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
    log(res.stdout)
    if res.returncode != 0:
        log("ERROR: Failed to install app and test APKs.")
        sys.exit(1)
    log("SUCCESS: App and test APKs pre-installed successfully.")

def prepare_english_environment():
    log("Setting device locale to en-US...")
    run_adb(["shell", "setprop", "persist.sys.locale", "en-US"])
    run_adb(["shell", "settings", "put", "system", "system_locales", "en-US"])
    run_adb(["shell", "am", "broadcast", "-a", "android.intent.action.LOCALE_CHANGED"])
    run_adb(["shell", "wm", "dismiss-keyguard"])
    time.sleep(3)

    remote_dir = "/data/local/tmp"
    pdf_files = ["tameshibu_episode1_2_en.pdf", "tameshibu_episode2_2_en.pdf"]
    for pdf in pdf_files:
        local_path = TESTDATA_DIR / pdf
        if local_path.exists():
            log(f"Pushing {pdf} to {remote_dir}/...")
            run_adb(["push", str(local_path), f"{remote_dir}/{pdf}"])
            run_adb(["shell", f"chmod 644 {remote_dir}/{pdf}"])
        else:
            log(f"WARNING: Local PDF {local_path} not found.")

def record_scene(scene_num):
    scene_name = f"scene_0{scene_num}"
    remote_path = f"/sdcard/{scene_name}.mp4"
    ASSETS_DIR.mkdir(parents=True, exist_ok=True)
    local_path = ASSETS_DIR / f"{scene_name}.mp4"

    log(f"\n==============================================")
    log(f" Recording English Scene {scene_num} ({scene_name}.mp4) via ShortsRecordingTest...")
    log(f"==============================================")

    run_adb(["shell", f"rm -f {remote_path}"])
    if local_path.exists():
        local_path.unlink()

    run_adb(["shell", "logcat", "-c"])

    log("Starting adb screenrecord (bit-rate 16Mbps)...")
    rec_proc = subprocess.Popen(["adb", "shell", "screenrecord", "--bit-rate", "16000000", remote_path])
    time.sleep(2)

    log(f"Running am instrument test for {scene_name}...")
    instrument_cmd = [
        "shell", "am", "instrument", "-w", "-r",
        "-e", "class", "com.takumayamada22.pdfutility.ShortsRecordingTest#recordScene",
        "-e", "scene", scene_name,
        "com.takumayamada22.pdfutility.test/androidx.test.runner.AndroidJUnitRunner"
    ]
    code, out, err = run_adb(instrument_cmd, timeout=120)
    log(out)

    _, logcat_out, _ = run_adb(["logcat", "-d", "-s", "ShortsTest:D", "TestRunner:D", "AndroidRuntime:E"])
    log(f"--- LOGCAT DIAGNOSTICS FOR {scene_name} ---\n{logcat_out}\n---------------------------------------------")

    if code != 0 or "FAILURES!!!" in out:
        log(f"ERROR: Instrumentation test failed for {scene_name}. Code: {code}, Err: {err}")
        run_adb(["shell", "pkill", "-INT", "screenrecord"])
        rec_proc.terminate()
        sys.exit(1)

    time.sleep(3)
    log("Stopping screenrecord...")
    run_adb(["shell", "pkill", "-INT", "screenrecord"])
    time.sleep(3)

    if rec_proc.poll() is None:
        rec_proc.terminate()
        try:
            rec_proc.wait(timeout=5)
        except subprocess.TimeoutExpired:
            rec_proc.kill()

    log(f"Pulling {remote_path} to {local_path}...")
    pull_code, _, pull_err = run_adb(["pull", remote_path, str(local_path)])
    if pull_code != 0 or not local_path.exists() or local_path.stat().st_size < 1024:
        log(f"ERROR: Failed to pull or invalid video file for {scene_name}. Err: {pull_err}")
        sys.exit(1)

    log(f"SUCCESS: Saved English video {local_path} (Size: {local_path.stat().st_size} bytes)")

def main():
    parser = argparse.ArgumentParser(description="Record English YouTube Shorts scenes via ShortsRecordingTest")
    parser.add_argument("scenes", nargs="+", help="Scene numbers (1-5) or 'all'")
    args = parser.parse_args()

    device = check_device()
    log(f"Using device: {device}")

    pre_install_app()
    log("Enabling touch visualization (show_touches: ON)...")
    run_adb(["shell", "settings", "put", "system", "show_touches", "1"])
    run_adb(["shell", "settings", "put", "system", "pointer_location", "0"])

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
                    log(f"Invalid scene number: {s} (must be 1-5)")
            except ValueError:
                log(f"Invalid argument: {s}")

    for num in requested_scenes:
        record_scene(num)

    log("\nAll requested YouTube Shorts scenes recorded successfully via ShortsRecordingTest!")

if __name__ == "__main__":
    main()
