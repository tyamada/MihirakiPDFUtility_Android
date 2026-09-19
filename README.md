# Mihiraki PDF Utility (Android)

見開き表示（Mihiraki）と直感的なページ編集機能を備えたAndroid用PDFユーティリティアプリです。
自炊した書籍の整理や、複数のPDFの結合・並び替えに最適です。

## 主な機能

- **見開き表示モード**: 2ページ並列での表示が可能。
- **右開き（RTL）対応**: 日本の書籍や漫画に合わせた「右から左」へのページ順序をサポート。
- **PDFの結合**: 複数のPDFファイルを1つにまとめることができます。
- **ドラッグ＆ドロップによる並び替え**: ページサムネイルを長押しして直感的に順序を入れ替えられます。
- **ページ編集**: 回転、削除、空白ページの挿入が可能。
- **パスワード保護対応**: パスワード付きPDFの読み込みと解除をサポート。
- **保存**: 編集した結果を新しいPDFとして保存。

## 技術スタック

- **UI**: Jetpack Compose (Material 3)
- **Architecture**: MVVM (ViewModel, StateFlow)
- **PDF処理**: [PdfBox-Android](https://github.com/TomRoush/PdfBox-Android)
- **言語**: Kotlin

## ライセンス

Apache License 2.0
