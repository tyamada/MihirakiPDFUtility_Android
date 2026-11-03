# 見開きPDFユーティリティ (Android)

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

Android向けの強力で直感的なPDFユーティリティアプリです。仕事・学業・提出書類のPDF整理や、**見開き（2ページスプレッド表示）**による自炊書籍・漫画の管理に最適です。

[**Webサイト (日本語)**](https://tyamada.github.io/MihirakiPDFUtility_Android/index_ja.html) | [**English Website**](https://tyamada.github.io/MihirakiPDFUtility_Android/) | [**English README (README.md)**](README.md)


## リリース候補

候補版：<!-- current-app-version:start -->1.2.9 (25)<!-- current-app-version:end -->。公開済みのサンプル漫画v2を全51冊へ更新し、中南米スペイン語・ポルトガル・香港標準書面語版を追加しました。取得時と開く前に内容を検証し、旧PDFを保持します。クローズドテスト配信の準備中です。

サンプル漫画は14言語・17言語地域区分×3話。地域名は原語と英語名を併記し、英語名順に表示します。第1話順／最新話順の選択は再起動後も保持します。

[配信報告書](docs/CLOSED_TEST_RELEASE_PROGRESS_1.2.9_25.ja.md) ／ [実機再確認手順](docs/CLOSED_TEST_DEVICE_CHECKLIST_1.2.9_25.ja.md)。

## 🚀 主な機能

| 機能 | 説明 |
| :--- | :--- |
| 📖 **見開き表示** | 2ページを横並びで表示。日本の書籍や漫画に合わせた「右開き（RTL）」にも完全対応しています。 |
| ✂️ **スマートページ分割** | 見開き1ページを2つのページに分割。垂直（左右）と水平（上下）の両方の分割方向をサポートします。 |
| 🔀 **ドラッグ＆ドロップ並べ替え** | サムネイルをドラッグするだけでページ順序を整理。直感的なインターフェースで編集が捗ります。 |
| ➕ **結合と追加** | 複数のPDFファイルを1つに結合したり、既存のPDFにページを追加したりできます。 |
| 🖥️ **アダプティブメニューとカスタマイズ** | 画面幅840dp以上の水平メニューバーと、小さい画面向けのカテゴリ別スクロール可能なボトムシートメニューの自動切り替え、およびオプションからのツールバーボタンカスタマイズ。 |
| 🔒 **セキュリティ＆プライバシー** | アプリ内プライバシーポリシー画面、パスワード保護、メタデータ編集、自動サニタイズ、ローカル監査ログ。 |
| 🌟 **インタラクティブチュートリアル** | 初回起動時の分かりやすいガイド画面。サンプルPDFの即時試用や、ヘルプからのいつでも再確認に対応。 |
| 🔍 **総合診断・ベンチマーク** | アプリ内で動的生成するテストPDFを活用した総合診断・ベンチマークテストの実行と、端末内ローカル監査ログ（24時間自動保持・外部送信なし）の確認が可能。 |
| 💻 **クロスデバイス対応** | スマートフォン、タブレット、折りたたみ端末、ChromeOS（デスクトップ環境）で快適に動作。 |
| 🔄 Play Store 更新確認 | 起動時の自動更新確認およびバージョン画面からの手動更新確認（レートリミット機能付き）、更新検出時のツールバーへの動的更新ボタン表示。 |

### 機能紹介

<img src="docs/images/feature-graphic-ja.png" alt="Feature Showcase" width="100%" style="margin: 20px 0; border-radius: 8px;">

### 画面イメージ

各デバイスでの動作例：

| デバイス | 表示 | 画像 |
| :--- | :--- | :--- |
| **スマートフォン** | 見開き表示 | <img src="docs/screenshots/smartphone/ja_phone_02_spread_landscape.png" width="300" height="auto" style="margin: 5px;"> |
| **タブレット** | ページ分割 | <img src="docs/screenshots/tablet_10/ja_tablet10_04_split_landscape.png" width="300" height="auto" style="margin: 5px;"> |
| **ChromeOS** | デスクトップUI | <img src="docs/screenshots/chromebook/ja_desktop_02_spread_landscape.png" width="300" height="auto" style="margin: 5px;"> |

## 🌐 多言語サポート

イタリア語、日本語、英語、韓国語、中国語、ドイツ語、フランス語、スペイン語、ポルトガル語、ベトナム語、タガログ語、ネパール語、インドネシア語など14言語に対応しています。

## サンプルPDF

アプリでお試しいただけるサンプルPDF（マンガ）をダウンロードできます：

14言語・17言語地域区分×3話の全51冊です。スペイン／中南米、ブラジル／ポルトガル、台湾／香港を区別して選べます。香港版は標準書面語で、口語の広東語版ではありません。取得時にサイズとSHA-256を検証し、旧PDFは保持します。

- [KLUB COBA-COBA - EPISODE 1 / PETA TEMPAT ISTIRAHAT — Bahasa Indonesia (Indonesian) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode1_3_id.pdf) (24.1 MiB)
- [KLUB COBA-COBA - EPISODE 2 / MEJANYA MAKIN LUAS — Bahasa Indonesia (Indonesian) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode2_3_id.pdf) (25.0 MiB)
- [KLUB COBA-COBA - EPISODE 3 / KLUB JALAN-JALAN — Bahasa Indonesia (Indonesian) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode3_2_id.pdf) (24.2 MiB)
- [Der Probierclub - Folge 1: Die Pausenkarte — Deutsch (German) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode1_3_de.pdf) (23.0 MiB)
- [Der Probierclub - Folge 2: Mehr Platz am Tisch — Deutsch (German) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode2_3_de.pdf) (23.9 MiB)
- [Der Probierclub - Folge 3: Unterwegs — Deutsch (German) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode3_2_de.pdf) (22.2 MiB)
- [The Try-It Club - Episode 1: The Break-Time Map — English — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode1_3_en.pdf) (23.1 MiB)
- [The Try-It Club - Episode 2: Room to Grow — English — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode2_3_en.pdf) (23.9 MiB)
- [The Try-It Club - Episode 3: Out and About — English — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode3_2_en.pdf) (22.3 MiB)
- [EL CLUB DE LOS EXPERIMENTOS - EPISODIO 1 / MAPA DE DESCANSOS — Español (España) (Spanish (Spain)) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode1_3_es.pdf) (23.0 MiB)
- [EL CLUB DE LOS EXPERIMENTOS - EPISODIO 2 / LA MESA CRECE — Español (España) (Spanish (Spain)) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode2_3_es.pdf) (23.9 MiB)
- [EL CLUB DE LOS EXPERIMENTOS - EPISODIO 3 / DE SALIDA — Español (España) (Spanish (Spain)) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode3_2_es.pdf) (22.3 MiB)
- [EL CLUB DE LOS EXPERIMENTOS - EPISODIO 1 / MAPA DE DESCANSOS (es-419) — Español (Latinoamérica) (Spanish (Latin America)) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode1_3_es_419.pdf) (23.1 MiB)
- [EL CLUB DE LOS EXPERIMENTOS - EPISODIO 2 / LA MESA CRECE (es-419) — Español (Latinoamérica) (Spanish (Latin America)) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode2_3_es_419.pdf) (23.9 MiB)
- [EL CLUB DE LOS EXPERIMENTOS - EPISODIO 3 / DE SALIDA (es-419) — Español (Latinoamérica) (Spanish (Latin America)) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode3_2_es_419.pdf) (22.3 MiB)
- [CLUB NG MGA PAGSUBOK - KABANATA 1 / MAPA NG PAHINGAHAN — Filipino — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode1_3_fil.pdf) (24.5 MiB)
- [CLUB NG MGA PAGSUBOK - KABANATA 2 / LUMUWANG ANG MESA — Filipino — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode2_3_fil.pdf) (25.0 MiB)
- [CLUB NG MGA PAGSUBOK - KABANATA 3 / LUMABAS ANG CLUB — Filipino — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode3_2_tl.pdf) (23.8 MiB)
- [Le Club des essais - Épisode 1 : Le plan des coins détente — Français (French) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode1_3_fr.pdf) (23.0 MiB)
- [Le Club des essais - Épisode 2 : La table s’agrandit — Français (French) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode2_3_fr.pdf) (23.9 MiB)
- [Le Club des essais - Épisode 3 : Le club en sortie — Français (French) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode3_2_fr.pdf) (22.2 MiB)
- [IL CLUB DEGLI ESPERIMENTI - EPISODIO 1 / LA MAPPA DELLE PAUSE — Italiano (Italian) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode1_3_it.pdf) (23.1 MiB)
- [IL CLUB DEGLI ESPERIMENTI - EPISODIO 2 / IL TAVOLO SI ALLARGA — Italiano (Italian) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode2_3_it.pdf) (23.9 MiB)
- [IL CLUB DEGLI ESPERIMENTI - EPISODIO 3 / IL CLUB IN TRASFERTA — Italiano (Italian) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode3_2_it.pdf) (22.3 MiB)
- [O CLUBE DAS EXPERIÊNCIAS - EPISÓDIO 1 / MAPA DO DESCANSO — Português (Brasil) (Portuguese (Brazil)) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode1_3_pt.pdf) (23.1 MiB)
- [O CLUBE DAS EXPERIÊNCIAS - EPISÓDIO 2 / A MESA CRESCE — Português (Brasil) (Portuguese (Brazil)) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode2_3_pt.pdf) (23.9 MiB)
- [O CLUBE DAS EXPERIÊNCIAS - EPISÓDIO 3 / O CLUBE VAI À BIBLIOTECA — Português (Brasil) (Portuguese (Brazil)) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode3_2_pt.pdf) (22.3 MiB)
- [O CLUBE DAS EXPERIÊNCIAS - EPISÓDIO 1 / MAPA DO DESCANSO (pt-PT) — Português (Portugal) (Portuguese (Portugal)) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode1_3_pt_pt.pdf) (23.2 MiB)
- [O CLUBE DAS EXPERIÊNCIAS - EPISÓDIO 2 / A MESA CRESCE (pt-PT) — Português (Portugal) (Portuguese (Portugal)) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode2_3_pt_pt.pdf) (24.0 MiB)
- [O CLUBE DAS EXPERIÊNCIAS - EPISÓDIO 3 / O CLUBE VAI À BIBLIOTECA (pt-PT) — Português (Portugal) (Portuguese (Portugal)) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode3_2_pt_pt.pdf) (22.3 MiB)
- [CÂU LẠC BỘ THỬ NGHIỆM - TẬP 1 / BẢN ĐỒ CHỖ NGHỈ — Tiếng Việt (Vietnamese) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode1_3_vi.pdf) (24.8 MiB)
- [CÂU LẠC BỘ THỬ NGHIỆM - TẬP 2 / CHIẾC BÀN RỘNG HƠN — Tiếng Việt (Vietnamese) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode2_3_vi.pdf) (25.5 MiB)
- [CÂU LẠC BỘ THỬ NGHIỆM - TẬP 3 / CÂU LẠC BỘ ĐI CÔNG TÁC — Tiếng Việt (Vietnamese) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode3_2_vi.pdf) (24.3 MiB)
- [प्रयोग क्लब - भाग 1 / आरामको नक्सा — नेपाली (Nepali) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode1_3_ne.pdf) (25.7 MiB)
- [प्रयोग क्लब - भाग 2 / टेबुल फराकिलो भयो — नेपाली (Nepali) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode2_3_ne.pdf) (26.6 MiB)
- [प्रयोग क्लब - भाग 3 / क्लबको पहिलो भ्रमण — नेपाली (Nepali) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode3_2_ne.pdf) (23.7 MiB)
- [ためし部 第1話 ひと息マップ — 日本語 (Japanese) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode1_2_ja.pdf) (23.1 MiB)
- [ためし部 第2話 机、ひろがる。 — 日本語 (Japanese) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode2_2_ja.pdf) (23.9 MiB)
- [ためし部 第3話 出張、ためし部！ — 日本語 (Japanese) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode3_ja.pdf) (22.2 MiB)
- [试试社 第1话 歇口气地图 — 简体中文 (Chinese (Simplified)) — 小真彩日和](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode1_2_zh_cn.pdf) (23.1 MiB)
- [试试社 第2话 桌子变大了 — 简体中文 (Chinese (Simplified)) — 小真彩日和](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode2_2_zh_cn.pdf) (23.9 MiB)
- [第3话 / 试试社，出差啦！ — 简体中文 (Chinese (Simplified)) — 小真彩日和](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode3_2_zh_cn.pdf) (22.2 MiB)
- [試試社 第1話 歇口氣地圖 — 繁體中文（台灣） (Chinese (Traditional, Taiwan)) — 小舞樓日和](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode1_2_zh_tw.pdf) (23.1 MiB)
- [試試社 第2話 桌子變大了 — 繁體中文（台灣） (Chinese (Traditional, Taiwan)) — 小舞樓日和](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode2_2_zh_tw.pdf) (23.9 MiB)
- [第3話 / 試試社，出差啦！ — 繁體中文（台灣） (Chinese (Traditional, Taiwan)) — 小舞樓日和](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode3_2_zh_tw.pdf) (22.2 MiB)
- [試試社 第1話 歇口氣地圖 (zh-Hant-HK) — 繁體中文（香港書面語） (Chinese (Traditional, Hong Kong written standard)) — 小舞樓日和](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode1_3_zh_hk.pdf) (23.1 MiB)
- [試試社 第2話 桌子變大了 (zh-Hant-HK) — 繁體中文（香港書面語） (Chinese (Traditional, Hong Kong written standard)) — 小舞樓日和](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode2_3_zh_hk.pdf) (24.0 MiB)
- [第3話 / 試試社，出差啦！ (zh-Hant-HK) — 繁體中文（香港書面語） (Chinese (Traditional, Hong Kong written standard)) — 小舞樓日和](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode3_2_zh_hk.pdf) (22.2 MiB)
- [해봄부 제1화 한숨 돌림 지도 — 한국어 (Korean) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode1_2_ko.pdf) (23.0 MiB)
- [해봄부 제2화 책상이 넓어지다 — 한국어 (Korean) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode2_2_ko.pdf) (23.8 MiB)
- [제3화 / 해봄부, 출장! — 한국어 (Korean) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode3_2_ko.pdf) (22.2 MiB)

### サンプルPDFのライセンス (CC BY 4.0)

『ためし部』第1〜3話
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

## リリースおよびドキュメント更新ルール

App Bundle ビルド前に、最新の仕様に合わせて `README.md`、`LICENSE`、`docs/*.html`、`store_listing/metadata*.txt` を更新するルールが設定されています。詳細は [RELEASE_RULES.md](RELEASE_RULES.md) を参照してください。Gradle (`preBundleDocumentationCheck`) によりビルド前の自動検証が行われます。

### サンプルPDFの配布

51件のサンプル漫画PDFは[GitHub Releases](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/tag/sample-manga-v2)から配布します。旧アプリ向けのGitHub Pages上のPDFと既存URLは維持します。[移行・検証記録](docs/SAMPLE_PDF_RELEASE_MIGRATION.ja.md)を参照してください。

[サンプルv2の実装・検証記録](docs/SAMPLE_PDF_V2_UPDATE.ja.md)
