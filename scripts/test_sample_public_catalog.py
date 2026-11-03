#!/usr/bin/env python3
"""Regression checks for missing, inconsistent or misordered public sample lists."""
import json
from pathlib import Path
import tempfile
import unittest
import sample_public_catalog as catalog


class PublicSampleCatalogTests(unittest.TestCase):
    def fixture(self, root):
        evidence = root / 'docs/sample-releases/sample-manga-v2'
        evidence.mkdir(parents=True)
        registry = root / 'app/src/main/java/example/SamplePdfLanguages.kt'
        registry.parent.mkdir(parents=True)
        registry.write_text('''SamplePdfLanguage("en", "English", "English")
SamplePdfLanguage("zh_hk", "繁體中文（香港書面語）", "Chinese (Traditional, Hong Kong written standard)")''')
        entries = []
        for code in ['en', 'zh_hk']:
            for episode in range(1, 4):
                entries.append(dict(sample_id=f'ep{episode}_{code}', language_key=code, episode=episode,
                                    title=f'Cover {episode} [zh-Hant-HK] & <text>' if code == 'zh_hk' else f'Episode {episode}',
                                    author='こまいろ日和 / Komairo Hiyori', bytes=1048576 * episode,
                                    original_source_url='https://example.org/original',
                                    fixed_download_url=f'https://github.com/example/App/releases/download/sample-manga-v2/tameshibu_episode{episode}_{code}.pdf'))
        (evidence / 'manifest.json').write_text(json.dumps(dict(pdf_count=6, entries=entries)))
        items = catalog.catalog_items(root)
        for name in ['README.md', 'README_ja.md']:
            (root / name).write_text('# App\n\n' + catalog.markdown_list(items) + '\nKeep https://example.org/legacy.pdf\n')
        (root / 'docs/index.html').write_text('<html lang="en"><body><h2 id="legacy-anchor">Samples</h2>' + catalog.html_list(items) +
                                            '<a href="https://example.org/legacy.pdf">Legacy</a></body></html>')
        old = root / 'docs/sample/legacy.pdf'
        old.parent.mkdir(); old.write_bytes(b'%PDF-legacy-fixture')
        return items

    def assert_invalid(self, path, text, items, message):
        with self.assertRaisesRegex(ValueError, message):
            catalog.validate_text(path, text, items)

    def test_zero_links_and_new_locales_are_never_skipped(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory); self.fixture(root)
            (root / 'docs/index_new.html').write_text('<html lang="en"><body>No samples</body></html>')
            with self.assertRaisesRegex(ValueError, 'found 0'):
                catalog.verify_public_lists(root)

    def test_omission_and_same_count_duplicate_are_rejected(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory); items = self.fixture(root)
            for changed in [items[:-1], [items[0], items[0]] + items[2:]]:
                self.assert_invalid(root / 'README.md', catalog.markdown_list(changed), items, 'manga links|duplicate')

    def test_language_and_episode_order_are_both_checked(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory); items = self.fixture(root)
            for changed in [items[3:] + items[:3], [items[1], items[0]] + items[2:]]:
                self.assert_invalid(root / 'docs/index.html', catalog.html_list(changed), items, 'sort by English')

    def test_old_release_other_app_and_wrong_filipino_filename_are_rejected(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory); items = self.fixture(root); text = catalog.markdown_list(items)
            url = items[0]['url']
            for wrong in [url.replace('sample-manga-v2', 'sample-manga-v1'), url.replace('/example/App/', '/example/Other/'),
                          url.replace('_zh_hk.pdf', '_fil.pdf')]:
                self.assert_invalid(root / 'README.md', text.replace(url, wrong), items, 'unexpected manga URLs')

    def test_title_language_author_and_size_are_checked(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory); items = self.fixture(root); text = catalog.html_list(items)
            for old, new in [('Cover 1', 'Wrong title'), ('Hong Kong written standard', 'Cantonese'),
                             ('Komairo Hiyori', 'Another author'), ('(1.0 MiB)', '(8.0 MiB)'),
                             ('(1.0 MiB)', '(1.0 MB)')]:
                self.assert_invalid(root / 'docs/index.html', text.replace(old, new, 1), items, 'differs|incorrect MiB')

    def test_markdown_and_html_render_the_same_literal_regional_brackets(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory); items = self.fixture(root)
            md = catalog.markdown_list(items); html = catalog.html_list(items)
            self.assertIn(r'\[zh-Hant-HK\]', md)
            self.assertIn('[zh-Hant-HK]', html)
            self.assertEqual([r['label'] for r in catalog.records(root / 'README.md', md)],
                             [r['label'] for r in catalog.records(root / 'docs/index.html', html)])
            self.assert_invalid(root / 'README.md', md.replace(r'\[zh-Hant-HK\]', '(zh-Hant-HK)'), items, 'differs')

    def test_missing_html_list_item_size_is_rejected(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory); items = self.fixture(root)
            self.assert_invalid(root / 'docs/index.html', catalog.html_list(items).replace('(1.0 MiB)', '', 1), items, 'incorrect MiB')

    def test_regeneration_is_idempotent_and_preserves_legacy_content(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory); items = self.fixture(root)
            before = (root / 'docs/sample-releases/sample-manga-v2/manifest.json').read_bytes()
            (root / 'README.md').write_text('# App\n' + catalog.markdown_list(list(reversed(items))) + 'Keep https://example.org/legacy.pdf\n')
            (root / 'docs/index_ms.html').write_text('<html lang="ms"><body><footer id="old-footer">Old</footer></body></html>')
            (root / 'docs/index_zh_hk.html').write_text('<html lang="zh-HK"><body></body></html>')
            self.assertEqual(set(catalog.regenerate(root)), {'README.md', 'docs/index_ms.html', 'docs/index_zh_hk.html'})
            self.assertEqual(catalog.regenerate(root), [])
            self.assertEqual(catalog.verify_public_lists(root)['public_pdf_links'], 30)
            self.assertIn('id="legacy-anchor"', (root / 'docs/index.html').read_text())
            self.assertIn('https://example.org/legacy.pdf', (root / 'README.md').read_text())
            self.assertIn('id="old-footer"', (root / 'docs/index_ms.html').read_text())
            self.assertIn('CC BY 4.0', (root / 'docs/index_zh_hk.html').read_text())
            self.assertEqual((root / 'docs/sample/legacy.pdf').read_bytes(), b'%PDF-legacy-fixture')
            self.assertEqual((root / 'docs/sample-releases/sample-manga-v2/manifest.json').read_bytes(), before)


if __name__ == '__main__':
    unittest.main()
