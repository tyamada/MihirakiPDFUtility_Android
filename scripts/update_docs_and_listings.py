#!/usr/bin/env python3
"""Validate release docs without rewriting history.

Only explicit current-app-version markers can be updated, and only with --write.
The default used by the pre-commit hook performs no writes.
"""
import argparse
from pathlib import Path
import re
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parent.parent
START = '<!-- current-app-version:start -->'
END = '<!-- current-app-version:end -->'


def get_gradle_version(root=ROOT):
    text = (root / 'app/build.gradle.kts').read_text()
    name = re.search(r'versionName\s*=\s*"([^"]+)"', text)
    code = re.search(r'versionCode\s*=\s*(\d+)', text)
    if not name or not code:
        raise ValueError('Missing app version; refusing to guess a historical default')
    return name.group(1), code.group(1)


def current_version_text(text, name, code):
    if text.count(START) != text.count(END):
        raise ValueError('Unmatched current-app-version markers')
    return re.sub(re.escape(START) + r'.*?' + re.escape(END),
                  lambda _: START + f'{name} ({code})' + END, text, flags=re.S)


def verify_help_screen_strings(root=ROOT):
    required = {'help_item_open_save', 'help_item_page_ops', 'help_item_security', 'help_item_support'}
    for language in ['values', 'values-ja']:
        path = root / 'app/src/main/res' / language / 'strings.xml'
        elements = {x.get('name'): ''.join(x.itertext()).strip() for x in ET.parse(path).getroot().findall('string')}
        missing = sorted(key for key in required if not elements.get(key))
        if missing:
            raise ValueError(f'{language}: missing/empty help keys: {missing}')


def sync_documents(root=ROOT, write=False):
    name, code = get_gradle_version(root)
    verify_help_screen_strings(root)
    targets = [root / 'README.md', root / 'README_ja.md']
    targets += sorted((root / 'docs').glob('*.html'))
    targets += sorted((root / 'store_listing').glob('metadata_*.txt'))
    pending = []
    for path in targets:
        if not path.exists():
            continue
        original = path.read_text()
        updated = current_version_text(original, name, code)
        if updated != original:
            pending.append(str(path.relative_to(root)))
            if write:
                path.write_text(updated)
    return pending


def main(argv=None):
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument('--write', action='store_true', help='Update explicitly marked current-version fields only')
    args = p.parse_args(argv)
    try:
        pending = sync_documents(write=args.write)
        print(f'App version: {get_gradle_version()[0]} ({get_gradle_version()[1]})')
        print(f'Explicit current-version fields needing update: {len(pending)}')
        print('Historical versions and unmarked prose were preserved.')
        return 0 if args.write or not pending else 1
    except (OSError, ValueError, ET.ParseError) as e:
        print(f'Document validation failed: {e}')
        return 1


if __name__ == '__main__':
    raise SystemExit(main())
