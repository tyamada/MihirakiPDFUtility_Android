"""Regression checks for language-aware centralized sample links."""
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
            (root / name).write_text('# App\n\n' + ''.join(f'- [{x["label"]}]({x["url"]}) ({x["size"]})\n' for x in items) + '\nKeep https://example.org/legacy.pdf\n')
        (root / 'docs/index.html').write_text('<html lang="en"><body><h2 id="legacy-anchor">Samples</h2>' + '<ul>'+''.join(f'<li><a href="{x["url"]}">Sample</a></li>' for x in items)+'</ul>' +
                                            '<a href="https://example.org/legacy.pdf">Legacy</a></body></html>')
        old = root / 'docs/sample/legacy.pdf'
        old.parent.mkdir(); old.write_bytes(b'%PDF-legacy-fixture')
        return items


    def test_locales_and_region_anchors(self):
        expected = {'README.md':'en/', 'README_ja.md':'ja/', 'index.html':'en/',
                    'index_zh.html':'zh-Hans/', 'index_zh_tw.html':'zh-Hant/#zh_tw',
                    'support_zh_hk.html':'zh-Hant/#zh_hk', 'index_in.html':'id/',
                    'index_tl.html':'fil/', 'index_ms.html':'en/'}
        for name, tail in expected.items():
            self.assertEqual(catalog.hub_url(Path(name)),catalog.HUB+tail)

    def test_regeneration_removes_pdf_lists_and_preserves_unrelated_links(self):
        with tempfile.TemporaryDirectory() as directory:
            root=Path(directory);self.fixture(root)
            (root/'docs/support_ja.html').write_text('<html><body>Support<a href="https://example.org/video">Video</a></body></html>')
            (root/'docs/privacy.html').write_text('<html><body>Privacy</body></html>')
            before=(root/'docs/sample/legacy.pdf').read_bytes()
            catalog.regenerate(root)
            result=catalog.verify_public_lists(root)
            self.assertEqual(result['public_pdf_links'],0)
            self.assertEqual(result['public_pages_checked'],5)
            self.assertIn('legacy-anchor',(root/'docs/index.html').read_text())
            self.assertIn('https://example.org/legacy.pdf',(root/'docs/index.html').read_text())
            self.assertIn('https://example.org/video',(root/'docs/support_ja.html').read_text())
            self.assertEqual((root/'docs/sample/legacy.pdf').read_bytes(),before)
            self.assertEqual(catalog.regenerate(root),[])

    def test_malay_explicitly_links_to_english(self):
        p=Path('index_ms.html');text=catalog.guide(p)
        self.assertIn('bahasa Inggeris',text)
        catalog.validate_text(p,text)
        with self.assertRaisesRegex(ValueError,'English guidance'):
            catalog.validate_text(p,text.replace(catalog.LABELS['ms'],'Samples'))

    def test_missing_wrong_locale_and_direct_pdf_links_fail(self):
        p=Path('index_ja.html')
        for text in ['No guide',catalog.guide(Path('index.html')),
                     catalog.guide(p)+'<a href="https://example.org/testdata/tameshibu_old.pdf">Old</a>']:
            with self.assertRaises(ValueError):catalog.validate_text(p,text)
        with self.assertRaises(ValueError):
            catalog.validate_text(Path('privacy.html'),'<a href="../sample/tameshibu_old.pdf">Old</a>')

    def test_unknown_locales_and_missing_readme_fail(self):
        with self.assertRaises(ValueError):catalog.hub_url(Path('index_unknown.html'))
        with tempfile.TemporaryDirectory() as directory:
            root=Path(directory);self.fixture(root);(root/'README.md').unlink()
            with self.assertRaises(ValueError):catalog.verify_public_lists(root)

    def test_machine_manifest_hash_and_host_drift_still_fail(self):
        with tempfile.TemporaryDirectory() as directory:
            root=Path(directory);self.fixture(root)
            p=root/'docs/sample-releases/manga-pdf-20261103-v1/pdf-manifest.json';original=p.read_text()
            for field,value in [('sha256','b'*64),('new_download_url','https://example.org/tameshibu.pdf')]:
                data=json.loads(original);data['entries'][0][field]=value;p.write_text(json.dumps(data))
                with self.assertRaises(ValueError):catalog.catalog_items(root)

if __name__=='__main__':unittest.main()
