#!/usr/bin/env python3
"""
MihirakiPDFUtility Test Data Transfer Script
Dynamically parses SamplePdfCatalog.kt to extract sample PDF filenames,
combines them with load/search test PDFs, cleans up device testdata,
and transfers them from PC/Mac testdata/ to the connected Android device via ADB.
"""

import os
import sys
import subprocess
import re
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

def get_sample_filenames_from_catalog(root_dir):
    catalog_path = root_dir / 'app' / 'src' / 'main' / 'java' / 'com' / 'takumayamada22' / 'pdfutility' / 'data' / 'SamplePdfCatalog.kt'
    if not catalog_path.exists():
        print(f"WARNING: SamplePdfCatalog.kt not found at {catalog_path}, falling back to glob.")
        return []

    content = catalog_path.read_text(encoding='utf-8')
    filenames = re.findall(r'fileName\s*=\s*"([^"]+)"', content)
    return filenames

def main():
    root_dir = Path(__file__).resolve().parent.parent
    testdata_dir = root_dir / 'testdata'

    if not testdata_dir.exists():
        print(f"ERROR: Local testdata/ directory not found at {testdata_dir}", file=sys.stderr)
        sys.exit(1)

    devices = get_connected_devices()
    if not devices:
        print("ERROR: No connected Android devices or emulators found via ADB.", file=sys.stderr)
        sys.exit(1)

    device_serial = devices[0]
    print(f"=== MihirakiPDFUtility Dynamic Test Data Transfer Tool ===")
    print(f"Target Device: {device_serial}")

    # Dynamically extract filenames from SamplePdfCatalog.kt
    catalog_filenames = get_sample_filenames_from_catalog(root_dir)
    print(f"Extracted {len(catalog_filenames)} sample PDF filenames from SamplePdfCatalog.kt")

    # Add benchmark/load/search test files
    extra_test_files = ['load_test_1100_pages.pdf', 'search_test_600_hits.pdf', 'search_test_600_hits_en.pdf']
    all_target_filenames = list(set(catalog_filenames + extra_test_files))

    pkg_name = 'com.github.tyamada.MihirakiPDFViewer_Android'
    device_test_dir = f"/sdcard/Android/data/{pkg_name}/files/testdata"

    print(f"Cleaning up old test files from device: {device_test_dir}")
    run_cmd(['adb', '-s', device_serial, 'shell', f'rm -rf {device_test_dir}/*'])
    run_cmd(['adb', '-s', device_serial, 'shell', f'mkdir -p {device_test_dir}'])

    success_count = 0
    for filename in all_target_filenames:
        local_file = testdata_dir / filename
        if local_file.exists() and local_file.is_file():
            dest_path = f"{device_test_dir}/{filename}"
            print(f"Pushing {filename} -> {dest_path} ...")
            code, out = run_cmd(['adb', '-s', device_serial, 'push', str(local_file), dest_path])
            if code == 0:
                success_count += 1
            else:
                print(f"WARNING: Failed to push {filename}:\n{out}")
        else:
            print(f"NOTICE: Catalog file {filename} not found locally in testdata/.")

    print(f"\n=== Dynamic Transfer Completed ===")
    print(f"Successfully transferred {success_count} / {len(all_target_filenames)} target test files to device storage.")

if __name__ == '__main__':
    main()
