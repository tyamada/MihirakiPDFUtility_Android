#!/usr/bin/env python3
"""Validate Utility's v2 catalog against all four published handoff files.

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


def verify(root, remote=False, download_dir=None):
    evidence = root / 'docs/sample-releases/sample-manga-v2'
    manifest = json.loads((evidence / 'manifest.json').read_text())
    handoff = list(csv.DictReader((evidence / 'android-handoff.csv').open(encoding='utf-8-sig')))
    public = json.loads((evidence / 'public-verification.json').read_text())
    checksums = dict(line.split('  ', 1)[::-1] for line in (evidence / 'SHA256SUMS').read_text().splitlines())
    assert manifest['repository'] == public['repository'] == 'tyamada/MihirakiPDFUtility_Android'
    assert manifest['fixed_tag'] == public['fixed_tag'] == 'sample-manga-v2'
    entries = manifest['entries']
    assert manifest['pdf_count'] == len(entries) == len(handoff) == len(checksums) == 51
    assert public['all_passed'] and public['pdf_passed'] == 51 and public['sha256sums_match']
    rows = {row['sample_id']: row for row in handoff}
    assert len(rows) == 51
    for entry in entries:
        assert all(str(value) == rows[entry['sample_id']][key] for key, value in entry.items())
        assert checksums[entry['filename']] == entry['sha256']
    assert sum(e['change_type'] == 'revised' for e in entries) == 33
    assert sum(e['change_type'] == 'unchanged' for e in entries) == 9
    assert sum(e['change_type'] == 'new_regional_variant' for e in entries) == 9
    catalog = next((root / 'app/src/main/java').rglob('SamplePdfCatalog.kt')).read_text()
    prefix = re.search(r'private const val BASE_URL = "([^"]+)"', catalog)[1]
    assert prefix == 'https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/'
    blocks = re.findall(r'^        SamplePdfItem\(\n(.*?)\n        \)', catalog, re.M | re.S)
    assert len(blocks) == 51
    by_id = {e['sample_id']: e for e in entries}
    for block in blocks:
        literals = dict((key, json.loads(value)) for key, value in re.findall(r'(\w+) = ("(?:[^"\\]|\\.)*")', block))
        entry = by_id[literals['id']]
        for key, source in [('title', 'title'), ('fileName', 'filename'), ('languageCode', 'language_key'), ('sha256', 'sha256'), ('locale', 'locale')]:
            assert literals[key] == entry[source], (entry['sample_id'], key)
        assert literals['url'].replace('${BASE_URL}', prefix) == entry['fixed_download_url']
        assert literals['fileSize'] == f"{entry['bytes']/1048576:.1f} MiB"
        assert int(re.search(r'bytes = (\d+)L', block)[1]) == entry['bytes']
        assert int(re.search(r'pageCount = (\d+)', block)[1]) == entry['pages']
        assert f'SamplePdfLanguages.displayName("{entry["language_key"]}")' in block
    for field in ['sample_id', 'filename', 'fixed_download_url']:
        assert len(set(e[field] for e in entries)) == 51, field
    assert len(set(e['language_key'] for e in entries)) == 17
    assert {e['sample_id'] for e in entries if e['replaces_sample_id']} == {e['replaces_sample_id'] for e in entries if e['replaces_sample_id']}
    public_checks = {check['filename']: check for check in public['checks'] if check['kind'] == 'pdf'}
    for entry in entries:
        check = public_checks[entry['filename']]
        assert check['passed'] and check['final_http_status'] == 200 and check['https_only_redirects_enforced']
        assert check['actual_sha256'] == entry['sha256'] and check['actual_bytes'] == entry['bytes']
    links = 0
    expected_urls = {e['fixed_download_url'] for e in entries}
    for page in list((root / 'docs').rglob('*.html')) + list(root.glob('README*.md')):
        urls = re.findall(r'https?://[^\s"<>\)]+tameshibu[^\s"<>\)]*\.pdf', page.read_text())
        if urls:
            assert len(urls) == 51 and set(urls) == expected_urls, page
        for url in urls:
            assert url in expected_urls, (page, url)
            links += 1

    def check(entry):
        result = dict(name=entry['filename'], bytes=entry['bytes'], sha256=entry['sha256'])
        if remote:
            with tempfile.TemporaryDirectory(prefix='utility-v2-') as directory:
                target = (download_dir or Path(directory)) / entry['filename']
                target.parent.mkdir(parents=True, exist_ok=True)
                process = subprocess.run(['curl', '--fail', '--silent', '--show-error', '--location',
                    '--proto', '=https', '--proto-redir', '=https', '--max-redirs', '5', '--retry', '2',
                    '--connect-timeout', '30', '--max-time', '300', '--output', str(target),
                    '--write-out', '%{http_code}', entry['fixed_download_url']], capture_output=True, text=True)
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
                region_count=17, public_pdf_links=links, remote=remote, files=results)


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
