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
| 🔍 **診断・ローカルログ** | アプリ内で実機診断テストを実行したり、端末内ローカルの監査ログ（24時間自動保持・外部送信機能なし・個人情報不含）を閲覧・共有できます。 |
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

## サンプルPDF

アプリでお試しいただけるサンプルPDF（マンガ）をダウンロードできます：

- [THE TRY-IT CLUB EPISODE 1: THE BREAK-TIME MAP (English) - Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/raw/main/testdata/tameshibu_episode1_2_en.pdf)
- [THE TRY-IT CLUB EPISODE 2: ROOM TO GROW (English) - Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/raw/main/testdata/tameshibu_episode2_2_en.pdf)
- [ためし部 第１話 ひと息マップ (Japanese) - こまいろ日和](https://github.com/tyamada/MihirakiPDFUtility_Android/raw/main/testdata/tameshibu_episode1_2_ja.pdf)
- [ためし部 第２話 机、ひろがる。 (Japanese) - こまいろ日和](https://github.com/tyamada/MihirakiPDFUtility_Android/raw/main/testdata/tameshibu_episode2_2_ja.pdf)
- [해봄부 제1화 한숨 돌림 지도 (Korean) - 코마이로 히요리](https://github.com/tyamada/MihirakiPDFUtility_Android/raw/main/testdata/tameshibu_episode1_2_ko.pdf)
- [해봄부제2화 책상이 넓어지다 (Korean) - 코마이로 히요리](https://github.com/tyamada/MihirakiPDFUtility_Android/raw/main/testdata/tameshibu_episode2_2_ko.pdf)
- [试试社 第1话 歇口气地图 (Chinese (Simplified)) - 小真彩日和](https://github.com/tyamada/MihirakiPDFUtility_Android/raw/main/testdata/tameshibu_episode1_2_zh_cn.pdf)
- [试试社 第2话 桌子变大了 (Chinese (Simplified)) - 小真彩日和](https://github.com/tyamada/MihirakiPDFUtility_Android/raw/main/testdata/tameshibu_episode2_2_zh_cn.pdf)
- [試試社 第1話 (Chinese (Traditional)) - 小舞樓日和](https://github.com/tyamada/MihirakiPDFUtility_Android/raw/main/testdata/tameshibu_episode1_2_zh_tw.pdf)
- [試試社 第2話 (Chinese (Traditional)) - 小舞樓日和](https://github.com/tyamada/MihirakiPDFUtility_Android/raw/main/testdata/tameshibu_episode2_2_zh_tw.pdf)
- [DER PROBIERCLUB FOLGE 1 (German) - Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/raw/main/testdata/tameshibu_episode1_2_de.pdf)
- [DER PROBIERCLUB FOLGE 2 (German) - Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/raw/main/testdata/tameshibu_episode2_2_de.pdf)
- [LE CLUB DES ESSAIS EPISODE 1 (French) - Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/raw/main/testdata/tameshibu_episode1_2_fr.pdf)
- [LE CLUB DES ESSAIS EPISODE 2 (French) - Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/raw/main/testdata/tameshibu_episode2_2_fr.pdf)

### サンプルPDFのライセンス (CC BY 4.0)

『ためし部』第1話・第2話
© 2026 こまいろ日和

本作品のうち、公開者が著作権その他の許諾対象となる権利を有する部分を、[Creative Commons 表示 4.0 国際（CC BY 4.0）](https://creativecommons.org/licenses/by/4.0/)で提供します。

再利用時は、作品名、作者名「こまいろ日和」、原作品の公開元、ライセンスへのリンクを表示し、変更した場合はその旨を明示してください。

本作品は、文章・画像の制作および翻訳に生成AIを使用しています。フォントなど第三者に権利がある素材には、それぞれのライセンスが適用されます。著作権等による保護を受けない部分の利用を、この表示によって制限するものではありません。

## 技術スタック

*   **UI**: Jetpack Compose (Material 3)
*   **アーキテクチャ**: MVVM (ViewModel, StateFlow)
*   **PDF処理**: [PdfBox-Android](https://github.com/TomRoush/PdfBox-Android)
*   **言語**: Kotlin

## ライセンス

Apache License 2.0
