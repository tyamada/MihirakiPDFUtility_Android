# Mihiraki PDF Utility (Android)

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

A powerful and intuitive PDF utility for Android, specializing in **Mihiraki (two-page spread view)**. It is optimized for managing scanned books, manga, and professional documents.

[**日本語サイト**](https://tyamada.github.io/MihirakiPDFUtility_Android/) | [**English Website**](https://tyamada.github.io/MihirakiPDFUtility_Android/index_en.html) | [**日本語 README (README_ja.md)**](README_ja.md)

## 🚀 Key Features

| Feature | Description |
| :--- | :--- |
| 📖 **Mihiraki View** | View two pages side-by-side. Full support for Right-to-Left (RTL) reading directions for manga and Japanese books. |
| ✂️ **Smart Splitting** | Split double-page spreads into individual pages. Supports vertical and horizontal splitting. |
| 🔀 **Drag & Drop Reordering** | Easily organize your PDF by dragging and dropping thumbnails. Intuitive page management. |
| ➕ **Merge & Append** | Combine multiple PDF files into one, or add pages to existing documents. |
| 🔒 **Security** | Handle encrypted PDFs with password protection and metadata editing. |
| 🔍 **Diagnostics & Logging** | Run quick in-app diagnostic tests and view local app audit logs (24-hour retention, privacy-protected with no external transmission, shareable). |
| 🖥️ **Cross-Device Support** | Optimized for Smartphones, Tablets, and ChromeOS (desktop experience). |

### Feature Showcase

<img src="store_listing/MihirakiPDFUtility-feature-graphic.png" alt="Feature Showcase" width="100%" style="margin: 20px 0; border-radius: 8px;">

### Visual Examples

Explore how the application functions across different device types:

| Device | View | Image |
| :--- | :--- | :--- |
| **Smartphone** | Spread View | <img src="store_listing/screenshots/smartphone/2_mihiraki_view.png" width="300" height="auto" style="margin: 5px;"> |
| **Tablet** | Smart Splitting | <img src="store_listing/screenshots/tablet_10/1_main_grid.png" width="300" height="auto" style="margin: 5px;"> |
| **ChromeOS** | Desktop Interface | <img src="store_listing/screenshots/chromebook/6_support_diarlog.png" width="300" height="auto" style="margin: 5px;"> |

## 🌐 Multi-language Support

13 languages supported, including Japanese, English, Korean, Chinese, German, French, Spanish, Portuguese, Vietnamese, Filipino, Nepali, and Indonesian.

## Sample PDFs

You can download and test sample PDFs (Manga) to try out Mihiraki PDF Utility:

- [THE TRY-IT CLUB EPISODE 1: THE BREAK-TIME MAP (English)](https://github.com/tyamada/MihirakiPDFUtility_Android/raw/main/testdata/tameshibu_episode1_en.pdf)
- [THE TRY-IT CLUB EPISODE 2: ROOM TO GROW (English)](https://github.com/tyamada/MihirakiPDFUtility_Android/raw/main/testdata/tameshibu_episode2_en.pdf)
- [ためし部 第１話 ひと息マップ (Japanese)](https://github.com/tyamada/MihirakiPDFUtility_Android/raw/main/testdata/tameshibu_episode1_ja.pdf)
- [ためし部 第２話 机、ひろがる。 (Japanese)](https://github.com/tyamada/MihirakiPDFUtility_Android/raw/main/testdata/tameshibu_episode2_ja.pdf)
- [해봄부 제1화 한숨 돌림 지도 (Korean)](https://github.com/tyamada/MihirakiPDFUtility_Android/raw/main/testdata/tameshibu_episode1_ko.pdf)
- [해봄부제2화 책상이 넓어지다 (Korean)](https://github.com/tyamada/MihirakiPDFUtility_Android/raw/main/testdata/tameshibu_episode2_ko.pdf)
- [试试社 第1话 歇口气地图 (Chinese (Simplified))](https://github.com/tyamada/MihirakiPDFUtility_Android/raw/main/testdata/tameshibu_episode1_zh_cn.pdf)
- [试试社 第2话 桌子变大了 (Chinese (Simplified))](https://github.com/tyamada/MihirakiPDFUtility_Android/raw/main/testdata/tameshibu_episode2_zh_cn.pdf)

## Technical Stack

*   **UI**: Jetpack Compose (Material 3)
*   **Architecture**: MVVM (ViewModel, StateFlow)
*   **PDF Processing**: [PdfBox-Android](https://github.com/TomRoush/PdfBox-Android)
*   **Language**: Kotlin

## License

Apache License 2.0
