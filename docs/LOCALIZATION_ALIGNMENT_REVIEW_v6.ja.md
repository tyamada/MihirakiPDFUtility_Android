> **Lint警告を修正しました。** [v7の警告修正・検証結果](LINT_WARNING_REVIEW_v7.ja.md)が現在のLint状態です。以下は前回時点の翻訳・検証記録です。

# Utility 英語残留・空文字の是正と検証結果 v6

## 実施結果

英語原文と同文だった項目のうち1367項目を現地語へ修正。固有名詞・技術用語・現地語でも同じ綴りが一般的な42項目は理由を記録して維持した。例：著作権者名、PDF、RTL、ContentResolver、ドイツ語のVersion、スペイン語のErrorなど。単に英字を含むという理由で固有名詞や技術語を変えていない。

空文字だった89項目も補完した。キーの存在だけでは空文字を検出できなかったため、文字列検査に空値の検出を追加した。旧版の英語説明・部分的に英語の説明も照合し、現行の基本文言に合わせて修正した。上記件数は異なる確認軸であり、合算して全変更件数とはしない。

| リソース構成 | キー数 | 原文同文から翻訳した項目 | 維持した同文項目 | 未分類英語候補 | 空文字 |
|---|---:|---:|---:|---:|---:|
| values-de | 302 | 40 | 5 | 0 | 0 |
| values-es | 302 | 39 | 3 | 0 | 0 |
| values-fr | 302 | 45 | 6 | 0 | 0 |
| values-in | 302 | 171 | 2 | 0 | 0 |
| values-it | 302 | 73 | 5 | 0 | 0 |
| values-ja | 302 | 25 | 1 | 0 | 0 |
| values-ko | 302 | 94 | 2 | 0 | 0 |
| values-ms | 302 | 0 | 2 | 0 | 0 |
| values-ne | 302 | 170 | 2 | 0 | 0 |
| values-pt | 302 | 170 | 2 | 0 | 0 |
| values-tl | 302 | 175 | 4 | 0 | 0 |
| values-vi | 302 | 171 | 2 | 0 | 0 |
| values-zh-rCN | 302 | 99 | 2 | 0 | 0 |
| values-zh-rHK | 302 | 0 | 2 | 0 | 0 |
| values-zh-rTW | 302 | 95 | 2 | 0 | 0 |

## 訳文の作成・確認方法

既存の同一原文の訳、こちらでの翻訳、ユーザーが明示許可したGoogle翻訳の候補を使用。外部へ送ったのはUI文言のみで、PDF・アプリログ・認証情報は送っていない。候補で固有名詞・書式引数・タグの欠落があったものはそのまま採用せず、小分けで再取得し、引数照合と手修正を行った。GitHub自体の問題を報告するように誤読された表現、フィリピン語の表示名、部分英語の注釈などを修正。

ENGLISH_RESIDUE_AUDIT_v6.jsonに変更前後、維持理由、技術的な借用語を含む文の確認記録を保存した。言語判定は補助検査であり、母語話者校正や全画面の目視確認の代わりとはしない。

## 今回の最終検証

- ビルド・Lint：終了コード0、BUILD SUCCESSFUL。
- Lint：エラー0件、警告75件、ヒント4件。未翻訳警告を隠す指定やベースラインは追加していない。
- scripts/validate_localized_strings.py：XML、重複キー、キー不足、空文字、書式引数の番号・型を確認し通過。
- scripts/audit_english_residue.py：明示した維持項目を除く原文同文候補0件、空文字0件。
- ローカル言語判定による長い英語文の候補0件。原文と異なる旧版英語を検出する補助として実施。
- scripts/validate_store_metadata.py：全原稿の文字数検査を通過。ストア原稿は今回変更していない。
- git diff --check：今回の変更差分について確認。

実行コマンド：

```text
:app:assembleDebug :app:lintDebug :app:preBundleDocumentationCheck --offline
```

Android Studio同梱JDKを使用。Lint詳細はapp/build/reports/lint-results-debug.xmlおよび同.html。今回のテキスト検査とビルドの結果はdocs/validation/v6/に保存。

### 残るLint警告・ヒント

- AutoboxingStateCreation (Hint): 4件
- AndroidGradlePluginVersion (Warning): 1件
- ConstantLocale (Warning): 1件
- GradleDependency (Warning): 3件
- IconDuplicates (Warning): 5件
- IconLauncherShape (Warning): 10件
- IconLocation (Warning): 4件
- ObsoleteSdkInt (Warning): 1件
- OldTargetApi (Warning): 1件
- PluralsCandidate (Warning): 1件
- TrustAllX509TrustManager (Warning): 3件
- TypographyEllipsis (Warning): 4件
- UnusedResources (Warning): 34件
- UseKtx (Warning): 7件

## 限界と残作業

- 母語話者校正、全端末・全画面の表示確認は未実施。自動検査の候補0件を、すべての訳の自然さ・意味の完全性の認定とはしない。
- 技術的な借用語は現地語の文中にも残る（例：download、stress test等）。その用法を英語の未翻訳文とは区別した。
- 既存コード・アイコン等の警告は残る。今回の範囲外の動作変更は行っていない。単体テストは今回未実行。
- 国別PRの根拠調査、Web・画像・ストアの完全照合は別途。プライバシー文面の翻訳は実装監査や法令適合認定を意味しない。
- Consoleの既存登録・公開状態・現行価格は未確認。今回の外部公開・コミット・push・価格変更・課金処理変更は実施なし。

v5以前は当時の記録であり、英語残留・空文字・Lintの現状は本書と監査JSONを参照すること。
