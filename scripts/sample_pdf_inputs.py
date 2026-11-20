#!/usr/bin/env python3
"""Prepare only explicitly selected fixed-release manga PDFs in an external cache.

Normal readers are offline and fail before device work. Only --download performs
network I/O. This never writes into docs/ or testdata/ or deletes user files.
"""
import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import tempfile
from urllib.request import urlopen

ROOT = Path(__file__).resolve().parents[1]
MANIFEST = 'docs/sample-releases/manga-pdf-20261103-v1/pdf-manifest.json'
LEGACY = 'docs/sample-releases/sample-manga-v2/manifest.json'
PREFIX = 'https://github.com/tyamada/manga-prompt-kit/releases/download/manga-pdf-20261103-v1/'


def catalog(root=ROOT):
    data = json.loads((root / MANIFEST).read_text())
    old = {e['sample_id']: e['filename'] for e in json.loads((root / LEGACY).read_text())['entries']}
    if data.get('repository') != 'tyamada/manga-prompt-kit' or data.get('fixed_tag') != 'manga-pdf-20261103-v1':
        raise ValueError('Unexpected fixed sample release')
    entries = {}
    for entry in data['entries']:
        item = dict(entry)
        name = item['filename']
        local = old[item['sample_id']]
        if any(Path(n).name != n or '/' in n or '\\' in n or not n.startswith('tameshibu') or not n.endswith('.pdf') for n in [name, local]):
            raise ValueError('Unsafe sample filename')
        if item['new_download_url'] != PREFIX + name or not re.fullmatch(r'[0-9a-f]{64}', item['sha256']) or type(item['bytes']) is not int or item['bytes'] <= 0:
            raise ValueError('Invalid sample integrity metadata')
        if item['sample_id'] in entries:
            raise ValueError('Duplicate sample ID')
        item['local_filename'] = local
        entries[item['sample_id']] = item
    if len(entries) != data['pdf_count'] or set(entries) != set(old):
        raise ValueError('Incomplete fixed sample manifest')
    return entries


def input_directory(value=None, root=ROOT):
    value = value or os.environ.get('MIHIRAKI_SAMPLE_PDF_DIR')
    if not value:
        raise ValueError('Specify --input-dir (or MIHIRAKI_SAMPLE_PDF_DIR); prepare PDFs with sample_pdf_inputs.py --download')
    path = Path(value).expanduser().resolve()
    if path.is_relative_to(root.resolve()):
        raise ValueError('Sample PDF input/cache must be outside the repository')
    return path


def verify_file(path, entry):
    if not path.is_file() or path.is_symlink() or path.stat().st_size != entry['bytes']:
        raise ValueError('Missing or wrong-size sample PDF: ' + str(path))
    with path.open('rb') as stream:
        if stream.read(5) != b'%PDF-':
            raise ValueError('Invalid PDF header: ' + str(path))
        stream.seek(0)
        if hashlib.file_digest(stream, 'sha256').hexdigest() != entry['sha256']:
            raise ValueError('Sample SHA-256 mismatch: ' + str(path))
    return path


def resolve_samples(sample_ids, directory=None, root=ROOT):
    folder = input_directory(directory, root)
    entries = catalog(root)
    result = {}
    for sample_id in dict.fromkeys(sample_ids):
        if sample_id not in entries:
            raise ValueError('Unknown sample ID: ' + sample_id)
        e = entries[sample_id]
        local = folder / e['local_filename']
        # Existing fil cache names and fixed-release tl asset names are aliases.
        path = local if local.exists() else folder / e['filename']
        result[sample_id] = verify_file(path, e)
    return result


def language_samples(languages, directory=None, root=ROOT, episode=1):
    aliases = {'tl': 'fil', 'in': 'id', 'zh-CN': 'zh_cn', 'zh-TW': 'zh_tw', 'zh-HK': 'zh_hk'}
    ids = {lang: f'ep{episode}_{aliases.get(lang, lang)}' for lang in languages}
    paths = resolve_samples(ids.values(), directory, root)
    return {lang: paths[sample_id] for lang, sample_id in ids.items()}


def prepare(sample_ids, directory, root=ROOT, download=False, opener=urlopen):
    folder = input_directory(directory, root)
    entries = catalog(root)
    selected = list(dict.fromkeys(sample_ids))
    if not selected or any(i not in entries for i in selected):
        raise ValueError('Select known --sample-id values explicitly')
    for sample_id in selected:
        e = entries[sample_id]
        target = folder / e['local_filename']
        existing = target if target.exists() else folder / e['filename']
        if existing.exists():
            verify_file(existing, e)  # Preserve, never replace corrupt or unknown input.
            continue
        if not download:
            raise ValueError('Missing sample; explicit --download required: ' + sample_id)
        folder.mkdir(parents=True, exist_ok=True)
        with tempfile.NamedTemporaryFile(dir=folder, prefix='.sample-', suffix='.part', delete=False) as stream:
            temp = Path(stream.name)
            try:
                with opener(e['new_download_url'], timeout=300) as source:
                    # Fixed HTTPS release assets may redirect to GitHub's asset CDN.
                    if not source.geturl().startswith('https://'):
                        raise ValueError('Sample download redirected away from HTTPS')
                    count = 0
                    while chunk := source.read(1024 * 1024):
                        count += len(chunk)
                        if count > e['bytes']:
                            raise ValueError('Sample download exceeds expected size')
                        stream.write(chunk)
                stream.flush()
                verify_file(temp, e)
                if target.exists():
                    raise ValueError('Destination appeared during download; not overwriting')
                os.link(temp, target)  # Atomic no-clobber publication; preserves concurrent input.
                temp.unlink()
            finally:
                if temp.exists():
                    temp.unlink()  # Only this invocation's incomplete temporary file.
    return resolve_samples(selected, folder, root)


def stage_device_file(serial, source, destination, runner=None):
    """Never overwrite a device file; reuse only an exact SHA-256 match.

    Caller must preflight every local input before device work. The temporary
    copy is unique to this invocation; only that temporary path is removed.
    """
    import shlex
    import subprocess
    import uuid
    runner = runner or subprocess.run
    source = Path(source)
    digest = hashlib.sha256(source.read_bytes()).hexdigest()
    def adb(*args, check=True):
        result = runner(['adb', '-s', serial, *args], capture_output=True, text=True)
        if check and result.returncode:
            raise RuntimeError('Device input staging failed: ' + result.stdout + result.stderr)
        return result
    exists = adb('shell', 'test', '-e', destination, check=False)
    if exists.returncode == 0:
        actual = adb('shell', 'sha256sum', destination).stdout.split()[0]
        if actual != digest:
            raise RuntimeError('Existing device input differs; preserved without overwrite: ' + destination)
        return
    if exists.returncode != 1:
        raise RuntimeError('Cannot check existing device input; refusing to overwrite')
    folder = str(Path(destination).parent)
    staged = folder + '/.utility-input-' + uuid.uuid4().hex + '.part'
    adb('shell', 'mkdir', '-p', folder)
    try:
        adb('push', str(source), staged)
        if adb('shell', 'sha256sum', staged).stdout.split()[0] != digest:
            raise RuntimeError('Staged device input SHA-256 mismatch')
        # POSIX noclobber protects a file created after the initial existence check.
        adb('shell', 'set -C; cat ' + shlex.quote(staged) + ' > ' + shlex.quote(destination))
        if adb('shell', 'sha256sum', destination).stdout.split()[0] != digest:
            raise RuntimeError('Published device input SHA-256 mismatch')
    finally:
        adb('shell', 'rm', '-f', staged, check=False)


def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument('--input-dir', required=True, type=Path)
    p.add_argument('--sample-id', action='append', required=True)
    p.add_argument('--download', action='store_true', help='Explicitly fetch missing selected PDFs; default is offline validation')
    a = p.parse_args()
    result = prepare(a.sample_id, a.input_dir, download=a.download)
    print(json.dumps({k: str(v) for k, v in result.items()}, indent=2))


if __name__ == '__main__':
    main()
