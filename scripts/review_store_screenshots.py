#!/usr/bin/env python3
"""Audit captures and build contact sheets; visual approval is a separate explicit step."""
import argparse
import json
from pathlib import Path
from PIL import Image, ImageDraw
import capture_store_screenshots as capture


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--approve', nargs='+', help='Exact PNG paths inspected by the reviewer')
    args = parser.parse_args()
    if args.approve:
        for name in args.approve:
            path = Path(name).resolve()
            if not path.is_relative_to(capture.OUT.resolve()):
                raise ValueError('Approval path must be inside capture output')
            record = path.with_suffix('.json')
            data = json.loads(record.read_text())
            if data['imageSha256'] != capture.sha(path):
                raise ValueError(f'Hash mismatch: {path}')
            capture.validate_image(path, 'portrait' if data['portrait'] else 'landscape', dimensions=data['dimensions'])
            data['validation'] = 'automated-pass; visual-reviewed'
            record.write_text(json.dumps(data, ensure_ascii=False, indent=2) + '\n')
    entries, missing, issues = [], [], []
    sheets = capture.WORK / 'contact-sheets'
    sheets.mkdir(parents=True, exist_ok=True)
    for device in capture.DEVICES:
        for lang in capture.LANGUAGES:
            tiles = []
            for scene in capture.selected_scenes(device):
                path = capture.OUT / device / f'{lang}_{scene}_{capture.orientation(device, scene)}.png'
                if not path.exists():
                    missing.append(str(path.relative_to(capture.ROOT)))
                    continue
                try:
                    record = json.loads(path.with_suffix('.json').read_text())
                    capture.validate_image(path, capture.orientation(device, scene), dimensions=capture.expected_size(device, record['portrait']))
                    if record['imageSha256'] != capture.sha(path):
                        raise ValueError('Image hash mismatch')
                    if (record['language'], record['scene'], record['avd'], record['versionCode']) != (lang, scene, device, capture.CONFIG['versionCode']):
                        raise ValueError('Capture identity mismatch')
                    entries.append(dict(path=str(path.relative_to(capture.ROOT)), **record))
                    with Image.open(path) as image:
                        image.thumbnail((600, 600))
                        tile = Image.new('RGB', (620, 650), '#dddddd')
                        tile.paste(image, ((620-image.width)//2, 30))
                        ImageDraw.Draw(tile).text((10, 8), f'{lang} {scene}', fill='black')
                        tiles.append(tile)
                except (OSError, ValueError, KeyError) as error:
                    issues.append(f'{path}: {error}')
            if tiles:
                sheet = Image.new('RGB', (620*4, 650*2), 'white')
                for i, tile in enumerate(tiles):
                    sheet.paste(tile, ((i%4)*620, (i//4)*650))
                sheet.save(sheets / f'{device}-{lang}.jpg', quality=92)
    summary = dict(expected=112, captured=len(entries), reviewed=sum(e['validation']=='automated-pass; visual-reviewed' for e in entries), missing=missing, issues=issues, entries=entries)
    (capture.WORK / 'review-manifest.json').write_text(json.dumps(summary, ensure_ascii=False, indent=2)+'\n')
    print(json.dumps({k:v for k,v in summary.items() if k != 'entries'},ensure_ascii=False,indent=2))
    if issues:
        raise SystemExit(1)

if __name__ == '__main__':
    main()
