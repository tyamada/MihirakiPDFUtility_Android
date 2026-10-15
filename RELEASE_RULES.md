# App Bundle ビルド前ルール (Release & Documentation Rules)

App Bundle (`bundleRelease` 等) をビルドする前に、アプリケーションの最新の仕様・機能変更・バージョン情報に合わせて、以下のドキュメント、メタデータ、**多言語翻訳項目**、および**ヘルプ画面（使い方ガイド）**を必ず最新の状態に更新・検証してください。

## 📋 更新・確認対象項目

1. **ドキュメント・README**
   - [`README.md`](README.md) (英語版 README / 機能紹介 / サンプル等)
   - [`README_ja.md`](README_ja.md) (日本語版 README / 主な機能等)
   - [`LICENSE`](LICENSE) (ライセンス条項・著作権表示の確認)

2. **Webサイトドキュメント (`docs/*.html`)**
   - `docs/index.html` / `docs/index_ja.html` および各言語版（de, es, fr, in, ko, ne, pt, tl, vi, zh, zh_tw）
   - `docs/privacy.html` / `docs/privacy_ja.html` 等のプライバシーポリシー
   - `docs/support.html` / `docs/support_ja.html` 等のサポートページ

3. **ストア掲載情報 メタデータ (`store_listing/metadata*.txt`)**
   - [`store_listing/metadata_ja.txt`](store_listing/metadata_ja.txt) (日本語ストア用タイトル・短い説明・詳細説明)
   - [`store_listing/metadata_en.txt`](store_listing/metadata_en.txt) (英語ストア用タイトル・短い説明・詳細説明)
   - `docs/store_listing/metadata_ja.txt` / `docs/store_listing/metadata_en.txt`

4. **🌐 未翻訳項目の確認ルール (Translation Completeness Check)**
   - App Bundle ビルド前に、全対応言語（13言語）において、以下の未翻訳項目・プレースホルダーがないことを確認してください。
     - **アプリ内文字列リソース**: `app/src/main/res/values-*/strings.xml` に不足しているキーがないこと、`TODO` や未翻訳の項目がないこと。
     - **ストア説明文**: メタデータファイル（`metadata_*.txt`）の全言語版が最新機能に対応していること。
     - **Webページ**: `docs/*.html` の各言語版が正しく同期されていること。

5. **📖 ヘルプ画面・使い方ガイドの更新ルール (Help Screen Update Rule)**
   - App Bundle ビルド前に、アプリ内ヘルプ画面（`PdfHelpDialog` / `res/values/strings.xml` の `help_*` 文字列リソース）が、新しい機能（余白カット、オプション設定、診断機能、ローカル監査ログ、複数デバイス対応等）を含めた最新の仕様を正確に反映していることを確認・更新してください。

---

## ⚙️ 自動検証ルール (Gradle Task)

Gradleビルドシステムには、App Bundleのビルド前（`bundle` タスク実行時）に自動的に実行される検証タスク `preBundleDocumentationCheck` が組み込まれています。

- 必須ドキュメント、メタデータファイル、全13言語の翻訳リソース（`strings.xml`）、およびアプリ内ヘルプ画面の定義キーの存在と整合性を検証します。
- 更新漏れを防ぐためのビルド前チェックとして機能します。
