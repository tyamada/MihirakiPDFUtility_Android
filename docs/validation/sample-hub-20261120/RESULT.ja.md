# サンプル案内集約・PDF依存整理（2026-11-20）

対象はAndroid Utility（`tyamada/MihirakiPDFUtility_Android`）。既存の未コミット47ファイルは保持し、専用worktreeで今回の変更だけを準備した。インストール、端末試験、ストア操作、アプリの新規Release公開は実施していない。

## 利用者向け案内

- README英語は https://tyamada.github.io/manga-prompt-kit/samples/en/ 、日本語は https://tyamada.github.io/manga-prompt-kit/samples/ja/ へ接続。
- 利用者向けREADME2件とHTML44件を検査。各index/supportは対応する集約ページへ接続し、個別漫画PDFの一覧・ボタンを撤去。一般PDF・動画・アプリストアリンク・既存HTML IDは保持。
- 対応はja/en/ko/zh-Hans/zh-Hant/de/fr/es/pt/vi/fil/ne/id/itの14言語。tw/hkは繁体字の地域アンカーを使い、旧tl→fil、in→idを対応。マレー語は独立PDFがないので英語案内であることを明示。
- 公開ガイド全14言語をHTTPS取得して確認。自リポジトリのPages公開後照合は別の作業出力へ記録する。
- 更新手順： https://github.com/tyamada/manga-prompt-kit/blob/main/docs/sample-download-maintenance.md
- `scripts/sample_public_catalog.py`は集約リンクを検証し、旧51冊の一覧を再生成しない。機械用manifestとアプリの固定URL・ID・保存名は維持。

## 取得・試験入力

`scripts/sample_pdf_inputs.py`は固定版manifestから指定IDだけを解決する。外部の `--input-dir` または `MIHIRAKI_SAMPLE_PDF_DIR` を必須とし、通常実行はオフラインの容量・SHA-256・PDFヘッダー検証。明示した `--download` 時だけ取得する。リポジトリ内への保存を拒否し、既存不一致ファイルは上書き・削除しない。ep1/ep2_filの配布側tl名と旧ローカルfil名も維持。

例（このコマンドはPDF準備だけで、端末へインストール・転送しない）：

```sh
python3 scripts/sample_pdf_inputs.py --input-dir /absolute/external/cache --sample-id ep1_en --sample-id ep2_en --download
```

macOSのPythonでCAが不足する環境では、実在する正規CAバンドル `/etc/ssl/cert.pem` を `SSL_CERT_FILE` に指定して検証する。TLS検証は無効にしない。

撮影・転送・耐久試験の各スクリプトはこの検証済み入力を使う。漫画はアプリの `files/Documents/MihirakiSamplePdfs`、一般回帰PDFは`files/testdata`へ分離。同名端末ファイルはSHA一致時だけ再利用し、不一致なら保持して失敗。転送時も固有一時パスとno-clobberを使用する。

アプリ内耐久試験はSamplePdfRepositoryの検証済みキャッシュを利用し、未取得なら明示失敗。診断の漫画一括取得も同repositoryの検証・atomic保存・旧キャッシュを再利用する。ShortsRecordingTestは `-e sampleInputDir <device-directory>` とep1_en/ep2_enの実PDFを必須とし、元入力・コピー後の容量/SHA/ヘッダーを検証する。欠落時の白紙PDF代用は廃止した。

旧`cleanup_testdata.py`の再コピー・一括unlinkは廃止し、非破壊のエラー終了にした。別アプリを対象とする旧Viewer smoke／撮影スクリプトはUtilityで明示停止し、実行した試験や成功として扱わない。今回その端末用本体を実行していない。

## PDF棚卸しと削除ゲート

実際の元作業ツリーにある漫画PDFは84件・2,043,902,658 bytes。docs42件・1,021,951,329 bytes、testdata42件・同容量。56件が追跡済み、28件は無視／未追跡。最新51冊とSHA一致するのは18ファイル。詳細はPDF_INVENTORY.json。

**削除0件・0 bytes、保持84件・2,043,902,658 bytes。**

公開済み1.2.7（23）の固定ソース `df4389b37275d9ad7040a1516d1fb645a621cfd1` はUtility Pages `/sample/` の33冊（810,091,722 bytes）を参照する。公開AABとの対応は既存 `docs/CLOSED_TEST_RELEASE_PROGRESS_1.2.7_23.ja.md` に記録。最新31版が存在しても旧利用者の依存解消の証明にはならないため、旧33冊は維持する。残り9冊（211,859,607 bytes）は現行51冊とは異なる版。旧Release照合実績は `docs/SAMPLE_PDF_RELEASE_MIGRATION.ja.md` にあるが、今回その全原版の再取得／復元確認は実施しておらず削除を承認できる状態とは扱わない。

testdataの42冊もdocsのコピーと内容一致するが、今回は上記異版・無視ファイルを保護し、全て保持する。参照修正だけを理由に削除しない。将来は旧版利用方針とファイル単位の公開原版／保管コピーの現状・復元可能性を再確認して判断する。Git履歴改変、旧Release削除、git clean、一括rmは実施しない。

`PdfViewModel.kt`に残るViewer Pages `/testdata/`は一般負荷・検索PDF3件専用で、漫画URLではない。この経路・一般回帰PDF・SEASIDE_LIVING・同梱JSON・取得済み利用者PDFは対象外として保持した。

## 検証

- Debugビルド、単体57件（失敗・エラー・スキップ0）、Lintエラー0・既存警告12、AndroidTest APKコンパイル成功。
- 公開リンク、言語マッピング、機械用51冊manifest/fil保存名、翻訳・公開ページ・ストア資料の検証成功。
- 外部入力小fixture13件、公開リンク回帰6件、撮影安全性9件、画像レビュー2件ほか既存スクリプト試験が成功。画像レビュー試験はモック入力の独立試験で、全登録画像の目視成功とは扱わない。
- 実PDF統合は外部作業HDDのep1_enだけを取得・容量/SHA照合（24,171,133 bytes）、オフライン再利用を確認。全51冊の再ダウンロードや端末試験は今回実施していない。
- 既存47ファイル・全84漫画PDFをSHA-256で照合して保持。今回のソース修正は既に配信済み31版へ後付けされず、アプリへ反映するには将来の版上げ・配信工程が必要。

## CI環境の補完

最初の19a007bのCIはアプリのビルド・Lint・単体試験に成功したが、追加した画像fixture試験のPillowがrunnerに無く停止した。Python 3.12の専用環境とローカル検証済みPillow 12.2.0の明示依存を追加する。失敗した試験はスキップせず、再実行して確認する。
