#!/usr/bin/env python3
"""
MihirakiPDFUtility Test Data Transfer Script
Transfers all sample manga PDF files (tameshibu*.pdf) and load test PDFs from PC/Mac testdata/
to the connected Android device via USB or Wi-Fi ADB.
"""

import os
import sys
import subprocess
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
    root_dir = Path(__file__).resolve().parent.parent
    testdata_dir = root_dir / 'testdata'

    if not testdata_dir.exists():
        print(f"ERROR: Local testdata/ directory not found at {testdata_dir}", file=sys.stderr)
        sys.exit(1)

    devices = get_connected_devices()
    if not devices:
        print("ERROR: No connected Android devices or emulators found via ADB (check USB or Wi-Fi connection).", file=sys.stderr)
        sys.exit(1)

    device_serial = devices[0]
    print(f"=== MihirakiPDFUtility Test Data Transfer Tool ===")
    print(f"Target Device: {device_serial}")

    pkg_name = 'com.github.tyamada.MihirakiPDFViewer_Android'
    device_test_dir = f"/sdcard/Android/data/{pkg_name}/files/testdata"

    print(f"Creating test directory on device: {device_test_dir}")
    run_cmd(['adb', '-s', device_serial, 'shell', f'mkdir -p {device_test_dir}'])

    # Target patterns: tameshibu*.pdf and load_test_1100_pages.pdf
    files_to_transfer = list(testdata_dir.glob('tameshibu*.pdf'))
    load_test_file = testdata_dir / 'load_test_1100_pages.pdf'
    if load_test_file.exists():
        files_to_transfer.append(load_test_file)

    print(f"Found {len(files_to_transfer)} test files to transfer.")
    success_count = 0

    for f in files_to_transfer:
        if f.is_file():
            dest_path = f"{device_test_dir}/{f.name}"
            print(f"Pushing {f.name} -> {dest_path} ...")
            code, out = run_cmd(['adb', '-s', device_serial, 'push', str(f), dest_path])
            if code == 0:
                success_count += 1
            else:
                print(f"WARNING: Failed to push {f.name}:\n{out}")

    print(f"\n=== Transfer Completed ===")
    print(f"Successfully transferred {success_count} / {len(files_to_transfer)} test files to device storage.")

if __name__ == '__main__':
    main()
