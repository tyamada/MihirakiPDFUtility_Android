# Mihiraki PDF Utility (Android)

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

A powerful and intuitive PDF utility for Android, specializing in organizing work, studies, submission documents, scanned books, and manga with **Mihiraki (two-page spread view)**.

[**日本語サイト**](https://tyamada.github.io/MihirakiPDFUtility_Android/index_ja.html) | [**English Website**](https://tyamada.github.io/MihirakiPDFUtility_Android/) | [**日本語 README (README_ja.md)**](README_ja.md)


## Release candidate

Candidate: <!-- current-app-version:start -->1.2.9 (25)<!-- current-app-version:end -->. This update includes all 51 sample manga PDFs from sample-manga-v2, with Latin American Spanish, European Portuguese and Hong Kong standard written Chinese editions. Downloads are verified before use, and older PDFs are retained. Closed-test distribution is being prepared.

The samples cover 14 languages and 17 language/region variants, with three episodes each. Native and English region names are sorted by English name. Episode order is retained after restarting the app.

[Release report](docs/CLOSED_TEST_RELEASE_PROGRESS_1.2.9_25.ja.md) · [Device retest checklist](docs/CLOSED_TEST_DEVICE_CHECKLIST_1.2.9_25.ja.md).

## 🚀 Key Features

| Feature | Description |
| :--- | :--- |
| 📖 **Mihiraki View** | View two pages side-by-side. Full support for Right-to-Left (RTL) reading directions for manga and Japanese books. |
| ✂️ **Smart Splitting** | Split double-page spreads into individual pages. Supports vertical and horizontal splitting. |
| 🔀 **Drag & Drop Reordering** | Easily organize your PDF by dragging and dropping thumbnails. Intuitive page management. |
| ➕ **Merge & Append** | Combine multiple PDF files into one, or add pages to existing documents. |
| 🖥️ **Adaptive Menu & Customization** | Adaptive layout: horizontal menu bar at widths of 840 dp or more, and a categorized, scrollable bottom sheet menu on smaller screens, plus customizable toolbar buttons via Options. |
| 🔒 **Security & Privacy** | Dedicated in-app privacy policy, password protection, metadata editing, automatic active content sanitization, and local audit logs. |
| 🌟 **Interactive Tutorial** | Guided step-by-step tutorial screen for first-time users with instant sample PDF trial and re-triggerable help. |
| 🔍 **Diagnostics & Benchmarks** | Run in-app comprehensive diagnostics and benchmark tests using dynamic test PDFs, and review local audit logs (24-hour retention, offline/privacy-protected). |
| 💻 **Cross-Device Support** | Optimized for Smartphones, Tablets, Foldables, and ChromeOS (desktop experience). |
| 🔄 Play Store Update Check | Automatic update check on launch and manual check in the version dialog with rate limiting, with a dynamic toolbar update button when an update is available. |

### Feature Showcase

<img src="docs/images/feature-graphic-en.png" alt="Feature Showcase" width="100%" style="margin: 20px 0; border-radius: 8px;">

### Visual Examples

Explore how the application functions across different device types:

| Device | View | Image |
| :--- | :--- | :--- |
| **Smartphone** | Spread View | <img src="docs/screenshots/smartphone/en_phone_02_spread_landscape.png" width="300" height="auto" style="margin: 5px;"> |
| **Tablet** | Smart Splitting | <img src="docs/screenshots/tablet_10/en_tablet10_04_split_landscape.png" width="300" height="auto" style="margin: 5px;"> |
| **ChromeOS** | Desktop Interface | <img src="docs/screenshots/chromebook/en_desktop_02_spread_landscape.png" width="300" height="auto" style="margin: 5px;"> |

## 🌐 Multi-language Support

14 languages supported, including Italian, Japanese, English, Korean, Chinese, German, French, Spanish, Portuguese, Vietnamese, Filipino, Nepali, and Indonesian.

## Sample PDFs

You can download and test sample PDFs (Manga) to try out Mihiraki PDF Utility:

51 PDFs in 14 languages and 17 language/region variants, with three episodes each. Spain and Latin America, Brazil and Portugal, and Taiwan and Hong Kong have separate filters. The Hong Kong edition uses standard written Chinese, not colloquial Cantonese. Downloads are verified against their expected size and SHA-256; older PDFs are retained.

The links below are sorted by English language name, then Episode 1–3.

- [试试社 第1话 歇口气地图 — 简体中文 (Chinese (Simplified)) — 小真彩日和](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode1_2_zh_cn.pdf) (23.1 MiB)
- [试试社 第2话 桌子变大了 — 简体中文 (Chinese (Simplified)) — 小真彩日和](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode2_2_zh_cn.pdf) (23.9 MiB)
- [第3话 / 试试社，出差啦！ — 简体中文 (Chinese (Simplified)) — 小真彩日和](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode3_2_zh_cn.pdf) (22.2 MiB)
- [試試社 第1話 歇口氣地圖 \[zh-Hant-HK\] — 繁體中文（香港書面語） (Chinese (Traditional, Hong Kong written standard)) — 小舞樓日和](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode1_3_zh_hk.pdf) (23.1 MiB)
- [試試社 第2話 桌子變大了 \[zh-Hant-HK\] — 繁體中文（香港書面語） (Chinese (Traditional, Hong Kong written standard)) — 小舞樓日和](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode2_3_zh_hk.pdf) (24.0 MiB)
- [第3話 / 試試社，出差啦！ \[zh-Hant-HK\] — 繁體中文（香港書面語） (Chinese (Traditional, Hong Kong written standard)) — 小舞樓日和](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode3_2_zh_hk.pdf) (22.2 MiB)
- [試試社 第1話 歇口氣地圖 — 繁體中文（台灣） (Chinese (Traditional, Taiwan)) — 小舞樓日和](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode1_2_zh_tw.pdf) (23.1 MiB)
- [試試社 第2話 桌子變大了 — 繁體中文（台灣） (Chinese (Traditional, Taiwan)) — 小舞樓日和](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode2_2_zh_tw.pdf) (23.9 MiB)
- [第3話 / 試試社，出差啦！ — 繁體中文（台灣） (Chinese (Traditional, Taiwan)) — 小舞樓日和](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode3_2_zh_tw.pdf) (22.2 MiB)
- [The Try-It Club - Episode 1: The Break-Time Map — English — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode1_3_en.pdf) (23.1 MiB)
- [The Try-It Club - Episode 2: Room to Grow — English — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode2_3_en.pdf) (23.9 MiB)
- [The Try-It Club - Episode 3: Out and About — English — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode3_2_en.pdf) (22.3 MiB)
- [CLUB NG MGA PAGSUBOK - KABANATA 1 / MAPA NG PAHINGAHAN — Filipino — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode1_3_fil.pdf) (24.5 MiB)
- [CLUB NG MGA PAGSUBOK - KABANATA 2 / LUMUWANG ANG MESA — Filipino — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode2_3_fil.pdf) (25.0 MiB)
- [CLUB NG MGA PAGSUBOK - KABANATA 3 / LUMABAS ANG CLUB — Filipino — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode3_2_tl.pdf) (23.8 MiB)
- [Le Club des essais - Épisode 1 : Le plan des coins détente — Français (French) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode1_3_fr.pdf) (23.0 MiB)
- [Le Club des essais - Épisode 2 : La table s’agrandit — Français (French) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode2_3_fr.pdf) (23.9 MiB)
- [Le Club des essais - Épisode 3 : Le club en sortie — Français (French) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode3_2_fr.pdf) (22.2 MiB)
- [Der Probierclub - Folge 1: Die Pausenkarte — Deutsch (German) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode1_3_de.pdf) (23.0 MiB)
- [Der Probierclub - Folge 2: Mehr Platz am Tisch — Deutsch (German) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode2_3_de.pdf) (23.9 MiB)
- [Der Probierclub - Folge 3: Unterwegs — Deutsch (German) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode3_2_de.pdf) (22.2 MiB)
- [KLUB COBA-COBA - EPISODE 1 / PETA TEMPAT ISTIRAHAT — Bahasa Indonesia (Indonesian) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode1_3_id.pdf) (24.1 MiB)
- [KLUB COBA-COBA - EPISODE 2 / MEJANYA MAKIN LUAS — Bahasa Indonesia (Indonesian) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode2_3_id.pdf) (25.0 MiB)
- [KLUB COBA-COBA - EPISODE 3 / KLUB JALAN-JALAN — Bahasa Indonesia (Indonesian) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode3_2_id.pdf) (24.2 MiB)
- [IL CLUB DEGLI ESPERIMENTI - EPISODIO 1 / LA MAPPA DELLE PAUSE — Italiano (Italian) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode1_3_it.pdf) (23.1 MiB)
- [IL CLUB DEGLI ESPERIMENTI - EPISODIO 2 / IL TAVOLO SI ALLARGA — Italiano (Italian) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode2_3_it.pdf) (23.9 MiB)
- [IL CLUB DEGLI ESPERIMENTI - EPISODIO 3 / IL CLUB IN TRASFERTA — Italiano (Italian) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode3_2_it.pdf) (22.3 MiB)
- [ためし部 第1話 ひと息マップ — 日本語 (Japanese) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode1_2_ja.pdf) (23.1 MiB)
- [ためし部 第2話 机、ひろがる。 — 日本語 (Japanese) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode2_2_ja.pdf) (23.9 MiB)
- [ためし部 第3話 出張、ためし部！ — 日本語 (Japanese) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode3_ja.pdf) (22.2 MiB)
- [해봄부 제1화 한숨 돌림 지도 — 한국어 (Korean) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode1_2_ko.pdf) (23.0 MiB)
- [해봄부 제2화 책상이 넓어지다 — 한국어 (Korean) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode2_2_ko.pdf) (23.8 MiB)
- [제3화 / 해봄부, 출장! — 한국어 (Korean) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode3_2_ko.pdf) (22.2 MiB)
- [प्रयोग क्लब - भाग 1 / आरामको नक्सा — नेपाली (Nepali) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode1_3_ne.pdf) (25.7 MiB)
- [प्रयोग क्लब - भाग 2 / टेबुल फराकिलो भयो — नेपाली (Nepali) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode2_3_ne.pdf) (26.6 MiB)
- [प्रयोग क्लब - भाग 3 / क्लबको पहिलो भ्रमण — नेपाली (Nepali) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode3_2_ne.pdf) (23.7 MiB)
- [O CLUBE DAS EXPERIÊNCIAS - EPISÓDIO 1 / MAPA DO DESCANSO — Português (Brasil) (Portuguese (Brazil)) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode1_3_pt.pdf) (23.1 MiB)
- [O CLUBE DAS EXPERIÊNCIAS - EPISÓDIO 2 / A MESA CRESCE — Português (Brasil) (Portuguese (Brazil)) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode2_3_pt.pdf) (23.9 MiB)
- [O CLUBE DAS EXPERIÊNCIAS - EPISÓDIO 3 / O CLUBE VAI À BIBLIOTECA — Português (Brasil) (Portuguese (Brazil)) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode3_2_pt.pdf) (22.3 MiB)
- [O CLUBE DAS EXPERIÊNCIAS - EPISÓDIO 1 / MAPA DO DESCANSO \[pt-PT\] — Português (Portugal) (Portuguese (Portugal)) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode1_3_pt_pt.pdf) (23.2 MiB)
- [O CLUBE DAS EXPERIÊNCIAS - EPISÓDIO 2 / A MESA CRESCE \[pt-PT\] — Português (Portugal) (Portuguese (Portugal)) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode2_3_pt_pt.pdf) (24.0 MiB)
- [O CLUBE DAS EXPERIÊNCIAS - EPISÓDIO 3 / O CLUBE VAI À BIBLIOTECA \[pt-PT\] — Português (Portugal) (Portuguese (Portugal)) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode3_2_pt_pt.pdf) (22.3 MiB)
- [EL CLUB DE LOS EXPERIMENTOS - EPISODIO 1 / MAPA DE DESCANSOS \[es-419\] — Español (Latinoamérica) (Spanish (Latin America)) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode1_3_es_419.pdf) (23.1 MiB)
- [EL CLUB DE LOS EXPERIMENTOS - EPISODIO 2 / LA MESA CRECE \[es-419\] — Español (Latinoamérica) (Spanish (Latin America)) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode2_3_es_419.pdf) (23.9 MiB)
- [EL CLUB DE LOS EXPERIMENTOS - EPISODIO 3 / DE SALIDA \[es-419\] — Español (Latinoamérica) (Spanish (Latin America)) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode3_2_es_419.pdf) (22.3 MiB)
- [EL CLUB DE LOS EXPERIMENTOS - EPISODIO 1 / MAPA DE DESCANSOS — Español (España) (Spanish (Spain)) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode1_3_es.pdf) (23.0 MiB)
- [EL CLUB DE LOS EXPERIMENTOS - EPISODIO 2 / LA MESA CRECE — Español (España) (Spanish (Spain)) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode2_3_es.pdf) (23.9 MiB)
- [EL CLUB DE LOS EXPERIMENTOS - EPISODIO 3 / DE SALIDA — Español (España) (Spanish (Spain)) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode3_2_es.pdf) (22.3 MiB)
- [CÂU LẠC BỘ THỬ NGHIỆM - TẬP 1 / BẢN ĐỒ CHỖ NGHỈ — Tiếng Việt (Vietnamese) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode1_3_vi.pdf) (24.8 MiB)
- [CÂU LẠC BỘ THỬ NGHIỆM - TẬP 2 / CHIẾC BÀN RỘNG HƠN — Tiếng Việt (Vietnamese) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode2_3_vi.pdf) (25.5 MiB)
- [CÂU LẠC BỘ THỬ NGHIỆM - TẬP 3 / CÂU LẠC BỘ ĐI CÔNG TÁC — Tiếng Việt (Vietnamese) — こまいろ日和 / Komairo Hiyori](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/download/sample-manga-v2/tameshibu_episode3_2_vi.pdf) (24.3 MiB)

### Sample PDF License (CC BY 4.0)

"The Try-It Club" Episodes 1–3
© 2026 Komairo Hiyori

Parts of this work for which the publisher holds copyright and other licensing rights are provided under the [Creative Commons Attribution 4.0 International License (CC BY 4.0)](https://creativecommons.org/licenses/by/4.0/).

When reusing this work, please display the work title, author name "Komairo Hiyori", the source of the original work, and a link to the license, and clearly indicate if any changes were made.

Generative AI was used in the production of text and images, as well as the translation of this work. Materials with rights held by third parties, such as fonts, are subject to their respective licenses. Nothing in this license constitutes a restriction on the use of material that is not protected by copyright or similar laws.

## Technical Stack

*   **UI**: Jetpack Compose (Material 3)
*   **Architecture**: MVVM (ViewModel, StateFlow)
*   **PDF Processing**: [PdfBox-Android](https://github.com/TomRoush/PdfBox-Android)
*   **Language**: Kotlin

## License

Apache License 2.0

## Release & Documentation Rules

Before building the App Bundle, ensure that `README.md`, `LICENSE`, `docs/*.html`, and `store_listing/metadata*.txt` are updated in accordance with the latest specifications. See [RELEASE_RULES.md](RELEASE_RULES.md) for details. Automated pre-bundle checks are enforced via Gradle (`preBundleDocumentationCheck`).

### Sample PDF distribution

The 51 sample manga PDFs are distributed through [GitHub Releases](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/tag/sample-manga-v2). Existing GitHub Pages PDF URLs remain available for older app versions. See [migration and verification notes](docs/SAMPLE_PDF_RELEASE_MIGRATION.ja.md).

[Sample v2 implementation and verification](docs/SAMPLE_PDF_V2_UPDATE.ja.md)

Sample list maintenance: run `python3 scripts/sample_public_catalog.py --write` to regenerate the lists from the published manifest and app language names. Run `python3 scripts/verify_sample_release_assets.py` to check every README and public index page, including missing lists, labels, sizes and order.
