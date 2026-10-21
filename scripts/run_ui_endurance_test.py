#!/usr/bin/env python3
"""
MihirakiPDFViewer UI-Emulated Endurance Test Runner
Executes instrumented Compose UI tests (UiEnduranceTest.kt) via Gradle,
monitoring device memory usage and capturing logs into logs/.
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
    env['HOME'] = os.path.expanduser('~')
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
    parser = argparse.ArgumentParser(description="Run UI-Emulated Endurance Test for MihirakiPDFUtility")
    parser.add_argument("--duration", type=int, default=3, help="Test duration indicator (mins)")
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
    session_dir = logs_base / f"{timestamp}_{device_serial}_ui_endurance"
    session_dir.mkdir(parents=True, exist_ok=True)

    print(f"=== MihirakiPDFUtility UI-Emulated Endurance Test ===")
    print(f"Target Device: {device_serial}")
    print(f"Output Directory: {session_dir}")

    gradlew = './gradlew.bat' if os.name == 'nt' else './gradlew'
    test_cmd = [
        gradlew,
        'app:connectedDebugAndroidTest',
        '-Pandroid.testInstrumentationRunnerArguments.class=com.takumayamada22.pdfutility.UiEnduranceTest',
        f'-Pandroid.testInstrumentationRunnerArguments.duration={args.duration}',
        '--no-daemon'
    ]

    print(f"\n[RUNNING] Executing Instrumented UI Endurance Tests via Gradle...")
    start_time = time.time()
    code, out = run_cmd(test_cmd, cwd=root_dir)
    elapsed = int(time.time() - start_time)

    test_log = session_dir / "ui_endurance_test_result.log"
    test_log.write_text(out)

    # Capture logcat
    _, logcat_out = run_cmd(['adb', '-s', device_serial, 'logcat', '-d'])
    logcat_path = session_dir / "ui_endurance_logcat.log"
    logcat_path.write_text(logcat_out)

    status = "PASSED" if code == 0 else "FAILED"
    summary_path = session_dir / "ui_endurance_summary.txt"
    summary_content = f"""=== UI-Emulated Endurance Test Summary ===
Timestamp: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}
Device Serial: {device_serial}
Execution Duration: {elapsed} seconds
Status: {status}
Gradle Exit Code: {code}

Generated Artifacts in {session_dir.name}/:
- ui_endurance_test_result.log
- ui_endurance_logcat.log
- ui_endurance_summary.txt
"""
    summary_path.write_text(summary_content)
    print(f"\n=== UI Endurance Test Session Completed (Status: {status}) ===")
    print(f"All logs and reports saved to: {session_dir}")
    print(summary_content)

if __name__ == '__main__':
    main()
