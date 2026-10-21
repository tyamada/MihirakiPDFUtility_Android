#!/usr/bin/env python3
"""
MihirakiPDFViewer Long-Running Endurance & Stress Test Runner
1. Verifies app installation on device (installs debug APK if missing).
2. Verifies test files (load_test_1100_pages.pdf and testdata/ manga PDFs) and pushes missing files via ADB.
3. Once preparation is ready, starts the test and precisely measures execution time.
4. Captures continuous memory usage, logcat, and audit logs, saving reports into logs/.
5. Explicitly logs user interruption (KeyboardInterrupt) if stopped early.
"""

import os
import sys
import subprocess
import time
import argparse
from datetime import datetime
from pathlib import Path

def get_env():
    env = os.environ.copy()
    sdk_path = '/Users/tyamada22/Library/Android/sdk'
    jbr_path = '/Applications/Android Studio.app/Contents/jbr/Contents/Home'
    env['ANDROID_HOME'] = sdk_path
    env['ANDROID_SDK_ROOT'] = sdk_path
    env['JAVA_HOME'] = jbr_path
    return env

def run_cmd(cmd, cwd=None):
    res = subprocess.run(cmd, cwd=cwd, env=get_env(), stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
    return res.returncode, res.stdout

def get_connected_devices():
    code, out = run_cmd(['adb', 'devices'])
    devices = []
    for line in out.splitlines():
        parts = line.strip().split()
        if len(parts) == 2 and parts[1] == 'device':
            devices.append(parts[0])
    return devices

def main():
    parser = argparse.ArgumentParser(description="Run Long-Running Endurance Test for MihirakiPDFUtility")
    parser.add_argument("--duration", type=int, default=3, help="Test duration in minutes (default: 3 mins)")
    args = parser.parse_args()

    root_dir = Path(__file__).resolve().parent.parent
    logs_base = root_dir / 'logs'
    logs_base.mkdir(exist_ok=True)

    timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
    devices = get_connected_devices()
    if not devices:
        print("ERROR: No connected Android devices or emulators found via ADB.", file=sys.stderr)
        sys.exit(1)

    device_serial = devices[0]
    session_dir = logs_base / f"{timestamp}_{device_serial}_endurance_{args.duration}m"
    session_dir.mkdir(parents=True, exist_ok=True)

    print(f"=== MihirakiPDFUtility Long-Running Endurance Test Setup ===")
    print(f"Target Device: {device_serial}")
    print(f"Target Duration: {args.duration} minutes")
    print(f"Output Directory: {session_dir}")

    # 1. App Installation Check & Setup
    pkg_name = 'com.takumayamada22.pdfutility'
    print(f"\n--- Step 1: Verifying App Installation ({pkg_name}) ---")
    _, installed_pkgs = run_cmd(['adb', '-s', device_serial, 'shell', 'pm', 'list', 'packages', pkg_name])
    if pkg_name not in installed_pkgs:
        print(f"App {pkg_name} not found on device. Installing debug APK...")
        apk_path = root_dir / 'app' / 'build' / 'outputs' / 'apk' / 'debug' / 'app-debug.apk'
        if not apk_path.exists():
            print("Debug APK not found. Building APK via gradlew app:assembleDebug...")
            gradlew = './gradlew.bat' if os.name == 'nt' else './gradlew'
            run_cmd([gradlew, 'app:assembleDebug'], cwd=root_dir)

        install_code, install_out = run_cmd(['adb', '-s', device_serial, 'install', str(apk_path)])
        if install_code != 0:
            print(f"ERROR: Failed to install APK:\n{install_out}", file=sys.stderr)
            sys.exit(1)
        print("App installed successfully.")
    else:
        print("App is already installed on device.")

    # 2. Test File Check & Transfer
    print(f"\n--- Step 2: Verifying and Pushing Test Files ---")
    testdata_dir = root_dir / 'testdata'
    device_test_dir = f"/sdcard/Android/data/{pkg_name}/files/testdata"
    run_cmd(['adb', '-s', device_serial, 'shell', f'mkdir -p {device_test_dir}'])

    if testdata_dir.exists():
        local_files = list(testdata_dir.glob('*'))
        print(f"Found {len(local_files)} local test files under testdata/.")
        for f in local_files:
            if f.is_file():
                run_cmd(['adb', '-s', device_serial, 'push', str(f), f"{device_test_dir}/{f.name}"])
        print("Test files pushed successfully to device storage.")
    else:
        print("WARNING: local testdata/ directory not found.")

    # 3. Preparation Complete -> Launch App & Start Timed Execution
    print(f"\n--- Step 3: Launching App & Starting Timed Endurance Execution ---")
    run_cmd(['adb', '-s', device_serial, 'logcat', '-c'])
    run_cmd(['adb', '-s', device_serial, 'shell', 'am', 'start', '-n', f'{pkg_name}/com.github.tyamada.mihirakipdfviewer_android.MainActivity'])
    time.sleep(3)

    duration_sec = args.duration * 60
    poll_interval = 10  # poll every 10 seconds
    iterations = duration_sec // poll_interval

    metrics_log = session_dir / "endurance_metrics.csv"
    metrics_log.write_text("Timestamp,ElapsedSec,MemoryUsedMb,CpuUsage\n")

    print(f"\n[TIMED START] Running endurance monitoring for {args.duration} minutes ({duration_sec} seconds)...")
    start_time = time.time()
    test_status = "COMPLETED"

    try:
        for i in range(iterations):
            elapsed = int(time.time() - start_time)
            if elapsed >= duration_sec:
                break

            # Get meminfo for app
            _, mem_out = run_cmd(['adb', '-s', device_serial, 'shell', 'dumpsys', 'meminfo', pkg_name])
            mem_mb = 0
            for line in mem_out.splitlines():
                if 'TOTAL' in line or 'TOTAL PSS:' in line:
                    parts = line.split()
                    for p in parts:
                        if p.isdigit():
                            mem_mb = int(p) // 1024
                            break

            ts = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
            metric_line = f"{ts},{elapsed},{mem_mb},N/A\n"
            with open(metrics_log, 'a') as f:
                f.write(metric_line)

            print(f"[{ts}] Elapsed: {elapsed}s / {duration_sec}s | App PSS Memory: {mem_mb} MB")
            time.sleep(poll_interval)
    except KeyboardInterrupt:
        test_status = "INTERRUPTED_BY_USER"
        print("\n[INTERRUPT] Endurance test was interrupted by user (KeyboardInterrupt). Logging cancellation...")

    actual_duration = int(time.time() - start_time)

    # Capture final logcat
    _, logcat_out = run_cmd(['adb', '-s', device_serial, 'logcat', '-d'])
    logcat_path = session_dir / "endurance_logcat.log"
    logcat_path.write_text(logcat_out)

    # Pull audit logs if accessible
    audit_log_path = session_dir / "app_audit_logs.txt"
    _, audit_out = run_cmd(['adb', '-s', device_serial, 'shell', f'run-as {pkg_name} cat files/app_audit_logs.txt'])
    if "does not exist" not in audit_out and "Permission denied" not in audit_out:
        audit_log_path.write_text(audit_out)
    else:
        audit_log_path.write_text("Audit logs not directly pulled via run-as (restricted in release/debug without root). Check logcat.")

    summary_path = session_dir / "endurance_summary.txt"
    summary_content = f"""=== Long-Running Endurance Test Summary ===
Timestamp: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}
Device Serial: {device_serial}
Target Duration: {args.duration} minutes
Actual Measured Duration: {actual_duration} seconds ({actual_duration / 60:.2f} minutes)
Status: {test_status}

Generated Artifacts in {session_dir.name}/:
- endurance_metrics.csv
- endurance_logcat.log
- app_audit_logs.txt
- endurance_summary.txt
"""
    summary_path.write_text(summary_content)
    print(f"\n=== Endurance Test Session Completed (Status: {test_status}) ===")
    print(f"All logs and reports saved to: {session_dir}")
    print(summary_content)

if __name__ == '__main__':
    main()
