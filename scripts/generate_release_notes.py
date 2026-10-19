#!/usr/bin/env python3
"""
Generate Play Console Release Notes for MihirakiPDFUtility
Extracts versionName and versionCode from app/build.gradle.kts,
and generates release notes (under 500 characters) in Japanese, English, Korean, and Chinese (Simplified),
saving them into `store_listing/` with versionName, versionCode, and language code in the filenames.
"""

import os
from pathlib import Path

def parse_gradle_version(gradle_path):
    version_name = "1.1.0"
    version_code = "9"
    if gradle_path.exists():
        for line in gradle_path.read_text().splitlines():
            if 'versionName' in line:
                parts = line.split('=')
                if len(parts) > 1:
                    version_name = parts[1].strip().strip('"\'')
            elif 'versionCode' in line:
                parts = line.split('=')
                if len(parts) > 1:
                    version_code = parts[1].strip().strip('"\'')
    return version_name, version_code

def main():
    root_dir = Path(__file__).resolve().parent.parent
    gradle_path = root_dir / 'app' / 'build.gradle.kts'
    version_name, version_code = parse_gradle_version(gradle_path)

    store_listing_dir = root_dir / 'store_listing'
    changelog_dir = store_listing_dir / 'changelog'
    changelog_dir.mkdir(parents=True, exist_ok=True)

    notes = {
        "ja": f"""見開きPDFユーティリティ v{version_name} (versionCode {version_code}) のリリースノート:
- ヘルプ画面に実際のアイコン付きの「主要なボタン・アイコンの機能一覧（チートシート）」を追加しました。
- ヘルプの「表示切り替え」と「デバイス対応」の項目を分かりやすく別々の行に分割しました。
- 右開き(RTL)と左開き(LTR)の読み方向切り替え機能をさらにブラッシュアップしました。
- ダイアログ構造のリファクタリングによる安定性・パフォーマンスの向上。
- 各種バグ修正と動作の最適化。""",

        "en": f"""Mihiraki PDF Utility v{version_name} (versionCode {version_code}) Release Notes:
- Added a visual toolbar buttons & icons cheat sheet inside the Help guide.
- Split Help categories into dedicated Display Modes and Device Support sections.
- Enhanced Right-to-Left (RTL) and Left-to-Right (LTR) reading direction toggling.
- Improved app stability and maintainability through dialog code refactoring.
- General bug fixes and performance optimizations.""",

        "ko": f"""미히라키 PDF 유틸리티 v{version_name} (versionCode {version_code}) 릴리스 노트:
- 도움말 가이드에 실제 아이콘이 포함된 주요 버튼 및 아이콘 치트 시트를 추가했습니다.
- 도움말의 '표시 모드'와 '기기 지원' 항목을 별도의 섹션으로 분리했습니다.
- 오른쪽에서 왼쪽으로(RTL) 및 왼쪽에서 오른쪽으로(LTR) 읽기 방향 전환 기능을 개선했습니다.
- 다이얼로그 코드 리팩토링을 통한 앱 안정성 및 유지보수성 향상.
- 일반적인 버그 수정 및 성능 최적화.""",

        "zh-CN": f"""见解PDF实用工具 v{version_name} (versionCode {version_code}) 发行说明：
- 在帮助指南中添加了带有实际图标的工具栏按钮与图标速查表（Cheat Sheet）。
- 将帮助中的“显示模式”与“设备支持”分类拆分为独立章节。
- 优化了从右到左 (RTL) 与从左到右 (LTR) 阅读方向的切换功能。
- 通过对话框代码重构提升了应用稳定性和可维护性。
- 常规错误修复与性能优化。"""
    }

    print(f"Generating release notes for version {version_name} ({version_code})...")
    for lang, text in notes.items():
        if len(text) > 500:
            print(f"WARNING: Release note for {lang} exceeds 500 characters ({len(text)} chars)!")

        filename = f"changelog_{version_name}_{version_code}_{lang}.txt"
        filepath = changelog_dir / filename
        filepath.write_text(text, encoding='utf-8')
        print(f"Saved: {filepath.relative_to(root_dir)} ({len(text)} chars)")

    print("All release notes generated successfully under store_listing/changelog/")

if __name__ == '__main__':
    main()
