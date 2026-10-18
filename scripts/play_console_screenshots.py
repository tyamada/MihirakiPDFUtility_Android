#!/usr/bin/env python3
"""Audit and stage the supported Play Console screenshot groups.

Chromebook screenshots are intentionally excluded from API uploads because the
Android Publisher API has no Chromebook image type. They remain a manual
Play Console task. Staged edits are never committed unless explicitly requested.
"""
import argparse
import csv
from datetime import datetime, timezone
import hashlib
import json
import os
from pathlib import Path
import struct
import sys

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / 'scripts'))
import capture_store_screenshots as capture  # noqa: E402

PACKAGE = capture.PACKAGE
SCOPES = ['https://www.googleapis.com/auth/androidpublisher']
DEVICE_TYPES = {
    'Smartphone_6.3': 'phoneScreenshots',
    'Tablet_7': 'sevenInchScreenshots',
    'Tablet_10': 'tenInchScreenshots',
}
PLAY_LANGUAGE_BY_CAPTURE_LOCALE = {
    'ja-JP': 'ja-JP', 'en-US': 'en-US', 'ko-KR': 'ko-KR', 'zh-TW': 'zh-TW',
    'de-DE': 'de-DE', 'fr-FR': 'fr-FR', 'es-ES': 'es-ES', 'pt-BR': 'pt-BR',
    'zh-CN': 'zh-CN', 'vi-VN': 'vi', 'fil-PH': 'fil', 'ne-NP': 'ne-NP',
    'id-ID': 'id', 'it-IT': 'it-IT',
}
STATE_PATH = ROOT / '.artifacts/play-console/screenshot-edit.json'
MANIFEST_PATH = ROOT / '.artifacts/play-console/screenshot-manifest.json'


def sha(path):
    digest = hashlib.sha256()
    with path.open('rb') as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b''):
            digest.update(block)
    return digest.hexdigest()


def png_info(path):
    with path.open('rb') as stream:
        header = stream.read(26)
    if len(header) != 26 or header[:8] != b'\x89PNG\r\n\x1a\n' or header[12:16] != b'IHDR':
        raise ValueError(f'Not a valid PNG: {path}')
    width, height = struct.unpack('>II', header[16:24])
    bit_depth, color_type = header[24:26]
    if bit_depth != 8 or color_type != 2:
        raise ValueError(f'Play listing PNG must be RGB 24-bit without alpha: {path}')
    if min(width, height) < 320 or max(width, height) > 3840 or max(width, height) > 2 * min(width, height):
        raise ValueError(f'Play screenshot dimensions/aspect ratio are not accepted: {path} ({width}x{height})')
    return width, height


def build_manifest():
    entries = []
    issues = []
    expected_langs = set(capture.LANGUAGES)
    for device, image_type in DEVICE_TYPES.items():
        scenes = capture.selected_scenes(device)
        if not 2 <= len(scenes) <= 8:
            issues.append(f'{device}: {len(scenes)} screenshots; Play supports 2–8 per device type')
        for lang in capture.LANGUAGES:
            found_locales = set()
            for order, scene in enumerate(scenes, 1):
                orientation = capture.orientation(device, scene)
                png = capture.OUT / device / f'{lang}_{scene}_{orientation}.png'
                record = png.with_suffix('.json')
                if not png.is_file() or not record.is_file():
                    issues.append(f'Missing screenshot or record: {png}')
                    continue
                metadata = json.loads(record.read_text())
                if metadata.get('versionCode') != 33:
                    issues.append(f'Wrong versionCode: {record}')
                if metadata.get('validation') != 'automated-pass; visual-reviewed':
                    issues.append(f'Screenshot is not fully reviewed: {record}')
                if metadata.get('imageSha256') != sha(png):
                    issues.append(f'Image SHA-256 mismatch: {png}')
                if metadata.get('scene') != scene or metadata.get('language') != lang or metadata.get('avd') != device:
                    issues.append(f'Record does not match filename/device: {record}')
                locale = metadata.get('locale')
                if not locale:
                    issues.append(f'Missing BCP-47 locale: {record}')
                else:
                    found_locales.add(locale)
                try:
                    width, height = png_info(png)
                except ValueError as error:
                    issues.append(str(error))
                    continue
                entries.append({
                    'language': lang,
                    'locale': locale,
                    'storeLanguage': PLAY_LANGUAGE_BY_CAPTURE_LOCALE.get(locale),
                    'device': device,
                    'imageType': image_type,
                    'order': order,
                    'scene': scene,
                    'path': str(png.relative_to(ROOT)),
                    'sha256': metadata['imageSha256'],
                    'width': width,
                    'height': height,
                })
                if locale not in PLAY_LANGUAGE_BY_CAPTURE_LOCALE:
                    issues.append(f'No Google Play language mapping for {locale}: {record}')
            if len(found_locales) != 1:
                issues.append(f'{device}/{lang}: expected one locale tag, found {sorted(found_locales)}')
    if expected_langs != set(capture.LANGUAGES):
        issues.append('Capture language configuration contains duplicates')
    if issues:
        raise RuntimeError('Preflight failed:\n- ' + '\n- '.join(issues[:50]))
    chromium_count = sum(len(capture.selected_scenes('Chromebook_FHD')) for _ in capture.LANGUAGES)
    return {
        'packageName': PACKAGE,
        'versionCode': 33,
        'apiImageCount': len(entries),
        'chromebookManualImageCount': chromium_count,
        'entries': entries,
        'generatedAt': datetime.now(timezone.utc).isoformat(),
    }


def save_manifest(manifest):
    MANIFEST_PATH.parent.mkdir(parents=True, exist_ok=True)
    MANIFEST_PATH.write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + '\n')


def manifest_digest(manifest):
    canonical = json.dumps(manifest['entries'], sort_keys=True, separators=(',', ':')).encode()
    return hashlib.sha256(canonical).hexdigest()


def load_api(service_account_path):
    from google.auth.exceptions import GoogleAuthError
    from google.oauth2 import service_account
    from googleapiclient.discovery import build
    path = Path(service_account_path).expanduser().resolve()
    if ROOT == path or ROOT in path.parents:
        raise RuntimeError('Store service-account JSON outside the repository workspace.')
    try:
        credentials = service_account.Credentials.from_service_account_file(str(path), scopes=SCOPES)
    except (OSError, ValueError, GoogleAuthError) as error:
        raise RuntimeError(f'Could not load service-account credentials: {error}') from error
    return build('androidpublisher', 'v3', credentials=credentials, cache_discovery=False)


def locale_groups(manifest):
    groups = {}
    for entry in manifest['entries']:
        groups.setdefault((entry['storeLanguage'], entry['imageType']), []).append(entry)
    for values in groups.values():
        values.sort(key=lambda item: item['order'])
    return groups


def read_markdown_listing(path):
    import re
    content = path.read_text(encoding='utf-8')
    sections = re.findall(r'^## [^\n]+\n(.*?)(?=^## |\Z)', content, flags=re.M | re.S)
    if len(sections) < 3:
        raise ValueError(f'Listing draft does not have name, short description, and full description: {path}')
    fields = [section.strip() for section in sections[:3]]
    title, short_description, full_description = [field.splitlines()[0].strip() if i < 2 else field
                                                   for i, field in enumerate(fields)]
    return {'title': title, 'shortDescription': short_description,
            'fullDescription': full_description.replace("\\'", "'")}


def missing_listing_content():
    """Load reviewed local listing drafts for all screenshot locales."""
    locale_aliases = {'fil': 'fil', 'id': 'id', 'ne': 'ne-NP', 'vi': 'vi'}
    csv_path = ROOT / 'store_listing/PLAY_STORE_TRANSLATIONS.csv'
    listings = {}
    with csv_path.open(encoding='utf-8', newline='') as stream:
        for row in csv.DictReader(stream):
            locale = locale_aliases.get(row['Locale'], row['Locale'])
            listings[locale] = {
                'title': row['Title'],
                'shortDescription': row['Short Description'],
                'fullDescription': row['Full Description'],
            }
    listings['it-IT'] = read_markdown_listing(ROOT / 'store_listing/STORE_LISTING.it.md')
    return listings


def stage(args, manifest):
    if STATE_PATH.exists():
        previous = json.loads(STATE_PATH.read_text())
        if previous.get('status') != 'committed':
            raise RuntimeError(f'A staged edit already exists at {STATE_PATH}; commit or discard it first.')
    api = load_api(args.service_account)
    edit = api.edits().insert(packageName=PACKAGE, body={}).execute()
    state = {
        'packageName': PACKAGE,
        'editId': edit['id'],
        'createdAt': datetime.now(timezone.utc).isoformat(),
        'manifestSha256': manifest_digest(manifest),
        'status': 'staging',
    }
    STATE_PATH.parent.mkdir(parents=True, exist_ok=True)
    STATE_PATH.write_text(json.dumps(state, indent=2) + '\n')
    try:
        active = api.edits().listings().list(packageName=PACKAGE, editId=edit['id']).execute()
        available = {item['language'] for item in active.get('listings', [])}
        required = {key[0] for key in locale_groups(manifest)}
        missing = sorted(required - available)
        listing_drafts = missing_listing_content()
        unsupported = sorted(set(missing) - set(listing_drafts))
        if unsupported:
            raise RuntimeError('These localized store listings must exist before image upload, and no local draft was found: '
                               + ', '.join(unsupported))
        for locale in missing:
            listing = listing_drafts[locale]
            api.edits().listings().update(packageName=PACKAGE, editId=edit['id'], language=locale,
                                          body={'language': locale, **listing}).execute()
            print(f'Added missing {locale} store listing from the repository draft to edit {edit["id"]}.')
        groups = locale_groups(manifest)
        for locale, image_type in groups:
            api.edits().images().deleteall(packageName=PACKAGE, editId=edit['id'], language=locale,
                                           imageType=image_type).execute()
        from googleapiclient.http import MediaFileUpload
        for (locale, image_type), entries in groups.items():
            for entry in entries:
                media = MediaFileUpload(str(ROOT / entry['path']), mimetype='image/png', resumable=True)
                api.edits().images().upload(packageName=PACKAGE, editId=edit['id'], language=locale,
                                            imageType=image_type, media_body=media).execute()
        validation = api.edits().validate(packageName=PACKAGE, editId=edit['id']).execute()
        state.update(status='staged-uncommitted', validation=validation)
        STATE_PATH.write_text(json.dumps(state, ensure_ascii=False, indent=2) + '\n')
        print(f'Staged {manifest["apiImageCount"]} phone/tablet images in edit {edit["id"]}. Not committed.')
        print(f'{manifest["chromebookManualImageCount"]} Chromebook images still require Play Console UI upload.')
    except Exception:
        try:
            api.edits().delete(packageName=PACKAGE, editId=edit['id']).execute()
            STATE_PATH.unlink(missing_ok=True)
        except Exception as cleanup_error:
            state.update(status='staging-failed-cleanup-required', cleanupError=str(cleanup_error))
            STATE_PATH.write_text(json.dumps(state, ensure_ascii=False, indent=2) + '\n')
        raise


def finish_edit(args, manifest, *, commit):
    if not STATE_PATH.is_file():
        raise RuntimeError(f'No staged edit found at {STATE_PATH}')
    state = json.loads(STATE_PATH.read_text())
    if state.get('packageName') != PACKAGE:
        raise RuntimeError('Staged edit package does not match this app.')
    api = load_api(args.service_account)
    if commit:
        if args.confirm_commit != f'{PACKAGE}:33':
            raise RuntimeError(f'Commit requires --confirm-commit {PACKAGE}:33')
        if state.get('status') != 'staged-uncommitted' or state.get('manifestSha256') != manifest_digest(manifest):
            raise RuntimeError('Staged edit is not complete or no longer matches the local manifest.')
        api.edits().commit(packageName=PACKAGE, editId=state['editId']).execute()
        state['status'] = 'committed'
        STATE_PATH.write_text(json.dumps(state, indent=2) + '\n')
        print(f'Committed store listing image edit {state["editId"]}.')
    else:
        if state.get('status') != 'staged-uncommitted':
            raise RuntimeError('Only a completed, uncommitted edit can be discarded by this command.')
        api.edits().delete(packageName=PACKAGE, editId=state['editId']).execute()
        STATE_PATH.unlink()
        print(f'Discarded uncommitted edit {state["editId"]}.')


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--service-account', default=os.environ.get('PLAY_CONSOLE_SERVICE_ACCOUNT_JSON'),
                        help='Path outside the repository to the Play Console service-account JSON; or set PLAY_CONSOLE_SERVICE_ACCOUNT_JSON.')
    commands = parser.add_subparsers(dest='command', required=True)
    commands.add_parser('audit', help='Validate local images and write an upload manifest; no API access.')
    commands.add_parser('stage', help='Upload phone/tablet images into a new uncommitted Play edit.')
    commit_parser = commands.add_parser('commit', help='Commit a completed staged edit to the Play listing.')
    commit_parser.add_argument('--confirm-commit', default='', help='Must exactly match PACKAGE:33 to commit.')
    commands.add_parser('discard', help='Delete the uncommitted staged edit without publishing it.')
    args = parser.parse_args()
    manifest = build_manifest()
    save_manifest(manifest)
    if args.command == 'audit':
        by_type = {}
        for entry in manifest['entries']:
            key = (entry['locale'], entry['imageType'])
            by_type[key] = by_type.get(key, 0) + 1
        print(f'Preflight passed: {manifest["apiImageCount"]} API images across {len(by_type)} locale/device groups.')
        print(f'{manifest["chromebookManualImageCount"]} Chromebook images are excluded from the API manifest.')
        print(f'Manifest: {MANIFEST_PATH.relative_to(ROOT)}')
        return
    if not args.service_account:
        raise RuntimeError('Set --service-account or PLAY_CONSOLE_SERVICE_ACCOUNT_JSON first.')
    if args.command == 'stage':
        stage(args, manifest)
    elif args.command == 'commit':
        finish_edit(args, manifest, commit=True)
    elif args.command == 'discard':
        finish_edit(args, manifest, commit=False)


if __name__ == '__main__':
    try:
        main()
    except Exception as error:
        raise SystemExit(str(error))
