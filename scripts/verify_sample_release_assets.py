#!/usr/bin/env python3
"""Verify all catalog PDFs and, optionally, unauthenticated release downloads.

Run from any directory. --remote downloads every PDF and verifies SHA-256.
No credentials are used, and no remote state is changed.
"""
import argparse
from concurrent.futures import ThreadPoolExecutor
import hashlib
import json
from pathlib import Path
import re
import subprocess
import tempfile


def verify(root, remote=False):
    catalog = next((root / 'app/src/main/java').rglob('SamplePdfCatalog.kt')).read_text()
    names = re.findall(r'fileName = "([^"]+)"', catalog)
    prefix = re.search(r'private const val BASE_URL = "([^"]+)"', catalog)[1]
    assert re.fullmatch(r'https://github\.com/tyamada/MihirakiPDF(?:Viewer|Utility)_Android/releases/download/sample-manga-v1/', prefix), prefix
    assert len(names) == len(set(names)) == 42
    assert re.findall(r'url = "\$\{BASE_URL\}([^"]+)"', catalog) == names
    checksums = dict(line.split('  ', 1)[::-1] for line in
                     (root / 'docs/sample/SHA256SUMS').read_text().splitlines())
    assert set(names) == set(checksums)
    public_links = 0
    for page in list((root / 'docs').rglob('*.html')) + list(root.glob('README*.md')):
        for url in re.findall(r'https?://[^\s"<>\)]+tameshibu[^\s"<>\)]*\.pdf', page.read_text()):
            assert url.startswith(prefix) and url[len(prefix):] in checksums, (page, url)
            public_links += 1

    def check(name):
        original = root / 'docs/sample' / name
        with original.open('rb') as stream:
            digest = hashlib.file_digest(stream, 'sha256').hexdigest()
        assert digest == checksums[name], f'Local checksum mismatch: {name}'
        result = dict(name=name, bytes=original.stat().st_size, sha256=digest,
                      local_verified=True, url=prefix + name)
        if remote:
            with tempfile.TemporaryDirectory(prefix='sample-release-') as temporary:
                target = Path(temporary) / name
                process = subprocess.run([
                    'curl', '--fail', '--silent', '--show-error', '--location',
                    '--proto', '=https', '--proto-redir', '=https',
                    '--max-redirs', '5', '--retry', '2', '--connect-timeout', '30',
                    '--max-time', '300', '--output', str(target),
                    '--write-out', '%{http_code}', prefix + name,
                ], capture_output=True, text=True)
                assert process.returncode == 0 and process.stdout == '200', (
                    name, process.stdout, process.stderr)
                with target.open('rb') as stream:
                    assert stream.read(5) == b'%PDF-', f'Not a PDF: {name}'
                    stream.seek(0)
                    actual = hashlib.file_digest(stream, 'sha256').hexdigest()
                assert target.stat().st_size == result['bytes'] and actual == digest, name
                result['remote_verified'] = True
        return result

    with ThreadPoolExecutor(max_workers=4) as executor:
        rows = list(executor.map(check, names))
    return dict(repository=root.name, prefix=prefix, pdf_count=len(rows),
                pdf_bytes=sum(row['bytes'] for row in rows), public_pdf_links=public_links,
                remote=remote, files=rows)


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--remote', action='store_true')
    parser.add_argument('--output', type=Path)
    args = parser.parse_args()
    result = verify(Path(__file__).resolve().parent.parent, args.remote)
    if args.output:
        args.output.write_text(json.dumps(result, indent=2) + '\n')
    print(json.dumps({k: v for k, v in result.items() if k != 'files'}, indent=2))
