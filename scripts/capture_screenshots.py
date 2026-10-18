#!/usr/bin/env python3
import os
import subprocess
import time
import sys
import hashlib

# Clean up conflicting Android environment variables to prevent AGP AndroidLocationsException
os.environ.pop("ANDROID_PREFS_ROOT", None)

# Supported languages and their locale strings for Android
# File naming convention: ja, en, ko, zh_cn
LANGUAGES = {
    "ja": "ja-JP",
    "en": "en-US",
    "ko": "ko-KR",
    "zh_cn": "zh-CN"
}

# Screen capture targets corresponding to the 5 store listing screenshots
SCREENSHOT_TARGETS = [
    "01_library_or_cover",
    "02_two_page_spread",
    "03_thumbnail_view",
    "04_text_search",
    "05_settings"
]

def run_adb(cmd):
    full_cmd = ["adb"] + cmd
    result = subprocess.run(full_cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
    return result.returncode, result.stdout.strip(), result.stderr.strip()

def check_device():
    run_adb(["start-server"])
    for _ in range(30):
        code, out, err = run_adb(["devices"])
        lines = out.splitlines()
        devices = [line.split()[0] for line in lines[1:] if "device" in line]
        if devices:
            print(f"Found active device: {devices[0]}")
            return devices[0]
        time.sleep(2)
    print("FATAL ERROR: No Android device/emulator found. Please start a virtual device. Aborting.")
    sys.exit(1)

def get_current_locale():
    code, out, _ = run_adb(["shell", "getprop", "persist.sys.locale"])
    if code == 0 and out.strip():
        return out.strip()
    code, out, _ = run_adb(["shell", "getprop", "ro.product.locale"])
    return out.strip()

def set_locale(locale_str):
    current = get_current_locale()
    print(f"Current device locale: {current}, Target locale: {locale_str}")
    if locale_str in current:
        print("Locale matches. Skipping locale change.")
        return

    print(f"Setting device locale to: {locale_str}")
    run_adb(["shell", "setprop", "persist.sys.locale", locale_str])
    run_adb(["shell", "setprop", "sys.locale", locale_str])

    # Broadcast locale change to update system UI without dropping emulator connection
    print("Broadcasting locale change...")
    run_adb(["shell", "am", "broadcast", "-a", "android.intent.action.LOCALE_CHANGED"])
    time.sleep(5)

    # Dismiss keyguard / unlock screen to prevent lockscreen blocking tests
    print("Dismissing keyguard / unlocking device screen...")
    run_adb(["shell", "wm", "dismiss-keyguard"])
    run_adb(["shell", "input", "keyevent", "82"])
    time.sleep(2)

def set_landscape_orientation():
    print("Setting orientation to Landscape...")
    run_adb(["shell", "settings", "put", "system", "accelerometer_rotation", "0"])
    run_adb(["shell", "settings", "put", "system", "user_rotation", "1"])

def detect_device_type():
    code, out, _ = run_adb(["shell", "wm", "size"])
    print(f"Screen size info: {out}")
    return "phone"

def verify_and_push_file(local_path, remote_dir, remote_filename):
    if not os.path.exists(local_path):
        print(f"\n[FATAL ERROR] 撮影用PDFファイルが元の場所に存在しません。")
        print(f"Expected path: {os.path.abspath(local_path)}")
        print(f"撮影用ファイルが配置されていないため、処理を中断します。\n")
        sys.exit(1)

    remote_path = f"{remote_dir}/{remote_filename}"
    print(f"Ensuring remote directory exists: {remote_dir}")
    run_adb(["shell", "mkdir", "-p", remote_dir])

    # Push file
    print(f"Pushing {local_path} to {remote_path}...")
    run_adb(["push", local_path, remote_path])

    # Verify existence and size on device
    code, out, err = run_adb(["shell", f"ls -l {remote_path}"])
    if code != 0 or remote_filename not in out:
        print(f"ERROR: File transfer verification failed for {remote_path}. Retrying push...")
        run_adb(["push", local_path, remote_path])
        code, out, err = run_adb(["shell", f"ls -l {remote_path}"])
        if code != 0 or remote_filename not in out:
            print(f"FATAL ERROR: Failed to verify pushed file on device: {remote_path}. Aborting.")
            sys.exit(1)

    print(f"VERIFIED: Sample PDF successfully transferred and verified on device: {remote_path}")
    print(f"Device file info: {out}")

def is_bad_image(filepath):
    if not os.path.exists(filepath) or os.path.getsize(filepath) < 1024:
        return True

    try:
        from PIL import Image
        img = Image.open(filepath).convert("L")
        extrema = img.getextrema()
        if extrema[0] == extrema[1]:
            print(f"Bad image detected (solid color {extrema[0]}): {filepath}")
            return True
        import numpy as np
        arr = np.array(img)
        avg = np.mean(arr)
        if avg < 5 or avg > 250:
            print(f"Bad image detected (extreme average brightness {avg}): {filepath}")
            return True
    except ImportError:
        pass

    return False

def get_image_hash(filepath):
    hasher = hashlib.md5()
    with open(filepath, 'rb') as f:
        buf = f.read()
        hasher.update(buf)
    return hasher.hexdigest()

def validate_unique_screenshots(screenshot_paths):
    hashes = {}
    for target, path in screenshot_paths.items():
        if not os.path.exists(path):
            continue
        file_hash = get_image_hash(path)
        if file_hash in hashes.values():
            print(f"\n[QA ERROR] 画面遷移検査に失敗しました！")
            print(f"スクリーンショット '{target}' の画像が他の画面と同一（重複）です。")
            print(f"画面が正しく遷移していないため、致命的エラーとして処理を中断します。\n")
            sys.exit(1)
        hashes[target] = file_hash
    print("QA CHECK PASSED: All captured screenshots are unique and distinct.")

def main():
    print("Pre-checking source PDF files...")
    for lang_key in LANGUAGES.keys():
        pdf_filename = f"tameshibu_episode1_2_{lang_key}.pdf"
        local_pdf_path = f"testdata/{pdf_filename}"
        if not os.path.exists(local_pdf_path):
            print(f"\n[FATAL ERROR] 撮影用PDFファイルが元の場所に存在しません。")
            print(f"Missing file path: {os.path.abspath(local_pdf_path)}")
            print(f"全ての対応言語のファイルが揃っていることを確認してください。処理を中断します。\n")
            sys.exit(1)
    print("All source PDF files verified locally.")

    device_id = check_device()
    device_type = detect_device_type()

    print("Building and installing test APK if needed...")
    build_code = subprocess.run(["./gradlew", "clean", "installDebug", "installDebugAndroidTest"], stdout=subprocess.PIPE, text=True)
    if build_code.returncode != 0:
        print("Build failed:", build_code.stdout)
        sys.exit(1)

    for lang_key, locale_str in LANGUAGES.items():
        print(f"\n==================== Processing Language: {lang_key} ({locale_str}) ====================")

        # 1. Set locale and broadcast (safe without dropping emulator connection)
        set_locale(locale_str)

        # 2. Set Landscape orientation
        set_landscape_orientation()

        # 3. Push sample PDF for this language and verify
        pdf_filename = f"tameshibu_episode1_2_{lang_key}.pdf"
        local_pdf_path = f"testdata/{pdf_filename}"
        remote_dir = "/sdcard/Download/TestPdfs"

        verify_and_push_file(local_pdf_path, remote_dir, pdf_filename)

        # 4. Run Instrumented test (Opening file, rendering, navigating views)
        print(f"Running screenshot test for {lang_key}...")
        test_cmd = ["./gradlew", "clean", "connectedAndroidTest", "-Pandroid.testInstrumentationRunnerArguments.class=com.github.tyamada.mihirakipdfviewer_android.StoreScreenshotTest"]
        res = subprocess.run(test_cmd)

        if res.returncode != 0:
            print(f"ERROR: Instrumented test failed for language '{lang_key}' with exit code {res.returncode}.")
            print(f"FATAL ERROR: Failed to open file, render PDF, or capture screenshots for language '{lang_key}'. Aborting process.")
            sys.exit(1)

        # 5. Pull screenshots from app external files dir and validate
        output_dir = os.path.expanduser(f"store_listing/screenshots/{device_type}/{lang_key}")
        os.makedirs(output_dir, exist_ok=True)

        temp_pull_dir = "/tmp/mihiraki_screenshots"
        os.makedirs(temp_pull_dir, exist_ok=True)

        captured_paths = {}
        for target in SCREENSHOT_TARGETS:
            remote_file = f"/sdcard/Android/data/com.github.tyamada.mihirakipdfviewer_android/files/{target}.png"
            local_temp_file = os.path.join(temp_pull_dir, f"{target}.png")
            run_adb(["pull", remote_file, local_temp_file])

            if os.path.exists(local_temp_file):
                if is_bad_image(local_temp_file):
                    print(f"ERROR: Screenshot '{target}' for language '{lang_key}' is invalid (black/white/error screen). Aborting.")
                    sys.exit(1)

                final_path = os.path.join(output_dir, f"{target}.png")
                os.rename(local_temp_file, final_path)
                captured_paths[target] = final_path
                print(f"Saved valid screenshot: {final_path}")
            else:
                print(f"ERROR: Required screenshot '{target}.png' for language '{lang_key}' was not found in app external files dir. Aborting.")
                sys.exit(1)

        # 6. Run unique screenshot validation (QA Check for duplicate screens)
        validate_unique_screenshots(captured_paths)

    print("\nAll screenshot capture tasks completed successfully!")

if __name__ == "__main__":
    main()
