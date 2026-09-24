# 見開きPDFユーティリティ (Android)

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

Android向けの強力で直感的なPDFユーティリティアプリです。**見開き（2ページスプレッド表示）**に特化しており、自炊した書籍、漫画、ビジネス文書の管理に最適です。

[**Webサイト (日本語)**](https://tyamada.github.io/MihirakiPDFUtility_Android/) | [**English Website**](https://tyamada.github.io/MihirakiPDFUtility_Android/index_en.html) | [**English README (README.md)**](README.md)

## 🚀 主な機能

| 機能 | 説明 |
| :--- | :--- |
| 📖 **見開き表示** | 2ページを横並びで表示。日本の書籍や漫画に合わせた「右開き（RTL）」にも完全対応しています。 |
| ✂️ **スマートページ分割** | 見開き1ページを2つのページに分割。垂直（左右）と水平（上下）の両方の分割方向をサポートします。 |
| 🔀 **ドラッグ＆ドロップ並べ替え** | サムネイルをドラッグするだけでページ順序を整理。直感的なインターフェースで編集が捗ります。 |
| ➕ **結合と追加** | 複数のPDFファイルを1つに結合したり、既存のPDFにページを追加したりできます。 |
| 🔒 **セキュリティ** | 暗号化されたPDFのパスワード保護や、メタデータの編集が可能です。 |
| 🖥️ **クロスデバイス対応** | スマートフォン、タブレット、ChromeOS（デスクトップ環境）で快適に動作するよう最適化されています。 |

### 機能紹介

<img src="store_listing/MihirakiPDFUtility-feature-graphic.png" alt="Feature Showcase" width="100%" style="margin: 20px 0; border-radius: 8px;">

### 画面イメージ

各デバイスでの動作例：

| デバイス | 表示 | 画像 |
| :--- | :--- | :--- |
| **スマートフォン** | 見開き表示 | <img src="store_listing/screenshots/smartphone/2_mihiraki_view.png" width="300" height="auto" style="margin: 5px;"> |
| **タブレット** | ページ分割 | <img src="store_listing/screenshots/tablet_10/1_main_grid.png" width="300" height="auto" style="margin: 5px;"> |
| **ChromeOS** | デスクトップUI | <img src="store_listing/screenshots/chromebook/6_support_diarlog.png" width="300" height="auto" style="margin: 5px;"> |

## 🌐 多言語サポート

日本語、英語、韓国語、中国語、ドイツ語、フランス語、スペイン語、ポルトガル語、ベトナム語、タガログ語、ネパール語、インドネシア語など13言語に対応しています。

## 技術スタック

*   **UI**: Jetpack Compose (Material 3)
*   **アーキテクチャ**: MVVM (ViewModel, StateFlow)
*   **PDF処理**: [PdfBox-Android](https://github.com/TomRoush/PdfBox-Android)
*   **言語**: Kotlin

## ライセンス

Apache License 2.0
