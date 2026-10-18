import copy
import struct
import sys
import tempfile
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import play_console_screenshots as play


class PlayConsoleScreenshotTests(unittest.TestCase):
    def test_manifest_separates_api_and_chromebook_images(self):
        manifest = play.build_manifest()
        groups = play.locale_groups(manifest)
        self.assertEqual(manifest['apiImageCount'], 294)
        self.assertEqual(manifest['chromebookManualImageCount'], 84)
        self.assertEqual(len(groups), 42)
        self.assertEqual({image_type for _, image_type in groups}, set(play.DEVICE_TYPES.values()))
        self.assertTrue(all(len(images) == 7 for images in groups.values()))
        self.assertEqual({language for language, _ in groups},
                         set(play.PLAY_LANGUAGE_BY_CAPTURE_LOCALE.values()))

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
        required = {'es-ES', 'fil', 'id', 'it-IT', 'ne-NP', 'vi'}
        self.assertTrue(required <= drafts.keys())
        for locale in required:
            self.assertLessEqual(len(drafts[locale]['title']), 30, locale)
            self.assertLessEqual(len(drafts[locale]['shortDescription']), 80, locale)
            self.assertLessEqual(len(drafts[locale]['fullDescription']), 4000, locale)
            self.assertTrue(drafts[locale]['fullDescription'].strip(), locale)


if __name__ == '__main__':
    unittest.main()
