# Play Console 4言語掲載情報・画像 登録計画

対象: `com.takumayamada22.pdfutility`  
対象ロケール: 日本語 `ja-JP`、英語 `en-US`、韓国語 `ko-KR`、中国語（簡体字）`zh-CN`  
この文書は登録手順と実行状況です。2026-10-18時点で掲載情報・画像のPlay Consoleアップロードはまだ完了していません。

## 1. 現状と登録対象

| ロケール | 掲載情報 | フィーチャーグラフィック | 登録に使うスクリーンショット |
| --- | --- | --- | --- |
| `ja-JP` | `store_listing/metadata_ja.txt` | `store_listing/feature-graphic/feature-v2-ja.png` | `store_listing/screenshots/v7/` の `ja_` 21枚を再利用 |
| `en-US` | `store_listing/metadata_en.txt` | `store_listing/feature-graphic/feature-v2-en.png` | `store_listing/screenshots/v7/` の `en_` 21枚を再利用 |
| `ko-KR` | `store_listing/metadata_ko.txt` | `store_listing/feature-graphic/feature-v2-ko.png` | `store_listing/screenshots/v7/` の `ko_` 21枚を再利用 |
| `zh-CN` | `store_listing/metadata_zh-CN.txt` | `store_listing/feature-graphic/feature-v2-zh-CN.png` | `store_listing/screenshots/v7/` の `zh_cn_` 28枚を使用 |

メタデータと4種類のv2フィーチャーグラフィックは揃っている。フィーチャーグラフィックは全て1024×500pxのPNG。英語の短い説明は72文字に修正済み。韓国語の短い説明に「각 사이트별 백분율（各サイト別の割合）」という誤記らしき表現があるため、登録前の文面校正で「각 면별 백분율（各辺ごとの割合）」等の意図を確認する。

既存の繁体字 `zh-TW` 画像は `zh_tw_` 名のまま保管し、`zh-CN` として登録しない。`store_listing/screenshots/v7/` に簡体字のスマートフォン、7インチ、10インチ、Chromebook各7枚を追加撮影済み。4フォームファクターの接触シートを目視確認し、全112枚が自動検査と視覚レビュー済み（`review_store_screenshots.py`: missing 0, issues 0）。

## 2. 自動化の方式と範囲

Google Play Developer API v3の1つのEditに、4ロケールの掲載情報（タイトル、短い説明、詳細な説明）を全て `edits.listings.update` で更新し、各ロケールのフィーチャーグラフィックを `featureGraphic` としてアップロードする。同じEditに、電話 `phoneScreenshots`、7インチ `sevenInchScreenshots`、10インチ `tenInchScreenshots` のスクリーンショットも入れる。対象は掲載情報4件と画像88ファイル（フィーチャーグラフィック4枚、API対応スクリーンショット84枚）となる。

Play Console画面には明示的な「Desktop のアセット > デスクトップのスクリーンショット」欄があり、このアプリの掲載情報でも確認できた。登録条件は4～8枚、PNG/JPEG、1枚8MB以下、16:9または9:16、各辺1,080～7,680px。今回のChromebook画像は1920×1080なので寸法・比率条件に合う。Play ConsoleヘルプもChromebookを対応スクリーンショット端末として案内している。一方、現行Android Publisher APIの `AppImageType` には `desktopScreenshots` / Chromebook種別がない。このためDesktop画像だけはAPIアップロードできず、認証済みPlay Consoleの画面で言語を切り替えながらブラウザー自動操作で登録する。最後に各言語のDesktop欄と保存状態を画面で確認する。

画面確認時（2026-10-18）のデフォルト日本語掲載情報では、Desktopスクリーンショットが既に6/8枚表示されていた。これは当該欄に画像を登録できる実画面上の確認であり、今回のアップロードでは既存画像を置換・重複しないよう差分を確認する。

公式APIはEditの作成、ローカライズ掲載情報の更新、言語・画像種別を指定した画像アップロード、検証、Editの確定を提供している。画像アップロードには `androidpublisher` OAuth scope が必要。APIのEdit確定後、Play Console上で審査への送信状態を確認する。

## 3. 実装前の準備

1. 現在のPlay Console掲載情報と画像一覧を読み取り、4ロケールの有無、画像件数、画像URL・ハッシュ、現在の掲載文をバックアップする。今回差し替える端末区分と画像の扱い（既存全置換か追加か）を明示する。画像を全置換する場合、APIの `images.deleteall` は指定ロケール・画像種別の全画像を消すため、対象を限定し、取得済みの旧画像を復元できる形で保管する。
2. ローカルメタデータを掲載情報の3フィールドへ転記する。各言語についてタイトル30文字以下、短い説明80文字以下、詳細説明4000文字以下を検査する。見出し・説明ラベルは本文に含めない。韓国語の誤記「각 사이트별 백분율」を「각 면별 백분율」に修正済み。英語の短い説明は72文字。
3. 完了。`scripts/store_capture_config.json` の対象を `zh_cn: zh-CN` にし、28枚を `--skip-build --language zh_cn --resume` で追加撮影。日本語・英語・韓国語の84枚もハッシュと `visual-reviewed` を維持して再利用。
4. API対応画像の事前監査を拡張する。フィーチャーグラフィック4枚は1024×500、PNG、容量・色形式を検証。スクリーンショットは4ロケールそれぞれ電話・7インチ・10インチ各7枚、計84枚で、PNG/JPEG・解像度・縦横比・ロケール・ハッシュ・視覚レビューを検査する。大画面用は各端末区分4枚以上を確保する。Chromebookも4ロケール各7枚の実在・言語を確認する。
5. 現在のブラウザーはPlay Consoleにログイン済み。画像選択時にCodex Chrome拡張からローカルファイルアクセス許可が必要とのエラーが出たため、許可後にブラウザーUI経由で続行する。サービスアカウント認証情報は利用可能な状態で確認できていない。鍵やアクセストークンはコマンド出力・ログ・Gitへ出さない。

## 4. 登録・検証手順

1. **Dry run:** ロケール・掲載文・フィーチャーグラフィック・各スクリーンショットの順序とSHA-256を一覧にした差分レポートを生成する。Play Console現状との差分を確認し、変更対象を絞る。
2. **Editをステージ:** 新しいEditを作成し、4ロケールの掲載情報を更新する。各言語の `featureGraphic` と、電話・7インチ・10インチ画像を指定順でアップロードする。既存掲載情報があっても4件すべて明示的に更新し、現在のスクリプトの「未登録ロケールだけ追加」動作に依存しない。
3. **サーバー検証:** APIの `edits.validate` を実行し、同じEditから掲載情報と各 `imageType` の一覧を再取得する。ロケール数4、フィーチャーグラフィック4枚、API画像84枚、スクリーンショットの各端末区分7枚を照合する。APIが返す画像URLを使いプレビューも確認する。
4. **人による確認:** Editを確定する前に、4言語の掲載文・フィーチャーグラフィック・各端末区分の画像順をレビューする。日本語・韓国語・簡体字の文字崩れと、zh-CN画像が簡体字UIであることを重点確認する。
5. **Consoleへの反映:** 承認後にAPI Editをcommitする。続いてPlay Console掲載情報を各ロケールで開き、Desktop欄に各7枚のスクリーンショットを登録して保存する。ブラウザー操作ではロケールを切り替えるごとに選択言語とファイル名を照合し、同じ言語の画像を重ねて登録しない。API commitは掲載情報・API対応画像をConsoleへ反映する操作であり、アプリの新バージョン公開操作とは分離する。
6. **完了確認:** Play Console上で4ロケールそれぞれの掲載情報、フィーチャーグラフィック、電話・7インチ・10インチ画像を再確認する。Desktop欄も同様に確認し、保存後の一覧とローカルmanifestを記録する。審査・公開状態はConsoleで別途確認する。

## 5. 既存スクリプトの扱い

`scripts/play_console_screenshots.py` はスクリーンショットの監査・API登録の土台にはなるが、そのまま本件の登録には使わない。現状はローカル掲載文を存在しない `store_listing/PLAY_STORE_TRANSLATIONS.csv` から読む分岐があり、既存の4つの `metadata_*.txt` を更新用に解釈しない。フィーチャーグラフィック登録にも未対応。また `stage` は対象の画像種別を `deleteall` してから撮影画像を上げるため、既存画像との差分バックアップと置換範囲の確認が必要である。Desktop画像は現行APIに画像種別がないため、API manifestとは別にPlay Console UIで自動登録する。

実装では既存の認証・Edit・validate・commitの仕組みを再利用し、掲載情報の読み込み、featureGraphic、zh-CN素材、差分・バックアップ、Desktop UI工程を追加する。APIの確定はレビュー済みmanifestのSHA-256と明示的な実行フラグが一致した場合にのみ許可する。

## 6. 完了条件

- 4言語すべてでタイトル・短い説明・詳細説明が登録され、制限値を満たす。
- 4言語すべてのフィーチャーグラフィックが正しいロケールに登録される。
- 電話・7インチ・10インチ各区分に各言語7枚、計84枚の正しいロケール画像が並ぶ。
- Desktop欄に各言語7枚、計28枚が登録される。UI工程で各言語・枚数・保存結果を確認する。
- `zh-CN` には簡体字の撮影画像のみを登録し、既存の `zh-TW` 画像を流用しない。
- Play Console上で全項目を読み戻して照合し、審査・公開状況を記録する。

## 実行状況（2026-10-18）

- zh-CN の不足していた28枚を `zh-CN` システムロケールで再撮影し、スマートフォン、7インチ、10インチ、Chromebookの接触シートを目視確認した。
- `review_store_screenshots.py` の監査結果は112/112、視覚レビュー112/112、missing 0、issues 0。
- Android Publisher APIに送る84枚のmanifest監査は成功し、12のロケール・端末グループを確認。Desktop/Chromebook 28枚は別UI工程としてmanifestから除外されている。
- 4言語のローカルメタデータの文字数は全て上限内。英語短い説明は72文字。韓国語の「각 사이트별 백분율」を「각 면별 백분율」に修正した。
- Play Consoleの掲載情報編集画面を開き、Desktop欄と既存画像を確認した。ファイルアップロードはCodex Chrome拡張のローカルファイルアクセス設定が無効で止まっており、現在は画像や掲載文を変更・保存していない。設定有効化後、既存素材との差分を確認して登録を再開する。

## 公式資料

- [Android Publisher API: localized listings](https://developers.google.com/android-publisher/api-ref/rest/v3/edits.listings)
- [Android Publisher API: image upload](https://developers.google.com/android-publisher/api-ref/rest/v3/edits.images/upload)
- [Android Publisher API: supported image types](https://developers.google.com/android-publisher/api-ref/rest/v3/AppImageType)
- [Google Play: preview asset and screenshot requirements](https://support.google.com/googleplay/android-developer/answer/9866151?hl=en)
- [Google Play: store listing fields and character limits](https://support.google.com/googleplay/android-developer/answer/9859152?hl=en)
