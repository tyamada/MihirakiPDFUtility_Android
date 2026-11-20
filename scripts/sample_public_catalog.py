#!/usr/bin/env python3
"""Validate centralized sample guides; --write replaces public manga lists.

The machine catalog remains validated against its fixed-release evidence.
Public README/HTML pages link to language-aware guides rather than PDF lists.
Legacy PDFs, release metadata, unrelated links and existing HTML IDs are retained.
"""
import argparse
import html
from html.parser import HTMLParser
import json
from pathlib import Path
import re
from urllib.parse import urlsplit


MANGA_LIST = re.compile(r'(?P<open><ul\b[^>]*>)(?:(?!</ul>).)*?tameshibu(?:(?!</ul>).)*?</ul>', re.S)
SIZE = re.compile(r'\((\d+\.\d+ MiB)\)')


def is_manga_url(url):
    name = urlsplit(url).path.rsplit('/', 1)[-1].lower()
    return name.startswith('tameshibu') and name.endswith('.pdf')


def escape_markdown(text):
    return re.sub(r'([\\`*_\[\]<>~])', r'\\\1', text)


def unescape_markdown(text):
    return re.sub(r'\\([\\`*_\[\]<>~])', r'\1', text)


def catalog_items(root, web=False):
    manifest = json.loads((root / 'docs/sample-releases/sample-manga-v2/manifest.json').read_text())
    registry = next((root / 'app/src/main/java').rglob('SamplePdfLanguages.kt')).read_text()
    languages = {code: (native, english) for code, native, english in re.findall(
        r'SamplePdfLanguage\("([^"]+)", "([^"]+)", "([^"]+)"\)', registry)}
    published = json.loads((root / 'docs/sample-releases/manga-pdf-20261103-v1/pdf-manifest.json').read_text())
    if published['repository'] != 'tyamada/manga-prompt-kit' or published['fixed_tag'] != 'manga-pdf-20261103-v1':
        raise ValueError('Unexpected shared sample release')
    by_id = {entry['sample_id']: entry for entry in published['entries']}
    for old in manifest['entries']:
        new = by_id.get(old['sample_id'])
        if new is None:
            raise ValueError('Published migration is missing a sample ID')
        for key in ('bytes', 'sha256', 'title', 'episode', 'language_key', 'pages', 'locale'):
            if new[key] != old[key]:
                raise ValueError(f'Shared release differs from legacy: {old["sample_id"]}: {key}')
        if new['filename'] != old.get('production_filename', old['filename']):
            raise ValueError('Shared filename differs from recorded production filename')
        expected = 'https://github.com/tyamada/manga-prompt-kit/releases/download/manga-pdf-20261103-v1/' + new['filename']
        if new['new_download_url'] != expected:
            raise ValueError('Unexpected shared download URL')
    if len(by_id) != published['pdf_count'] or set(by_id) != {e['sample_id'] for e in manifest['entries']}:
        raise ValueError('Published migration has missing or duplicate sample IDs')
    entries = [dict(entry, fixed_download_url=by_id[entry['sample_id']]['new_download_url'])
               for entry in manifest['entries']]
    if len(entries) != manifest['pdf_count'] or len({e['sample_id'] for e in entries}) != len(entries):
        raise ValueError('Manifest has missing or duplicate sample IDs')
    ordered = sorted(entries, key=lambda e: (languages[e['language_key']][1].casefold(), int(e['episode'])))
    items = []
    for entry in ordered:
        native, english = languages[entry['language_key']]
        language = native if native == english else f'{native} ({english})'
        items.append(dict(id=entry['sample_id'], url=entry['fixed_download_url'],
                          label=f"{entry['title']} — {language} — {entry['author']}", source=entry['original_source_url'],
                          size=f"{entry['bytes'] / 1048576:.1f} MiB"))
    if len({x['url'] for x in items}) != len(items):
        raise ValueError('Manifest has duplicate URLs')
    return items


HUB = "https://tyamada.github.io/manga-prompt-kit/samples/"
MAINTENANCE = "https://github.com/tyamada/manga-prompt-kit/blob/main/docs/sample-download-maintenance.md"
LABELS = {
    "en": "Sample PDF downloads", "ja": "サンプルPDFのダウンロード", "ko": "샘플 PDF 다운로드",
    "zh-Hans": "示例 PDF 下载", "zh-Hant": "範例 PDF 下載", "de": "Beispiel-PDFs herunterladen",
    "fr": "Télécharger les PDF d’exemple", "es": "Descargar PDF de muestra", "pt": "Baixar PDFs de exemplo",
    "vi": "Tải PDF mẫu", "fil": "I-download ang mga sample na PDF", "ne": "नमुना PDF डाउनलोड",
    "id": "Unduh PDF contoh", "it": "Scarica PDF di esempio", "ms": "Panduan muat turun dalam bahasa Inggeris",
}


def page_locale(path):
    if path.name == "README.md": return "en", ""
    if path.name == "README_ja.md": return "ja", ""
    stem = path.stem
    code = stem.split("_", 1)[1] if "_" in stem else "en"
    aliases = {"zh": "zh-Hans", "zh_cn": "zh-Hans", "zh-CN": "zh-Hans", "zh_hans": "zh-Hans",
               "zh_tw": "zh-Hant", "zh-TW": "zh-Hant", "zh_hant": "zh-Hant", "zh_hk": "zh-Hant",
               "zh-HK": "zh-Hant", "tl": "fil", "in": "id"}
    anchor = {"zh_tw": "zh_tw", "zh-TW": "zh_tw", "zh_hk": "zh_hk", "zh-HK": "zh_hk"}.get(code, "")
    language = aliases.get(code, code)
    if language not in LABELS: raise ValueError(f"{path}: unsupported public page locale {code}")
    return language, anchor


def hub_url(path):
    language, anchor = page_locale(path)
    return HUB + ("en" if language == "ms" else language) + "/" + ("#" + anchor if anchor else "")


def guide(path):
    language, _ = page_locale(path)
    url, label = hub_url(path), LABELS[language]
    if path.suffix == ".md": return f"[{label}]({url})"
    return f'<p class="sample-hub"><a href="{url}">{label}</a></p>'


def target_pages(root):
    required = [root / "README.md", root / "README_ja.md"]
    if any(not p.is_file() for p in required): raise ValueError("Both English and Japanese READMEs are required")
    return sorted(root.glob("README*.md")) + sorted((root / "docs").glob("*.html"))


def page_links(path, text):
    if path.suffix == ".md": return re.findall(r"\[[^\n]*?\]\(([^\s)]+)\)", text)
    class Links(HTMLParser):
        def __init__(self): super().__init__(); self.links = []
        def handle_starttag(self, tag, attrs):
            if tag == "a": self.links.append(dict(attrs).get("href", ""))
    parser = Links(); parser.feed(text); return parser.links


def validate_text(path, text, items=None):
    links = page_links(path, text)
    if any(is_manga_url(url) for url in links): raise ValueError(f"{path}: direct manga PDF link remains")
    needs_guide = path.suffix == ".md" or path.stem.startswith(("index", "support"))
    if needs_guide and hub_url(path) not in links: raise ValueError(f"{path}: missing localized sample hub link")
    if needs_guide and page_locale(path)[0] == "ms" and LABELS["ms"] not in text:
        raise ValueError(f"{path}: Malay page must explicitly identify English guidance")
    return sum(url.startswith(HUB) for url in links)


def verify_public_lists(root):
    # Machine catalog URLs remain validated independently; public pages contain no PDF lists.
    catalog_items(root)
    files = []
    for path in target_pages(root):
        count = validate_text(path, path.read_text())
        files.append(dict(file=path.relative_to(root).as_posix(), manga_links=0, hub_links=count))
    return dict(public_pages_checked=len(files), public_pdf_links=0,
                public_list_order="centralized sample hub; no replicated PDF lists", public_lists=files)


def regenerate(root):
    catalog_items(root)
    changes = {}
    for path in target_pages(root):
        text = path.read_text()
        if path.suffix == ".md":
            lines = text.splitlines(keepends=True); output = []; inserted = False
            for line in lines:
                if any(is_manga_url(url) for url in page_links(path, line)):
                    if not inserted: output.append(guide(path) + "\n"); inserted = True
                else: output.append(line)
            updated = "".join(output)
            updated = re.sub(r"Sample list maintenance:.*", f"Sample download maintenance: {MAINTENANCE}", updated)
            updated = re.sub(r"サンプル一覧の保守：.*", f"サンプル案内の更新手順：{MAINTENANCE}", updated)
        else:
            updated = MANGA_LIST.sub(lambda _: guide(path), text)
            # Other individual manga buttons outside lists must point to the same localized hub.
            updated = re.sub(r'(<a\b[^>]*href=["\'])([^"\']+)(["\'])',
                lambda m: m[1] + (hub_url(path) if is_manga_url(html.unescape(m[2])) else m[2]) + m[3], updated)
        # Replace shared Release landing links in user-facing download guidance too.
        updated = updated.replace("https://github.com/tyamada/manga-prompt-kit/releases/tag/manga-pdf-20261103-v1", hub_url(path))
        if path.suffix == ".html" and path.stem.startswith(("index", "support")) and hub_url(path) not in page_links(path, updated):
            match = re.search(r"<footer\b|</body>", updated)
            if not match: raise ValueError(f"{path}: no insertion point for sample guide")
            updated = updated[:match.start()] + guide(path) + "\n" + updated[match.start():]
        validate_text(path, updated)
        if updated != text: changes[path] = updated
    for path, text in changes.items(): path.write_text(text)
    return [p.relative_to(root).as_posix() for p in changes]


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--write", action="store_true", help="Replace public manga lists with localized sample hub links")
    args = parser.parse_args(); root = Path(__file__).resolve().parent.parent
    changed = regenerate(root) if args.write else []
    print(json.dumps(dict(changed=changed, **verify_public_lists(root)), ensure_ascii=False, indent=2))


if __name__ == "__main__": main()
