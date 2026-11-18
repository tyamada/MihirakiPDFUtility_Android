#!/usr/bin/env python3
"""Validate Utility's migrated catalog, stable cache names and published evidence.

No PDF is required in the repository. --remote verifies all 51 downloads;
--download-dir optionally retains verified PDFs outside docs/ and testdata/.
"""
import argparse
from concurrent.futures import ThreadPoolExecutor
import csv
import hashlib
import json
from pathlib import Path
import re
import subprocess
import tempfile

from sample_public_catalog import verify_public_lists


def verify(root, remote=False, download_dir=None):
    evidence = root / 'docs/sample-releases/manga-pdf-20261103-v1'
    manifest = json.loads((evidence / 'pdf-manifest.json').read_text())
    migration = list(csv.DictReader((evidence / 'pdf-migration.csv').open(encoding='utf-8-sig')))
    public = json.loads((evidence / 'pdf-public-verification.json').read_text())
    checksums = dict(line.split('  ', 1)[::-1] for line in (evidence / 'SHA256SUMS').read_text().splitlines())
    previous = json.loads((root / 'docs/sample-releases/sample-manga-v2/manifest.json').read_text())
    old = {entry['sample_id']: entry for entry in previous['entries']}
    entries = manifest['entries']
    by_id = {entry['sample_id']: entry for entry in entries}
    assert manifest['status'] == 'published'
    assert manifest['repository'] == public['repository'] == 'tyamada/manga-prompt-kit'
    assert manifest['fixed_tag'] == public['tag'] == 'manga-pdf-20261103-v1'
    assert manifest['pdf_count'] == public['pdf_count'] == len(entries) == len(by_id) == 51
    assert set(by_id) == set(old)
    assert public['all_passed'] and public['asset_count'] == len(public['results']) == 55
    assert len(checksums) == 54
    assert {name for name in checksums if name.endswith(".pdf")} == {entry["filename"] for entry in entries}
    rows = {row['sample_id']: row for row in migration
            if row['source_repository'] == 'tyamada/MihirakiPDFUtility_Android'}
    assert len(rows) == sum(row['source_repository'] == 'tyamada/MihirakiPDFUtility_Android' for row in migration) == 51
    prefix = 'https://github.com/tyamada/manga-prompt-kit/releases/download/manga-pdf-20261103-v1/'
    for entry in entries:
        previous_entry = old[entry['sample_id']]
        assert all(entry[key] == previous_entry[key] for key in
                   ['title', 'bytes', 'sha256', 'pages', 'language_key', 'locale', 'episode'])
        row = rows[entry['sample_id']]
        assert row['old_filename'] == previous_entry['filename']
        assert row['old_url'] == previous_entry['fixed_download_url']
        assert row['new_filename'] == entry['filename']
        assert row['language_key'] == entry['language_key']
        assert row['new_download_url'] == entry['new_download_url'] == prefix + entry['filename']
        assert row['new_repository'] == manifest['repository']
        assert row['new_tag_candidate'] == manifest['fixed_tag']
        assert row['sha256'] == entry['sha256'] == checksums[entry['filename']]
        assert entry['license'] == 'CC-BY-4.0'
    assert {entry['language_key'] for entry in entries} == {
        'ja', 'en', 'ko', 'zh_cn', 'zh_tw', 'de', 'fr', 'es', 'pt', 'vi', 'fil', 'ne', 'id', 'it', 'es_419', 'pt_pt', 'zh_hk'}
    for language in {entry['language_key'] for entry in entries}:
        assert {entry['episode'] for entry in entries if entry['language_key'] == language} == {1, 2, 3}
    for field in ['sample_id', 'filename', 'new_download_url']:
        assert len({entry[field] for entry in entries}) == 51, field
    assert [entry['filename'] for entry in entries if entry['language_key'] == 'fil'] == [
        'tameshibu_episode1_3_tl.pdf', 'tameshibu_episode2_3_tl.pdf', 'tameshibu_episode3_2_tl.pdf']
    catalog = next((root / 'app/src/main/java').rglob('SamplePdfCatalog.kt')).read_text()
    assert re.search(r'private const val BASE_URL = "([^"]+)"', catalog)[1] == prefix
    blocks = re.findall(r'^        SamplePdfItem\(\n(.*?)\n        \)', catalog, re.M | re.S)
    assert len(blocks) == 51
    actual_ids = []
    for block in blocks:
        literals = dict((key, json.loads(value)) for key, value in re.findall(r'(\w+) = ("(?:[^"\\]|\\.)*")', block))
        entry = by_id[literals['id']]
        actual_ids.append(literals['id'])
        for key, source in [('title', 'title'), ('languageCode', 'language_key'), ('sha256', 'sha256'), ('locale', 'locale')]:
            assert literals[key] == entry[source], (entry['sample_id'], key)
        assert literals['fileName'] == old[entry['sample_id']]['filename'], 'Preserve local cache names'
        assert literals['url'].replace('${BASE_URL}', prefix) == entry['new_download_url']
        assert literals['fileSize'] == f"{entry['bytes']/1048576:.1f} MiB"
        assert int(re.search(r'bytes = (\d+)L', block)[1]) == entry['bytes']
        assert int(re.search(r'pageCount = (\d+)', block)[1]) == entry['pages'] == 8
        assert f'SamplePdfLanguages.displayName("{entry["language_key"]}")' in block
    assert len(set(actual_ids)) == 51
    for result in public['results']:
        assert result['anonymous'] and result['final_https'] and result['http_status'] == 200
        assert result['matches_local']
        filename = result['filename']
        assert result['url'] == prefix + filename
        if filename.endswith('.pdf'):
            entry = next(entry for entry in entries if entry['filename'] == filename)
            assert result['sha256'] == entry['sha256']
            assert result['bytes'] == entry['bytes'] and result['pages'] == 8
        else:
            data = (evidence / filename).read_bytes()
            assert len(data) == result['bytes'] and hashlib.sha256(data).hexdigest() == result['sha256']
    public_lists = verify_public_lists(root)
    links = public_lists['public_pdf_links']

    def check(entry):
        result = dict(name=entry['filename'], bytes=entry['bytes'], sha256=entry['sha256'])
        if remote:
            with tempfile.TemporaryDirectory(prefix='utility-pdf-migration-') as directory:
                target = (download_dir or Path(directory)) / entry['filename']
                target.parent.mkdir(parents=True, exist_ok=True)
                process = subprocess.run(['curl', '--fail', '--silent', '--show-error', '--location',
                    '--proto', '=https', '--proto-redir', '=https', '--max-redirs', '5', '--retry', '2',
                    '--connect-timeout', '30', '--max-time', '300', '--output', str(target),
                    '--write-out', '%{http_code}', entry['new_download_url']], capture_output=True, text=True)
                assert process.returncode == 0 and process.stdout == '200', (entry['filename'], process.stderr)
                with target.open('rb') as stream:
                    assert stream.read(5) == b'%PDF-'
                    stream.seek(0)
                    digest = hashlib.file_digest(stream, 'sha256').hexdigest()
                assert digest == entry['sha256'] and target.stat().st_size == entry['bytes'], entry['filename']
                result['remote_verified'] = True
        return result
    with ThreadPoolExecutor(max_workers=4) as pool:
        results = list(pool.map(check, entries))
    return dict(repository=manifest['repository'], tag=manifest['fixed_tag'], pdf_count=51,
                region_count=17, public_pdf_links=links,
                public_pages_checked=public_lists['public_pages_checked'],
                public_list_order=public_lists['public_list_order'], remote=remote, files=results)


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--remote', action='store_true')
    parser.add_argument('--download-dir', type=Path)
    parser.add_argument('--output', type=Path)
    args = parser.parse_args()
    if args.download_dir and not args.remote:
        parser.error('--download-dir requires --remote')
    root = Path(__file__).resolve().parent.parent
    if args.download_dir:
        destination = args.download_dir.resolve()
        if destination.is_relative_to(root / 'docs') or destination.is_relative_to(root / 'testdata'):
            parser.error('Keep downloaded PDFs outside docs/ and testdata/')
    result = verify(root, args.remote, args.download_dir)
    if args.output:
        args.output.write_text(json.dumps(result, indent=2) + '\n')
    print(json.dumps({key:value for key,value in result.items() if key != 'files'}, indent=2))
