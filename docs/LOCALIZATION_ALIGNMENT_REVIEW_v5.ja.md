> **英語残留・空文字と検証結果を更新しました。** [v6報告書](LOCALIZATION_ALIGNMENT_REVIEW_v6.ja.md)が現在の記録です。以下は前回時点の結果です。

# Utility 不足翻訳補完・Lint検証結果 v5

## 今回の結果

マレー語199項目、香港繁体字215項目、計414項目のキー不足を補完した。さらにViewerと英語原文が一致する共通文言を照合して訳を再利用し、英語同文値の一部を修正した。追加・更新は合計600項目。変更キーはLOCALIZATION_COMPLETION_v5.jsonに記録。

既存の基本リソースにあった tools:ignore="MissingTranslation" と、利用者向け文言11項目の translatable=false を解除。翻訳ファイル側にもあった非翻訳指定を解除し、Untranslatable警告を解消した。Lintのベースラインや新たな警告抑制は追加していない。

## 全リソース構成の確認

基本言語302キー、翻訳15構成すべて302キー。不足・重複・書式引数不一致は0件。合計16構成は地域バリアントを含み、16種類の独立した言語を意味しない。

| 翻訳構成 | キー数 | 不足 | 英語原文と同文のキー数（校正候補） |
|---|---:|---:|---:|
| values-de | 302 | 0 | 45 |
| values-es | 302 | 0 | 42 |
| values-fr | 302 | 0 | 51 |
| values-in | 302 | 0 | 173 |
| values-it | 302 | 0 | 78 |
| values-ja | 302 | 0 | 26 |
| values-ko | 302 | 0 | 96 |
| values-ms | 302 | 0 | 2 |
| values-ne | 302 | 0 | 172 |
| values-pt | 302 | 0 | 172 |
| values-tl | 302 | 0 | 179 |
| values-vi | 302 | 0 | 173 |
| values-zh-rCN | 302 | 0 | 101 |
| values-zh-rHK | 302 | 0 | 2 |
| values-zh-rTW | 302 | 0 | 97 |

同文の値にはPDF、RTL、固有名詞、技術用語と、既存の未翻訳と思われるUIが含まれる。キー不足0件・Lint成功を全翻訳の内容校正完了とは扱わない。候補キーをJSONに列挙した。今回追加した訳の母語話者校正および全画面の目視確認は未実施。

## 今回実行した検証

1. 修正前：assembleDebugとlintDebugは成功していたが、MissingTranslation一括抑制とマレー語・香港繁体字の不足があったため、その成功だけを翻訳完了の根拠としない。
2. 修正後：`:app:assembleDebug :app:lintDebug :app:preBundleDocumentationCheck --offline` を実行、終了コード0、BUILD SUCCESSFUL。
3. Lint最終結果：エラー0件、警告88件、ヒント4件。MissingTranslation／Untranslatableの指摘は0件。
4. scripts/validate_localized_strings.py：XML構文、重複キー、全ロケールのキー不足、書式引数の番号・型を検査。16構成を対象とする。
5. scripts/validate_store_metadata.py：既存の全9原稿の文字数を確認。
6. git diff --checkで変更差分の空白を確認。

既存preBundleDocumentationCheckはrequiredLocalesが14構成で、マレー語・香港繁体字を対象に含んでいない。その範囲は変更せず、新規文字列検査では実在する全構成を検査した。単体テストは今回未実行（動作ロジックの変更なし）。

### Lintに残る警告・ヒント

- Hint / AutoboxingStateCreation: 4件
- Warning / AndroidGradlePluginVersion: 1件
- Warning / ConstantLocale: 1件
- Warning / GradleDependency: 3件
- Warning / IconDuplicates: 5件
- Warning / IconLauncherShape: 10件
- Warning / IconLocation: 4件
- Warning / ObsoleteSdkInt: 1件
- Warning / OldTargetApi: 1件
- Warning / PluralsCandidate: 1件
- Warning / TrustAllX509TrustManager: 3件
- Warning / TypographyEllipsis: 16件
- Warning / Typos: 1件
- Warning / UnusedResources: 34件
- Warning / UseKtx: 7件

検証レポート：app/build/reports/lint-results-debug.xml および同.html。今回の翻訳修正と無関係な既存コード・アイコン等の警告は残している。

## 変更範囲と残作業

今回の変更は翻訳リソース、再検証スクリプト、監査JSON、報告書。商品ID・課金処理・価格・購入権利・外部公開は変更していない。Console登録・既存公開状態・現行価格は未確認。今回の外部反映は実施なし。

- 英語同文候補の内容校正（機能語・固有名詞として妥当なものと未翻訳UIを区別）。
- 母語話者校正と、全言語の画面表示・改行・文言の確認。
- 画像・Web・ストアの完全照合、国別PRの根拠・指標の確認。
- 公開前のConsole読取・価格比較。

v4の未完了記録は履歴として維持し、今回のキー不足とLint状態は本書を参照すること。
