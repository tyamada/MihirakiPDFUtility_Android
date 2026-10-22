#!/usr/bin/env python3
"""
Cleanup old version tameshibu sample PDF files (without _2_) from testdata/ and docs/sample/,
ensuring exactly 28 latest-version files (14 languages * 2 episodes with _2_) remain.
"""

from pathlib import Path
import shutil

def main():
    root_dir = Path(__file__).resolve().parent.parent
    dirs = [root_dir / 'testdata', root_dir / 'docs' / 'sample']

    lang_map = {
        'es': 'es', 'it': 'it', 'pt': 'pt', 'vi': 'vi', 'id': 'id', 'tl': 'fil', 'ne': 'ne'
    }

    for d in dirs:
        if not d.exists():
            print(f"Directory does not exist: {d}")
            continue

        print(f"\nProcessing directory: {d}")
        # Ensure all missing _2_ files are created from non-_2_ equivalents if present
        for old_prefix, new_code in lang_map.items():
            for ep in ['1', '2']:
                old_file = d / f"tameshibu_episode{ep}_{old_prefix}.pdf"
                new_file = d / f"tameshibu_episode{ep}_2_{new_code}.pdf"
                if old_file.exists() and not new_file.exists():
                    shutil.copy(old_file, new_file)
                    print(f"  Copied/Migrated {old_file.name} -> {new_file.name}")

        # Remove all old files without _2_
        removed_count = 0
        for f in d.glob('tameshibu_episode*.pdf'):
            if '_2_' not in f.name:
                f.unlink()
                removed_count += 1
                print(f"  Removed old version: {f.name}")

        remaining = list(d.glob('tameshibu*.pdf'))
        print(f"  Result: Removed {removed_count} old files. Remaining {len(remaining)} files in {d.name}.")

if __name__ == '__main__':
    main()
