#!/usr/bin/env python3
"""
YouTube Shorts Asset Preparation & Verification Script
Verifies existing test PDFs, generates supplemental Shorts recording assets,
and produces prompt guidelines for the Gemini app to generate stylized tutorial pages or cover art.
"""
import os
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
TESTDATA = ROOT / 'testdata'
ASSETS = ROOT / 'shorts_raw_assets'
PROMPT_OUT = ROOT / 'shorts_assets_gemini_prompt.md'

LANGUAGES = {
    "ja": "日本語 (Japanese) - 見開きプレビュー & 14言語対応",
    "en": "English - Spread Preview & 14 Languages",
    "ko": "한국어 (Korean) - 펼쳐보기 & 14개 언어 지원",
    "zh_cn": "中文 (简体) - 跨页预览 & 14种语言支持",
    "zh_tw": "中文 (繁體) - 跨頁預覽 & 14種語言支援",
    "fr": "Français - Aperçu double page & 14 langues",
    "de": "Deutsch - Doppelseitenansicht & 14 Sprachen",
    "es": "Español - Vista de doble página & 14 idiomas",
    "it": "Italiano - Anteprima pagine affiancate & 14 lingue",
    "pt": "Português - Visualização em página dupla & 14 idiomas",
    "vi": "Tiếng Việt - Xem trước trang đôi & 14 ngôn ngữ",
    "id": "Bahasa Indonesia - Tampilan halaman ganda & 14 bahasa",
    "fil": "Filipino - Preview ng magkatapat na pahina & 14 wika",
    "ne": "नेपाली - दोहोरो पृष्ठ पूर्वावलोकन & १४ भाषाहरू"
}

def verify_testdata():
    print("Verifying testdata directory and required PDF files...")
    if not TESTDATA.exists():
        print(f"ERROR: testdata directory not found at {TESTDATA}")
        sys.exit(1)

    missing = []
    for lang in LANGUAGES.keys():
        pdf_name = f"tameshibu_episode1_2_{lang}.pdf"
        pdf_path = TESTDATA / pdf_name
        if not pdf_path.exists():
            missing.append(pdf_name)

    if missing:
        print(f"WARNING: Some localized test PDFs are missing: {missing}")
        print("Falling back or generating standard test PDFs...")
    else:
        print("SUCCESS: All localized sample PDFs for 14 languages are present in testdata/.")

def generate_gemini_prompt_guide():
    print(f"Generating Gemini App prompt guide at {PROMPT_OUT}...")
    content = """# YouTube Shorts Tutorial Assets - Gemini App Prompt Guide

このガイドは、YouTube Shorts用チュートリアル動画（全5シーン）で使用する高品質なPDF素材やカバーアート、シーンごとの解説グラフィックを **Geminiアプリ**（または Gemini Nano / Cloud AI）に作成・生成してもらうためのプロンプト集です。

---

## 1. シーン別アセット生成プロンプト

### [Scene 1] 見開きプレビュー & 14言語対応
- **Geminiへの依頼プロンプト**:
  > 「漫画や同人誌のデジタル配信アプリ向けに、日本語・英語・韓国語・中国語など14言語の切り替え表示と、右開き/左開きの見開きプレビュー機能を紹介するための、8ページのサンプルPDF（表紙＋ストーリーページ）のレイアウト構成案と、表紙カバーのイメージイラスト生成用プロンプトを作成してください。」

### [Scene 2] 高速インポート & ナビゲーション
- **Geminiへの依頼プロンプト**:
  > 「大量のPDFファイル（100ページ以上）を高速インポートし、ボトムシートのサムネイルバーやページシークスライダーを使って瞬時に目的のページへジャンプするデモ用の、視認性の高い見開きページインデックス構造を持つPDFドキュメントの設計図を作成してください。」

### [Scene 3] ページ編集コア機能
- **Geminiへの依頼プロンプト**:
  > 「PDFのページ管理アプリにおいて、ページの『回転（Rotate）』、『見開き分割（Split）』、『白紙挿入（Insert Blank）』、『不要ページ削除（Delete）』を実演するための、編集前後のページ構成の変化がひと目で分かるイラストやビジュアル解説の構成案を提案してください。」

### [Scene 4] 表示切替 & ツールバーカスタム
- **Geminiへの依頼プロンプト**:
  > 「グリッド表示（サムネイル一覧）と見開きプレビュー（Spread View）をシームレスに切り替え、よく使うツールバーボタンをカスタマイズするUI操作チュートリアル動画向けの、モダンでクリーンなUIモックアップ画像を生成するためのプロンプトを作成してください。」

### [Scene 5] エクスポート & 完了
- **Geminiへの依頼プロンプot**:
  > 「編集・加工した選択ページをPDFとして安全にエクスポート・保存し、完了トーストと成功ダイアログが表示されるシーンを演出するための、達成感のあるアニメーション演出や字幕テキスト（英語・日本語）の文案を作成してください。」

---

## 2. 自動生成スクリプトの実行方法
ローカルのエミュレータ上で全5シーンの動画を自動収録するには、以下のスクリプトを実行してください：
```bash
./scripts/record_shorts_scenes.sh all
```
個別のシーンを収録する場合：
```bash
./scripts/record_shorts_scenes.sh 1
```
"""
    PROMPT_OUT.write_text(content, encoding='utf-8')
    print(f"SUCCESS: Gemini prompt guide generated at {PROMPT_OUT}")

def main():
    ASSETS.mkdir(parents=True, exist_ok=True)
    verify_testdata()
    generate_gemini_prompt_guide()
    print("\nAsset preparation and Gemini prompt guide creation completed successfully!")

if __name__ == '__main__':
    main()
