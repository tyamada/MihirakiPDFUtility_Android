#!/usr/bin/env python3
"""
Organize changelog files into version-based subdirectories under store_listing/changelog/
"""

import shutil
from pathlib import Path

def main():
    root_dir = Path(__file__).resolve().parent.parent
    changelog_dir = root_dir / 'store_listing' / 'changelog'
    if not changelog_dir.exists():
        print("changelog directory not found.")
        return

    files = list(changelog_dir.glob('*.txt'))
    for file_path in files:
        name = file_path.name
        parts = name.split('_')
        if len(parts) >= 3:
            version = parts[1]
            target_dir = changelog_dir / f"v{version}"
            target_dir.mkdir(exist_ok=True)

            dest_path = target_dir / name
            shutil.move(str(file_path), str(dest_path))
            print(f"Moved {name} -> v{version}/{name}")

if __name__ == '__main__':
    main()
