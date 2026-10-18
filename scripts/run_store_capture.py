#!/usr/bin/env python3
"""Capture the configured locales sequentially in separate Python processes."""
import json
import os
from pathlib import Path
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[1]

def main():
    config = json.loads((ROOT / 'scripts/store_capture_config.json').read_text())
    work = ROOT / '.artifacts/store-capture'
    work.mkdir(parents=True, exist_ok=True)
    state = {'versionCode': config['versionCode'], 'languages': {}, 'complete': False}
    for lang in config['languages']:
        log = work / f'capture-{lang}.log'
        with log.open('w') as stream:
            result = subprocess.run([sys.executable, 'scripts/capture_store_screenshots.py', '--skip-build', '--resume', '--language', lang], cwd=ROOT, env={**os.environ, 'PYTHONDONTWRITEBYTECODE':'1'}, stdout=stream, stderr=subprocess.STDOUT)
        state['languages'][lang] = {'exitCode':result.returncode, 'log':str(log.relative_to(ROOT))}
        (work / 'language-runs.json').write_text(json.dumps(state, indent=2)+'\n')
        print(f'{lang}: exit {result.returncode}; {log}', flush=True)
        if result.returncode:
            return result.returncode
    state['complete'] = True
    (work / 'language-runs.json').write_text(json.dumps(state, indent=2)+'\n')
    return 0

if __name__ == '__main__':
    raise SystemExit(main())
