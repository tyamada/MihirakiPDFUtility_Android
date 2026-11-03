# Utility：サンプル漫画v2の更新・検証結果

2026-11-03。現在開いているAndroid Studioのプロジェクト`MihirakiPDFUtility_Android`だけを変更した。git remoteは`https://github.com/tyamada/MihirakiPDFUtility_Android.git`。`RELEASE_RULES.md`を確認した。Viewerの追跡済みファイルは変更していない。

## 根拠資料とカタログ

公開済みの[Utility sample-manga-v2](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/tag/sample-manga-v2)から、manifest.json、android-handoff.csv、SHA256SUMS、public-verification.jsonを取得した。JSON・CSV・チェックサム・制作側の取得結果を全51件で照合し、資料原本を`docs/sample-releases/sample-manga-v2/`へ保存した。制作側の原本にある`android_qa: NOT_RUN`は変更せず、この作業のAndroid結果を別の記録として保持した。

- 全51冊：14言語・17言語地域区分×第1〜3話。
- 既存42作品のIDを維持。33件改訂、9件同一内容、9件地域版追加。
- ファイル名・作品名・正確なバイト数・表示サイズ・ページ数・SHA-256・固定URL・ロケールを資料から反映。
- Utilityのフィリピン語は第1・2話`_fil.pdf`、第3話`_tl.pdf`、言語キー`fil`を維持。Viewerの名前を流用していない。
- es_419・pt_pt・zh_hkを追加。スペイン／中南米、ブラジル／ポルトガル、台湾／香港を区別し、香港は標準書面語と明示。口語広東語版として扱わない。
- 言語リストと同じ話数内の並びは英語名順。第1話順／最新話順の選択と保持を維持。端末ロケールも各地域へ対応。
- README・14の公開サンプル案内ページを51件へ更新。PDFリンクは計816件で、各ページに全51件の重複なし掲載を検証。
- 作者・CC BY 4.0・第三者素材の条件を維持。旧PagesのPDF、旧Releases、既存URLは削除・変更していない。v2 PDFをdocs/sample/・testdata/へ追加していない。

## キャッシュと取得

新規取得の保存先は`MihirakiSamplePdfs/verified/<期待SHA-256>/<ファイル名>`。既存ルートのPDFは名前だけで取得済みにせず、期待サイズ・PDFヘッダー・SHA-256が一致したものだけを再利用する。同一内容の継続版はオフラインでも使える。旧版や無関係な利用者ファイルを自動削除しない。

ネットワーク取得はHTTPSのみ、転送回数とタイムアウトを制限。試行ごとの一時ファイルを使い、サイズ・PDFヘッダー・SHA-256を検証してから同一ディレクトリのrenameで公開する。不一致を期待値の書き換えで回避しない。失敗・キャンセル時に削除するのは当該試行の一時ファイルだけ。明示削除は管理対象の当該版だけに限定し、旧ルートのPDFを消さずに再表示を防ぐマーカーを使用する。

一覧のハッシュ検証はIO上で一覧更新時に実行し、進捗更新ごとの全ファイル再読込を避ける。PDFを開く直前には再度完全検証し、表示後の破損も拒否する。整合性エラーは全16リソース区分へ翻訳した。

## 実際の検証結果

|確認|結果|
|---|---|
|公開PDFの匿名取得|全51件成功。HTTPS転送・最終HTTP 200・PDF実体・サイズ・SHA-256一致|
|PDF本体の保持|QA後も全51件のハッシュ一致。公開PDFを編集・差し替えていない|
|Debug・Release Bundleビルド|成功。R8有効の成果物内にv2 URL51件を確認|
|単体テスト|17件成功、失敗・エラー0。キャッシュの9試験を含む|
|Debug／Release Lint|それぞれエラー0・警告12。警告抑制追加なし|
|文書・翻訳・ストア原稿・スクリプト|9種類の検査が成功|
|17地域の絞り込み・ALL|エミュレーターで各3件、ALL51件、ID・ラベル・順序を確認|
|並び替え|最終APKで最新話順／第1話順と各話内の英語名順を確認|
|Android PDF描画|全51冊408ページをアプリのThumbnailProviderで描画。ページ数・ページインデックスを検証|
|Android PDF文字抽出・検索|408ページすべて抽出文字あり。51冊の表紙で言語別の指定語検索成功。語・ページ・結果はandroid-pdf-qa.jsonに記録|
|新3地域の画面|中南米スペイン語・ポルトガル・香港の第1話で表紙、ページ順、LTR／RTL見開き配置を確認。ライセンス確認はキャンセルし、同意操作なし|
|コピー|新3地域の表紙抽出文字列をAndroidクリップボードへ転送し、全文一致を確認|
|実際のAndroid HTTPS取得|香港第3話で新規取得・検証済みデータのオフライン再利用・破損検出・再取得・旧ファイル保持が成功|

端末試験は`emulator-5554`、sdk_gphone16k_arm64、Android 17／API 37で実施。PDF全件検証とフィルターの2試験、見開き・コピー・実通信の各試験を実施した。最終ソースの追加順序試験も成功。実通信試験は`sampleV2Network=true`を指定した場合だけ動く。通常の端末試験では外部通信を必須にしない。

単体では新規取得、旧版取得済み、同一内容の旧版再利用、オフライン失敗と再試行、同一サイズ・同一mtimeでの破損、切断・不正データ・キャンセル、明示削除後の再取得、期待ハッシュ変更後の旧版保持を確認した。Androidのオフライン状況は通信実装を失敗させる方法で再現し、端末全体のネットワーク設定は変更していない。

## 検証の範囲と未実施

- 物理実機、Android 17以外のOS、Playからの上書き更新は未実施。
- UtilityにはPDF本文の検索・文字選択コピーのUIがないため、そのUI操作は未実施。PDFBoxによる指定語検索・文字抽出とAndroidクリップボード転送の結果を、UI機能の合格と混同しない。
- 全408ページの描画成功を確認したが、全ページの細部の目視校正や全言語の翻訳校正までは実施していない。目視は51冊の表紙一覧、新3地域の代表画面・本文ページ一覧等。
- 検証した検索語・ページ・表示範囲では、制作側へ修正を依頼すべきPDFの問題は見つかっていない。任意の検索語すべてを保証するものではない。

## 再検証

```sh
python3 scripts/verify_sample_release_assets.py
python3 scripts/verify_sample_release_assets.py --remote --download-dir /private/tmp/utility-v2-pdfs --output /private/tmp/utility-v2-verification.json
./gradlew assembleDebug testDebugUnitTest lintDebug assembleDebugAndroidTest bundleRelease lintRelease
```

端末に公開PDFを試験用フォルダーへ転送したうえで、`SamplePdfFilterTest`、`SamplePdfV2DeviceTest`、`SamplePdfV2LayoutTest`へ`samplePdfFixtureDir`を指定して実行する。実通信を確認する`SamplePdfV2NetworkTest`には`sampleV2Network=true`を指定する。

## 配信に残る作業

今回はコミット・push、GitHub Pagesのデプロイ、Play Console操作、アプリ公開・配信を実施していない。配信国と価格も変更していない。

版番号は既存1.2.8（24）のまま。今回のAABは検証用で、配信時には未使用のversionCodeと適切なversionNameへ更新し、リリースノート、署名付き成果物、実機での旧版からの上書き更新と保持・再取得を確認する必要がある。

検証用AAB SHA-256: `7062fb2e2231d492ad4f5e280a654a910e1908757040b058c9ecfd247d4b9b95`。

## 後続リリース準備

2026-11-03、ユーザーの公開指示により1.2.9（25）として配信準備を開始。[最新の配信記録](CLOSED_TEST_RELEASE_PROGRESS_1.2.9_25.ja.md)を参照。上記の1.2.8（24）は実装検証時点の履歴。
