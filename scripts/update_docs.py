#!/usr/bin/env python3
"""
Update documentation files (README.md, README_ja.md, docs/*.html) to reference sample/ instead of testdata/
for sample PDFs, and update version numbers to v1.1.2.
"""

from pathlib import Path

def main():
    root_dir = Path(__file__).resolve().parent.parent

    files_to_update = [root_dir / 'README.md', root_dir / 'README_ja.md']
    docs_dir = root_dir / 'docs'
    if docs_dir.exists():
        files_to_update.extend(docs_dir.glob('*.html'))

    for path in files_to_update:
        if not path.exists():
            continue
        content = path.read_text(encoding='utf-8')

        # Replace testdata/tameshibu with sample/tameshibu
        updated = content.replace('/testdata/tameshibu_', '/sample/tameshibu_')
        updated = updated.replace('testdata/tameshibu_', 'sample/tameshibu_')

        # Update version strings
        updated = updated.replace('1.1.1', '1.1.2').replace('v1.1.1', 'v1.1.2')

        if updated != content:
            path.write_text(updated, encoding='utf-8')
            print(f"Updated: {path.relative_to(root_dir)}")

if __name__ == '__main__':
    main()
