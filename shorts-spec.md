# YouTube Shorts 自動生成タスク仕様書 (MihirakiPDFUtility)

## 目的
Androidエミュレータ上で `MihirakiPDFUtility` の編集動作を自動操作しながら録画（`adb screenrecord`）し、MacのTTS（`say`）による英語ナレーションとテロップをFFmpegで合成して、YouTube Shorts用動画（全5シーン）を完全自動生成する。

## プロジェクト前提
- 本アプリは「マンガ・自炊書籍向け見開きPDF編集ツール」。
- PDFを開いた直後は「サムネイル画像の一覧（グリッド）」が表示される。
- サンプルPDFには分割可能な横長結合画像や余分な空白ページはないため、通常ページを対象に操作する。

## UI要素の特定ルール
`app/src/main/` 配下のソースコード（Compose / Layout / Menu）を解析し、以下の要素を特定して操作すること：
1. サムネイル一覧と見開き表示の切り替えボタン
2. ページ編集コマンド（回転、白紙挿入、削除）のボタン／メニュー
3. ツールバーカスタマイズのトリガーおよび操作領域
4. 保存／エクスポートボタンおよび確定ボタン

---

## シーン別タイムシート・ナレーション・テロップ定義

### Scene 1: サムネイル一覧からの見開き表示 (目標尺: 15秒)
- **Voiceover**: "Meet Mihiraki PDF Utility—the dedicated editor designed specifically for scanned books and manga. Preview your layout in authentic two-page spreads while you edit. Getting started is completely effortless."
- **タイムテーブル**:
  - `0.0s - 2.5s`: サムネイル一覧表示（初期状態）で待機
  - `2.5s - 5.5s`: 表示切り替えボタンをタップして「見開きプレビュー」へ移行
  - `5.5s - 15.0s`: 左右2ページが並んだ見開き状態を維持（プレビュー確認）
- **テロップ (drawtext)**:
  - `0.0s - 5.0s`: "Mihiraki PDF Utility: Manga PDF Editor"
  - `5.0s - 15.0s`: "Authentic Two-Page Spread Preview"

### Scene 2: PDF読み込みとメニュー操作 (目標尺: 15秒)
- **Voiceover**: "Import your scanned files in a snap. The streamlined interface keeps powerful tools right within reach. Access essential edit actions instantly, making adjustments quick and painless even on mobile."
- **タイムテーブル**:
  - `0.0s - 3.5s`: ファイル選択・インポート操作を実行
  - `3.5s - 7.5s`: サムネイル一覧が瞬時に展開される
  - `7.5s - 15.0s`: ツールバー／アクションメニューを展開し、編集コマンドへアクセスする様子を見せる
- **テロップ (drawtext)**:
  - `0.0s - 6.0s`: "Instant File Import"
  - `6.0s - 15.0s`: "Fast & Intuitive Menu Controls"

### Scene 3: ページ編集コア機能 (目標尺: 20秒 ※ハイライト)
- **Voiceover**: "Fix imperfect scans with precision. Rotate misoriented pages, split wide scans into single leaves, and insert blank pages to re-align your spreads perfectly. Need to clean up? Remove unwanted pages with a single tap."
- **タイムテーブル**:
  - `0.0s - 4.5s`: ページ選択 →「回転」を実行
  - `4.5s - 9.0s`: 分割メニューの表示または安全な分割アクションの提示
  - `9.0s - 14.5s`: 「白紙挿入」を実行し、見開きの左右位置を調整（★最重要見せ場）
  - `14.5s - 20.0s`: ページ選択 →「削除」を実行
- **テロップ (drawtext)**:
  - `0.0s - 4.5s`: "1. Rotate Orientation"
  - `4.5s - 9.0s`: "2. Split Wide Spreads"
  - `9.0s - 14.5s`: "3. Insert Blank (Fix Spread Alignment!)"
  - `14.5s - 20.0s`: "4. Delete Pages"

### Scene 4: 表示切替とツールバーカスタム (目標尺: 15秒)
- **Voiceover**: "Switch between a comprehensive grid view to manage whole chapters and spread preview to inspect every detail. You can even customize your toolbar to keep your most-used editing features exactly where you want them."
- **タイムテーブル**:
  - `0.0s - 5.5s`: サムネイル一覧と見開き表示の切り替えを実行
  - `5.5s - 15.0s`: ツールバーカスタマイズUIを開き、アイコンの表示／配置操作を行う
- **テロップ (drawtext)**:
  - `0.0s - 6.5s`: "Grid View vs Spread Preview"
  - `6.5s - 15.0s`: "Fully Customizable Toolbar"

### Scene 5: 保存・エクスポート (目標尺: 17秒)
- **Voiceover**: "Export your newly organized PDF instantly with zero quality loss. Best of all, all processing happens entirely on your device—no cloud uploads, no account required, complete privacy. Download Mihiraki PDF Utility on Google Play today!"
- **タイムテーブル**:
  - `0.0s - 3.5s`: 「エクスポート / 保存」ボタンをタップ
  - `3.5s - 8.0s`: 保存確定ダイアログを操作・保存実行
  - `8.0s - 17.0s`: 完了表示を確認し、ローカル処理完了の状態で待機
- **テロップ (drawtext)**:
  - `0.0s - 6.5s`: "Instant Lossless Export"
  - `6.5s - 12.0s`: "100% Offline & Private"
  - `12.0s - 17.0s`: "Get It on Google Play!"