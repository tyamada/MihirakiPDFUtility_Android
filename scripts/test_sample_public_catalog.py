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
        for entry in entries:
            entry.update(filename=entry['fixed_download_url'].rsplit('/', 1)[-1], sha256='a' * 64,
                         pages=8, locale=entry['language_key'])
        (evidence / 'manifest.json').write_text(json.dumps(dict(pdf_count=6, entries=entries)))
        published = root / 'docs/sample-releases/manga-pdf-20261103-v1'
        published.mkdir()
        (published / 'pdf-manifest.json').write_text(json.dumps(dict(
            repository='tyamada/manga-prompt-kit', fixed_tag='manga-pdf-20261103-v1', pdf_count=len(entries),
            entries=[dict(entry, new_download_url='https://github.com/tyamada/manga-prompt-kit/releases/download/manga-pdf-20261103-v1/' + entry['filename']) for entry in entries])))
        items = catalog.catalog_items(root)
        for name in ['README.md', 'README_ja.md']:
            (root / name).write_text('# App\n\n' + catalog.markdown_list(items) + '\nKeep https://example.org/legacy.pdf\n')
        (root / 'docs/index.html').write_text('<html lang="en"><body><h2 id="legacy-anchor">Samples</h2>' + catalog.html_list(items) +
                                            '<a href="https://example.org/legacy.pdf">Legacy</a></body></html>')
        old = root / 'docs/sample/legacy.pdf'
        old.parent.mkdir(); old.write_bytes(b'%PDF-legacy-fixture')
        return items

    def shared_fixture(self, root):
        self.fixture(root)
        return root / 'docs/sample-releases/manga-pdf-20261103-v1/pdf-manifest.json'

    def test_shared_html_and_legacy_readme_are_both_strictly_verified(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory); self.shared_fixture(root)
            items = catalog.catalog_items(root, web=True)
            page = root / 'docs/index.html'; page.write_text(catalog.html_list(items))
            self.assertEqual(catalog.verify_public_lists(root)['public_pdf_links'], 18)
            self.assertEqual(catalog.regenerate(root), [])
            for changed in [items[:-1], [items[0], items[0]] + items[2:], list(reversed(items))]:
                self.assert_invalid(page, catalog.html_list(changed), items, 'manga links|duplicate|sort by English')

    def test_shared_manifest_hash_and_download_host_cannot_drift(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory); path = self.shared_fixture(root); original = path.read_text()
            for field, value in [('sha256', 'b' * 64), ('new_download_url', 'https://example.org/tameshibu.pdf')]:
                data = json.loads(original); data['entries'][0][field] = value; path.write_text(json.dumps(data))
                with self.assertRaises(ValueError): catalog.catalog_items(root, web=True)

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
            for wrong in [url.replace('manga-pdf-20261103-v1', 'sample-manga-v2'), url.replace('/tyamada/manga-prompt-kit/', '/example/Other/'),
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
