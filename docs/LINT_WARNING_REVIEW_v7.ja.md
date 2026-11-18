> 2026-11-02追記：この文書は過去のv7検証記録です。最新版1.2.7（23）の結果は[CLOSED_TEST_RELEASE_PROGRESS_1.2.7_23.ja.md](CLOSED_TEST_RELEASE_PROGRESS_1.2.7_23.ja.md)を参照してください。単体6件成功・Debug/Release Lint各エラー0／警告12に更新されています。

# Utility Lint警告の修正・最終検証 v7

## 結果

警告は75件から8件へ減少。Lintエラー0件、ビルドとLintは終了コード0で成功。警告抑制、Lintベースライン、依存ライブラリの除外による回避は行っていない。

## 実修正

- 実コードで参照されないことを照合した旧ヘルプ・旧UI文言と未使用ベクターを削除。ローカライズ先も同じキーを整理。
- 元のカラー画像データを変更せず、Androidのアダプティブ起動アイコンに使用する構成へ変更。重複した通常／丸型PNGを整理し、単色表示用ベクターを追加。実機ランチャー上の外観確認は未実施。
- バッジ画像をdrawable-nodpiへ移動し、暗黙の画素密度による拡大縮小を避ける。元の画像内容は変更なし。
- URI・色変換等を同じ意味のKTX APIへ置換。
- 件数を表示する文言をpluralsへ移行し、実際の件数で選択するよう呼び出しを修正。言語別の必要な数量カテゴリを定義。
- 三点リーダー表記を整備。
- Composeの数値状態をmutableIntStateOf／mutableFloatStateOfへ変更。
- 永続化ログの日時をLocale.ROOTで生成・解析し、端末の言語変更で日時形式が変わる問題を修正。ASCII数字と、言語切替後の解析を確認する2件の回帰テストを追加（既存のダミーテストを置換）。
- 旧画面の未使用文言を要求していた文書チェックを、現在実際に表示するヘルプ項目へ更新。マレー語・香港繁体字も含め全16構成を検査。
- 空のv26リソースディレクトリを整理。

## 最終検証

実行コマンド（Android Studio同梱JDK、オフライン）：

```text
:app:assembleDebug :app:lintDebug :app:preBundleDocumentationCheck --offline
```

- 上記の最終実行は成功。
- 単体テストは途中の検証で5件実行、失敗1件。詳細は下表。最後の微修正は数量カテゴリ・単色アイコン・未使用文言等で、テスト対象の動作ロジックはその後変更していない。
- ローカライズのキー不足・空文字・書式引数不一致は0件。
- 維持理由を明示したものを除く、原文同文の未分類英語候補は0件。
- 全言語のpluralsについてotherの存在と書式引数の一致を確認。数量カテゴリの適否はLintで確認。
- git diff --checkを確認。
- 証拠はdocs/validation/v7/。Lint詳細はapp/build/reports/lint-results-debug.xmlおよび同.html。

| テスト群 | 件数 | 失敗 | エラー |
|---|---:|---:|---:|
| com.takumayamada22.pdfutility.SamplePdfCatalogTest | 1 | 1 | 0 |
| com.takumayamada22.pdfutility.domain.PdfProcessorCancellationTest | 1 | 0 | 0 |
| com.takumayamada22.pdfutility.domain.PdfProcessorTest | 1 | 0 | 0 |
| com.takumayamada22.pdfutility.util.AppLoggerTest | 2 | 0 | 0 |

### 単体テストの未解消事項

SamplePdfCatalogTestはtameshibu_episode3_ko.pdfのtestdata側とdocs/sample側のSHA-256不一致により失敗。今回の作業ではPDF本体・カタログを変更していない。どちらを正として公開するか判断が必要なため、自動で上書きせず残した。ログ日時の新規回帰テスト2件とPDF処理系2件は通過。

- testdata側SHA-256: 4e0049cb3022a7bf6e8fdb249a75c16272bdb95436e682b66c7da8ca733363c5
- docs/sample側SHA-256: e48424dfae6175c8eff8da6474f9b0a4009362b36c64f77c1271448024ee3627

## 残る警告と判断

- AndroidGradlePluginVersion（Warning）: 1件。Gradle更新候補の通知。現在のAGPとの組み合わせでビルド検証済みであり、警告解消だけを目的とするビルド基盤の更新は行わない。
- GradleDependency（Warning）: 3件。core-ktx・activity-compose・Billingの更新候補。特にBillingのメジャー更新は購入・復元の回帰検証を伴うため、今回の警告修正では変更しない。
- OldTargetApi（Warning）: 1件。targetSdk更新の案内。対象API引き上げはOSの互換動作を変えるため、実機回帰検証を伴う別作業として残した。
- TrustAllX509TrustManager（Warning）: 3件。PDFBoxが取り込むBouncy Castle 1.72のEST補助クラスにある明示的なtrust-all機能／クライアント検証省略コード。アプリのHTTPS処理は標準HttpsURLConnectionを使用し、EST/JcaJceUtilsや独自TrustManagerの呼び出しは検索で見つからなかった。PDFBoxのclasses.jarにもESTへの直接参照は見つからなかった。依存ライブラリ全体の安全性を認定したものではない。警告は抑制せず残し、ライブラリ更新・到達可能性の評価を別途行う。暗号・署名・暗号化PDFの互換性を確認せずに依存関係を削除しない。

## 範囲と制約

公開・価格変更・コミット・pushは実施なし。UI／起動アイコンの全実機表示確認やOSターゲット移行試験は未実施。残件を隠すためのバージョン変更・警告抑制・テスト弱体化は行っていない。

参考：Android 12以降のバックアップ設定形式は[Android公式のAuto Backup説明](https://developer.android.com/identity/data/autobackup)を確認した。元のLint警告と実際のAPI呼び出し・リソース参照を照合して修正している。
