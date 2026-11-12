import copy
import struct
import sys
import tempfile
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import play_console_screenshots as play


class PlayConsoleScreenshotTests(unittest.TestCase):
    def test_legacy_viewer_configuration_cannot_stage_utility_images(self):
        with self.assertRaisesRegex(RuntimeError, 'different application'):
            play.build_manifest()

    def test_png_preflight_accepts_rgb_and_rejects_alpha(self):
        signature = b'\x89PNG\r\n\x1a\n'

        def png_header(width, height, color_type):
            return signature + struct.pack('>I4sIIBB', 13, b'IHDR', width, height, 8, color_type)

        with tempfile.TemporaryDirectory() as folder:
            rgb = Path(folder) / 'rgb.png'
            rgb.write_bytes(png_header(1920, 1080, 2))
            self.assertEqual(play.png_info(rgb), (1920, 1080))
            alpha = Path(folder) / 'alpha.png'
            alpha.write_bytes(png_header(1920, 1080, 6))
            with self.assertRaisesRegex(ValueError, 'without alpha'):
                play.png_info(alpha)

    def test_manifest_digest_ignores_generation_timestamp(self):
        manifest = {'generatedAt': 'first', 'entries': [{'path': 'a.png', 'sha256': 'abc'}]}
        regenerated = copy.deepcopy(manifest)
        regenerated['generatedAt'] = 'later'
        self.assertEqual(play.manifest_digest(manifest), play.manifest_digest(regenerated))
        regenerated['entries'][0]['sha256'] = 'changed'
        self.assertNotEqual(play.manifest_digest(manifest), play.manifest_digest(regenerated))

    def test_missing_locale_drafts_cover_required_store_locales(self):
        drafts = play.missing_listing_content()
        required = {'en-US', 'ja-JP', 'ko-KR', 'de-DE', 'fr-FR', 'it-IT', 'zh-CN', 'zh-TW', 'zh-HK'}
        self.assertEqual(required, drafts.keys())
        self.assertIn("Utility", drafts["en-US"]["title"])
        for locale in required:
            self.assertLessEqual(len(drafts[locale]['title']), 30, locale)
            self.assertLessEqual(len(drafts[locale]['shortDescription']), 80, locale)
            self.assertLessEqual(len(drafts[locale]['fullDescription']), 4000, locale)
            self.assertTrue(drafts[locale]['fullDescription'].strip(), locale)


if __name__ == '__main__':
    unittest.main()
