#!/usr/bin/env python3
"""Check README/index sample lists; --write regenerates only their manga lists.

The published manifest supplies URLs, titles, authors and sizes. The app's
language registry supplies display names and the English-name sort key.
Legacy PDFs, release metadata, other links and existing HTML IDs are untouched.
"""
import argparse
import html
from html.parser import HTMLParser
import json
from pathlib import Path
import re
from urllib.parse import urlsplit


MANGA_LIST = re.compile(r'(?P<open><ul\b[^>]*>)(?:(?!</ul>).)*?tameshibu(?:(?!</ul>).)*?</ul>', re.S)
MD_LINK = re.compile(r'^- \[((?:\\.|[^\]\\])*)\]\(([^\s)]+)\) \((\d+\.\d+ MiB)\)\s*$')
SIZE = re.compile(r'\((\d+\.\d+ MiB)\)')


def is_manga_url(url):
    name = urlsplit(url).path.rsplit('/', 1)[-1].lower()
    return name.startswith('tameshibu') and name.endswith('.pdf')


def escape_markdown(text):
    return re.sub(r'([\\`*_\[\]<>~])', r'\\\1', text)


def unescape_markdown(text):
    return re.sub(r'\\([\\`*_\[\]<>~])', r'\1', text)


def catalog_items(root):
    manifest = json.loads((root / 'docs/sample-releases/sample-manga-v2/manifest.json').read_text())
    registry = next((root / 'app/src/main/java').rglob('SamplePdfLanguages.kt')).read_text()
    languages = {code: (native, english) for code, native, english in re.findall(
        r'SamplePdfLanguage\("([^"]+)", "([^"]+)", "([^"]+)"\)', registry)}
    entries = manifest['entries']
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


def target_pages(root):
    required = [root / 'README.md', root / 'README_ja.md']
    if any(not p.is_file() for p in required):
        raise ValueError('Both English and Japanese READMEs are required')
    pages = sorted(root.glob('README*.md')) + sorted((root / 'docs').glob('index*.html'))
    if not any(p.suffix == '.html' for p in pages):
        raise ValueError('No public index pages found')
    return pages


class SampleLinks(HTMLParser):
    def __init__(self, text):
        super().__init__(convert_charrefs=True)
        self.links = []
        self.anchor = None
        self.lists = []
        self.feed(text)

    def handle_starttag(self, tag, attrs):
        attrs = dict(attrs)
        if tag == 'li':
            self.lists.append(dict(text=[], links=[]))
        if tag == 'a':
            self.anchor = dict(url=attrs.get('href', ''), label='', size='')

    def handle_data(self, data):
        if self.anchor is not None:
            self.anchor['label'] += data
        if self.lists:
            self.lists[-1]['text'].append(data)

    def handle_endtag(self, tag):
        if tag == 'a' and self.anchor is not None:
            if is_manga_url(self.anchor['url']):
                self.links.append(self.anchor)
                if self.lists:
                    self.lists[-1]['links'].append(self.anchor)
            self.anchor = None
        if tag == 'li' and self.lists:
            context = self.lists.pop()
            sizes = SIZE.findall(''.join(context['text']))
            for link in context['links']:
                link['size'] = sizes[0] if len(sizes) == 1 else ''


def records(path, text):
    if path.suffix == '.html':
        return SampleLinks(text).links
    links = []
    for number, line in enumerate(text.splitlines(), 1):
        if not re.search(r'tameshibu[^\s)]+\.pdf', line, re.I):
            continue
        match = MD_LINK.fullmatch(line)
        if not match:
            raise ValueError(f'{path}:{number}: malformed manga Markdown link or size')
        links.append(dict(url=match[2], label=unescape_markdown(match[1]), size=match[3]))
    return links


def validate_text(path, text, items):
    actual = records(path, text)
    if len(actual) != len(items):
        raise ValueError(f'{path}: expected {len(items)} manga links, found {len(actual)} (including zero-link pages)')
    expected = {x['url']: x for x in items}
    urls = [x['url'] for x in actual]
    if len(set(urls)) != len(urls) or set(urls) != set(expected):
        raise ValueError(f'{path}: missing, duplicate or unexpected manga URLs')
    if urls != [x['url'] for x in items]:
        raise ValueError(f'{path}: sort by English language name, then ascending episode')
    for link in actual:
        item = expected[link['url']]
        if ' '.join(link['label'].split()) != ' '.join(item['label'].split()):
            raise ValueError(f"{path}: {item['id']}: title/language/author differs from manifest/registry")
        if link['size'] != item['size']:
            raise ValueError(f"{path}: {item['id']}: missing or incorrect MiB size")
    return len(actual)


def verify_public_lists(root):
    items = catalog_items(root)
    files = []
    for path in target_pages(root):
        count = validate_text(path, path.read_text(), items)
        files.append(dict(file=path.relative_to(root).as_posix(), manga_links=count))
    return dict(public_pages_checked=len(files), public_pdf_links=sum(x['manga_links'] for x in files),
                public_list_order='English language name, episode ascending', public_lists=files)


def markdown_list(items):
    return ''.join(f"- [{escape_markdown(x['label'])}]({x['url']}) ({x['size']})\n" for x in items)


def html_list(items, opening='<ul>'):
    return opening + '\n' + ''.join(
        f'                <li><a href="{html.escape(x["url"], quote=True)}">{html.escape(x["label"])}</a> ({x["size"]})</li>\n'
        for x in items) + '            </ul>'


def missing_section(filename, items):
    # New locales need an intentional localized introduction, never silent omission.
    if filename == 'index_ms.html':
        title = 'PDF manga contoh'
        intro = ('51 PDF dalam 14 bahasa dan 17 varian bahasa/wilayah, dengan tiga episod setiap varian. '
                 'Disusun mengikut nama bahasa dalam bahasa Inggeris, kemudian episod 1–3. '
                 'Edisi Sepanyol/Amerika Latin, Brazil/Portugal dan Taiwan/Hong Kong dibezakan. '
                 'Edisi Hong Kong menggunakan bahasa Cina tulisan standard, bukan Kantonis percakapan.')
        license_text = ('Bahagian karya yang haknya dimiliki oleh penerbit disediakan di bawah '
                        '<a href="https://creativecommons.org/licenses/by/4.0/">CC BY 4.0</a>. '
                        'Bahan pihak ketiga seperti fon kekal tertakluk pada lesen masing-masing. '
                        'Apabila menggunakan semula, nyatakan tajuk, pengarang, sumber asal dan lesen serta sebarang perubahan. '
                        'Teks, imej dan terjemahan dihasilkan menggunakan AI generatif.')
        source_label = 'Sumber asal'
    elif filename == 'index_zh_hk.html':
        title = '範例漫畫 PDF'
        intro = ('共51份PDF，涵蓋14種語言、17種語言及地區版本，每種3話。按英文語言名稱排序，同一語言按第1至3話排列。'
                 '西班牙／拉丁美洲、巴西／葡萄牙、台灣／香港分開標示。香港版採用標準書面中文，並非口語粵語版。')
        license_text = ('公開者擁有相關權利的部分以<a href="https://creativecommons.org/licenses/by/4.0/">CC BY 4.0</a>授權。'
                        '字型等第三方素材仍適用各自授權。重用時請標示作品名稱、作者、原作公開來源與授權，並註明修改。'
                        '文字、圖像與翻譯使用生成式AI製作。')
        source_label = '原作公開來源'
    else:
        raise ValueError(f'{filename}: missing manga list; add a localized introduction before regeneration')
    source = items[0]['source']
    return (f'    <section id="sample-pdfs">\n        <h2>{title}</h2>\n'
            f'        <div class="content-area" style="max-width: 800px; margin: 0 auto; text-align: left;">\n'
            f'            <p>{intro}</p>\n            {html_list(items)}\n'
            f'            <p>© 2026 こまいろ日和 / Komairo Hiyori</p>\n'
            f'            <p>{license_text}</p>\n'
            f'            <p><a href="{source}">{source_label}</a></p>\n'
            '        </div>\n    </section>\n\n')


def regenerate(root):
    items = catalog_items(root)
    changes = {}
    for path in target_pages(root):
        text = path.read_text()
        if path.suffix == '.md':
            lines = text.splitlines(keepends=True)
            positions = [n for n, line in enumerate(lines) if re.search(r'tameshibu[^\s)]+\.pdf', line, re.I)]
            if not positions or positions != list(range(positions[0], positions[-1] + 1)):
                raise ValueError(f'{path}: expected one contiguous manga Markdown list')
            updated = ''.join(lines[:positions[0]]) + markdown_list(items) + ''.join(lines[positions[-1] + 1:])
        else:
            matches = list(MANGA_LIST.finditer(text))
            if len(matches) == 1:
                match = matches[0]
                updated = text[:match.start()] + html_list(items, match['open']) + text[match.end():]
            elif not matches:
                section = missing_section(path.name, items)
                anchor = (re.search(r'(?m)^[ \t]*(?:<footer\b|</body>)', text)
                          or re.search(r'<footer\b|</body>', text))
                if not anchor:
                    raise ValueError(f'{path}: no insertion point')
                updated = text[:anchor.start()] + section + text[anchor.start():]
            else:
                raise ValueError(f'{path}: multiple manga lists')
        validate_text(path, updated, items)
        if updated != text:
            changes[path] = updated
    # Validate every generated page before making any changes.
    for path, text in changes.items():
        path.write_text(text)
    return [p.relative_to(root).as_posix() for p in changes]


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--write', action='store_true', help='Regenerate manga lists, preserving other content')
    args = parser.parse_args()
    root = Path(__file__).resolve().parent.parent
    changed = regenerate(root) if args.write else []
    print(json.dumps(dict(changed=changed, **verify_public_lists(root)), ensure_ascii=False, indent=2))


if __name__ == '__main__':
    main()
