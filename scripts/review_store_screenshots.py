#!/usr/bin/env python3
"""Verify selected original captures and produce contact sheets; never upload."""
import argparse
import json
import math
from PIL import Image, ImageDraw
import capture_store_screenshots as capture


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--device', choices=capture.DEVICES, nargs='+', default=list(capture.DEVICES))
    parser.add_argument('--language', choices=capture.LANGUAGES, nargs='+', default=list(capture.LANGUAGES))
    parser.add_argument('--compatible-fingerprint', action='append', default=[],
                        help='Allow verified records from an equivalent capture runner revision')
    parser.add_argument('--mark-reviewed', action='store_true', help='Use only after inspecting every selected original')
    options = parser.parse_args()
    apks = [capture.ROOT / 'app/build/outputs/apk/debug/app-debug.apk', capture.ROOT / 'app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk']
    stamp = capture.fingerprint(apks)
    report_dir = capture.ROOT / 'store_listing/screenshot_review'
    report_dir.mkdir(exist_ok=True)
    rows, records, sheets = [], [], []
    seen = set()
    # Validate the entire selection before marking any record as visually reviewed.
    for avd in options.device:
        scenes = capture.selected_scenes(avd)
        for lang in options.language:
            row_heights = []
            for start in range(0, len(scenes), 3):
                heights = []
                for scene in scenes[start:start+3]:
                    width, height = capture.expected_size(avd, capture.orientation(avd, scene) == 'portrait')
                    heights.append(math.ceil(height * min(468 / width, 800 / height)))
                row_heights.append(max(heights) + 50)
            sheet = Image.new('RGB', (1440, sum(row_heights)), '#707070')
            draw = ImageDraw.Draw(sheet)
            links = []
            for i, scene in enumerate(scenes):
                target = capture.orientation(avd, scene)
                path = capture.OUT / avd / f'{lang}_{scene}_{target}.png'
                record = path.with_suffix('.json')
                expected = dict(fingerprint=stamp, pdfSha256=capture.sha(capture.ROOT / f'testdata/tameshibu_episode1_2_{lang}.pdf'),
                                avd=avd, language=lang, scene=scene, portrait=target == 'portrait',
                                versionCode=capture.CONFIG['versionCode'], dimensions=list(capture.expected_size(avd, target == 'portrait')))
                if not capture.can_resume(path, record, expected, options.compatible_fingerprint):
                    raise RuntimeError(f'Missing/stale/invalid capture: {path}')
                info = json.loads(record.read_text())
                if info['pageCount'] != capture.CONFIG['pageCount']:
                    raise RuntimeError(f'Unexpected PDF: {path}')
                if scene == '07_outline' and not info.get('outlineVerified'):
                    raise RuntimeError(f'Outline not verified: {path}')
                if capture.PROFILES[avd]['desktop'] and not info.get('maximized'):
                    raise RuntimeError(f'Desktop window not maximized: {path}')
                if scene == '04_search' and not any(r['top'] > 40 and r['bottom'] < 560 for r in info['page6SearchRects']):
                    raise RuntimeError(f'No dialogue search hit: {path}')
                digest = capture.sha(path)
                if digest in seen:
                    raise RuntimeError(f'Duplicate screenshot: {path}')
                seen.add(digest)
                records.append((record, info))
                with Image.open(path) as raw:
                    picture = raw.convert('RGB')
                    picture.thumbnail((468, 800))
                    x, y = i % 3 * 480, sum(row_heights[:i // 3])
                    sheet.paste(picture, (x + (480-picture.width)//2, y+35))
                    draw.text((x+8, y+10), f'{lang} {scene} {raw.width}x{raw.height}', fill='white')
                links.append(f'[{scene}](screenshots/{avd}/{path.name})')
            name = f'{avd}-{lang}.jpg'
            sheets.append((sheet, report_dir / name))
            rows.append(f'| {avd} | {lang} | ' + ' / '.join(links) + f' | [一覧](screenshot_review/{name}) |')
    for sheet, path in sheets:
        sheet.save(path, quality=92)
    if options.mark_reviewed:
        for path, info in records:
            info['validation'] = 'automated-pass; visual-reviewed'
            path.write_text(json.dumps(info, ensure_ascii=False, indent=2)+'\n')
    total = sum(len(capture.selected_scenes(avd)) for avd in capture.DEVICES) * len(capture.LANGUAGES)
    scope = '全対象' if len(seen) == total else f'部分確認（全体{total}枚中{len(seen)}枚、全体未完了）'
    status = '目視確認済み' if options.mark_reviewed else '目視確認未完了'
    report = f"# スクリーンショット検品結果\n\nversionCode {capture.CONFIG['versionCode']}。{scope}。自動検査合格、{status}。\nPlay Consoleへの登録は未実施。登録にはPNG原本を使用。\n\n| AVD | 言語 | 原本（掲載順） | 確認用一覧 |\n| --- | --- | --- | --- |\n" + '\n'.join(rows)+'\n'
    name = 'SCREENSHOT_CAPTURE_REPORT.ja.md' if len(seen) == total else 'SCREENSHOT_CAPTURE_REPORT.partial.ja.md'
    (capture.ROOT / 'store_listing' / name).write_text(report)
    print(f'Validated {len(seen)}/{total} originals; {status}')


if __name__ == '__main__':
    main()
