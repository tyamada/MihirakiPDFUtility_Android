#!/usr/bin/env python3
"""Capture every v33 store scene in a fresh host process per language."""
import json
import hashlib
import os
from pathlib import Path
import subprocess
import sys
import time

ROOT = Path(__file__).resolve().parents[1]
CONFIG = ROOT / "scripts/store_capture_config.json"
WORK = ROOT / ".artifacts/store-capture"
STATE = WORK / "v33-language-runs.json"
INTERMEDIATE_FINGERPRINTS = (
    "5526e5870469fa8f0df20a88e205fddba894fe55ab9ee8854ea1726969842781",
)

def legacy_fingerprint():
    """Trust prior v33 records made before the adb stale-device recovery fix."""
    apks = [ROOT / "app/build/outputs/apk/debug/app-debug.apk",
            ROOT / "app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk"]
    prior_runner = subprocess.run(
        ["git", "show", "HEAD:scripts/capture_store_screenshots.py"],
        cwd=ROOT, check=True, stdout=subprocess.PIPE,
    ).stdout
    digest = hashlib.sha256()
    inputs = "".join(hashlib.sha256(p.read_bytes()).hexdigest() for p in apks)
    inputs += hashlib.sha256(prior_runner).hexdigest()
    inputs += hashlib.sha256(CONFIG.read_bytes()).hexdigest()
    return hashlib.sha256(inputs.encode()).hexdigest()

def main():
    config = json.loads(CONFIG.read_text())
    if config["versionCode"] != 33:
        raise SystemExit("store_capture_config.json must target versionCode 33")
    WORK.mkdir(parents=True, exist_ok=True)
    state = json.loads(STATE.read_text()) if STATE.exists() else {"versionCode": 33, "languages": {}}
    compatible = legacy_fingerprint()
    compatible_fingerprints = (compatible, *INTERMEDIATE_FINGERPRINTS)
    for language in config["languages"]:
        print(f"=== {language}: start ===", flush=True)
        command = [sys.executable, "scripts/capture_store_screenshots.py", "--skip-build", "--resume", "--clear-app-data"]
        for fingerprint in compatible_fingerprints:
            command.extend(["--compatible-fingerprint", fingerprint])
        command.extend(["--language", language])
        result = subprocess.run(
            command,
            cwd=ROOT, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True,
            env={**os.environ, "PYTHONDONTWRITEBYTECODE": "1"},
        )
        log = WORK / f"v33-{language}.log"
        log.write_text(result.stdout)
        print(result.stdout, end="" if result.stdout.endswith("\n") else "\n", flush=True)
        state["languages"][language] = {"exitCode": result.returncode, "log": str(log.relative_to(ROOT))}
        STATE.write_text(json.dumps(state, ensure_ascii=False, indent=2) + "\n")
        if result.returncode:
            print(f"=== {language}: failed; safe to resume ===", flush=True)
            return result.returncode
        print(f"=== {language}: complete ===", flush=True)
        time.sleep(5)
    state["complete"] = True
    STATE.write_text(json.dumps(state, ensure_ascii=False, indent=2) + "\n")
    print("All 14 languages completed.", flush=True)
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
