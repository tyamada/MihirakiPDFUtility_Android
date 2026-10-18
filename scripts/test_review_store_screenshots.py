"""Review output regression tests; no device access."""
import json
from pathlib import Path
import sys
import tempfile
import unittest
from unittest.mock import patch
from PIL import Image
import capture_store_screenshots as capture
import review_store_screenshots as review


class ReviewTest(unittest.TestCase):
    def test_seven_scene_sheet_and_partial_report(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            output = root / 'store_listing/screenshots'
            avd, lang = 'Smartphone_6.3', 'ja'
            images = output / avd
            images.mkdir(parents=True)
            for i, scene in enumerate(capture.selected_scenes(avd)):
                direction = capture.orientation(avd, scene)
                path = images / f'{lang}_{scene}_{direction}.png'
                Image.new('RGB', (1080, 1920), (i*30, 50, 100)).save(path)
                path.with_suffix('.json').write_text(json.dumps(dict(pageCount=8, outlineVerified=True,
                    page6SearchRects=[dict(top=50, bottom=100)])))
            with patch.object(capture, 'ROOT', root), patch.object(capture, 'OUT', output), \
                    patch.object(capture, 'fingerprint', return_value='stamp'), \
                    patch.object(capture, 'can_resume', return_value=True), \
                    patch.object(capture, 'sha', side_effect=lambda p: str(p)), \
                    patch.object(sys, 'argv', ['review', '--device', avd, '--language', lang]):
                review.main()
            report = root / 'store_listing/SCREENSHOT_CAPTURE_REPORT.partial.ja.md'
            self.assertIn('全体未完了', report.read_text())
            with Image.open(root / f'store_listing/screenshot_review/{avd}-{lang}.jpg') as sheet:
                self.assertEqual((1440, 2550), sheet.size)

    def test_failed_selection_does_not_mark_earlier_records_reviewed(self):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            output = root / 'store_listing/screenshots'
            avd = 'Smartphone_6.3'
            first = capture.selected_scenes(avd)[0]
            path = output / avd / f'ja_{first}_landscape.png'
            path.parent.mkdir(parents=True)
            Image.new('RGB', (1920, 1080), 'red').save(path)
            record = path.with_suffix('.json')
            original = json.dumps(dict(pageCount=8, validation='visual review pending'))
            record.write_text(original)
            with patch.object(capture, 'ROOT', root), patch.object(capture, 'OUT', output), \
                    patch.object(capture, 'fingerprint', return_value='stamp'), \
                    patch.object(capture, 'can_resume', side_effect=[True, False]), \
                    patch.object(capture, 'sha', side_effect=lambda p: str(p)), \
                    patch.object(sys, 'argv', ['review', '--device', avd, '--language', 'ja', '--mark-reviewed']):
                with self.assertRaisesRegex(RuntimeError, 'Missing/stale'):
                    review.main()
            self.assertEqual(original, record.read_text())


if __name__ == '__main__':
    unittest.main()
