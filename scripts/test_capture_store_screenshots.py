"""Safety/QA regression checks; no emulator or adb is used."""
from pathlib import Path
import sys
import json
import tempfile
import unittest
from unittest.mock import patch

from PIL import Image, ImageDraw
import capture_store_screenshots as capture


class CaptureSafetyTest(unittest.TestCase):
    def test_missing_source_is_fatal_before_build_or_device_operations(self):
        original = Path.is_file
        def is_file(path):
            return False if path.name == 'tameshibu_episode1_2_ko.pdf' else original(path)
        with patch.object(sys, 'argv', ['capture', '--device', 'Smartphone_6.3', '--language', 'ko']), \
                patch.object(Path, 'is_file', is_file), patch.object(capture, 'run') as run, \
                patch.object(capture.subprocess, 'run') as subprocess_run:
            with self.assertRaisesRegex(RuntimeError, 'FATAL: source PDFs missing.*ko.pdf'):
                capture.main()
            run.assert_not_called()
            subprocess_run.assert_not_called()

    def test_unselected_missing_pdf_does_not_block_selected_languages(self):
        original = Path.is_file
        def is_file(path):
            return path.name == 'tameshibu_episode1_2_fr.pdf' if path.suffix == '.pdf' else original(path)
        with patch.object(sys, 'argv', ['capture', '--device', 'Smartphone_6.3', '--language', 'fr']), \
                patch.object(Path, 'is_file', is_file), \
                patch.object(Path, 'read_text', side_effect=RuntimeError('reached AVD preflight')), \
                patch.object(capture, 'run') as run:
            with self.assertRaisesRegex(RuntimeError, 'reached AVD preflight'):
                capture.main()
            run.assert_not_called()

    def test_resume_rejects_stale_corrupt_or_wrong_scene_outputs(self):
        with tempfile.TemporaryDirectory() as folder:
            path = Path(folder) / 'screen.png'
            record = path.with_suffix('.json')
            image = Image.new('RGB', (1080, 1920), 'white')
            ImageDraw.Draw(image).rectangle((40, 100, 360, 500), fill='black')
            image.save(path)
            expected = dict(fingerprint='current', pdfSha256='source', avd='Smartphone_6.3',
                            language='ja', scene='01_reader', portrait=True)
            record.write_text(json.dumps(dict(expected, imageSha256=capture.sha(path))))
            self.assertTrue(capture.can_resume(path, record, expected))
            self.assertFalse(capture.can_resume(path, record, dict(expected, fingerprint='new APK')))
            self.assertTrue(capture.can_resume(path, record, dict(expected, fingerprint='compatible transport fix'), ('current',)))
            self.assertFalse(capture.can_resume(path, record, dict(expected, scene='04_search')))
            path.write_bytes(b'broken PNG')
            self.assertFalse(capture.can_resume(path, record, expected))
            record.write_text('{invalid JSON')
            self.assertFalse(capture.can_resume(path, record, expected))

    def test_profiles_and_unsupported_scene(self):
        self.assertEqual(378, sum(len(capture.selected_scenes(d)) for d in capture.DEVICES) * len(capture.LANGUAGES))
        with self.assertRaises(ValueError):
            capture.selected_scenes('Chromebook_FHD', ['01_reader'])
        self.assertTrue(all(capture.orientation('Chromebook_FHD', s) == 'landscape' for s in capture.selected_scenes('Chromebook_FHD')))

    def test_restore_existing_overrides_after_failure(self):
        device = capture.Device('Tablet_7')
        answers = ['Physical size: 720x1280\nOverride size: 900x1600', 'Physical density: 240\nOverride density: 300', '1', '2', 'lock 2']
        with patch.object(device, 'text', side_effect=answers), patch.object(device, 'adb') as adb:
            device.save_display()
            try:
                raise RuntimeError('capture failed')
            except RuntimeError:
                device.restore_display()
            adb.assert_any_call('shell', 'wm', 'size', '900x1600')
            adb.assert_any_call('shell', 'wm', 'density', '300')
            adb.assert_any_call('shell', 'wm', 'user-rotation', 'lock', '2')

    def test_restore_without_overrides(self):
        device = capture.Device('Tablet_7')
        with patch.object(device, 'text', side_effect=['Physical size: 720x1280', 'Physical density: 240', '1', '0', 'free']), patch.object(device, 'adb') as adb:
            device.save_display()
            device.restore_display()
            adb.assert_any_call('shell', 'wm', 'size', 'reset')
            adb.assert_any_call('shell', 'wm', 'density', 'reset')
            adb.assert_any_call('shell', 'wm', 'user-rotation', 'free')

    def test_resume_rejects_unverified_outline_and_small_images(self):
        with tempfile.TemporaryDirectory() as folder:
            path = Path(folder) / 'outline.png'
            record = path.with_suffix('.json')
            image = Image.new('RGB', (1080, 1920), 'white')
            ImageDraw.Draw(image).rectangle((40, 100, 800, 1500), fill='black')
            image.save(path)
            expected = dict(fingerprint='current', avd='Smartphone_6.3', scene='07_outline', portrait=True)
            info = dict(expected, imageSha256=capture.sha(path), outlineVerified=False)
            record.write_text(json.dumps(info))
            self.assertFalse(capture.can_resume(path, record, expected))
            info['outlineVerified'] = True
            record.write_text(json.dumps(info))
            self.assertTrue(capture.can_resume(path, record, expected))
            Image.new('RGB', (720, 1280), 'white').save(path)
            with self.assertRaisesRegex(RuntimeError, 'dimensions'):
                capture.validate_image(path, 'portrait')

    def test_blank_or_wrong_orientation_images_are_rejected(self):
        with tempfile.TemporaryDirectory() as folder:
            path = Path(folder) / 'screen.png'
            for color in ['white', 'black']:
                Image.new('RGB', (1080, 1920), color).save(path)
                with self.assertRaisesRegex(RuntimeError, 'Blank/black/white'):
                    capture.validate_image(path, 'portrait')
            image = Image.new('RGB', (1080, 1920), 'white')
            draw = ImageDraw.Draw(image)
            for y in range(100, 600, 60):
                draw.rectangle((30, y, 370, y + 20), fill='black')
            image.save(path)
            capture.validate_image(path, 'portrait')
            with self.assertRaisesRegex(RuntimeError, 'orientation mismatch'):
                capture.validate_image(path, 'landscape')
            with self.assertRaisesRegex(RuntimeError, 'highlight missing'):
                capture.validate_image(path, 'portrait', search=True)


if __name__ == '__main__':
    unittest.main()
