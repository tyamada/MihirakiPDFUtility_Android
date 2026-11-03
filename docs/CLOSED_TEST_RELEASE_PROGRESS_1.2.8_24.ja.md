# ユーティリティ1.2.8（24）：クローズドテスト配信状況

2026-11-03。1.2.7（23）の実機指摘を修正し、Releases配布URLを含めた更新版を準備中。

## 変更

- 暗号化PDFのパスワード入力・描画と、一時復号キャッシュの削除。
- 縦長画面の全メニューへのアクセス。
- サンプル言語名・英語名順・14言語×3話の42件。
- TalkBack標準実行からのプレビューと選択カスタム操作。
- 左右回転の取消・やり直しで角度と表示画像を復元。
- サンプル全42件の取得先を固定タグのGitHub Releasesへ変更。

## 検証と配信範囲

Debugビルド・単体7件・Debug Lintは成功（エラー0、警告12）。前段階の修正後Android回帰試験は14件成功。全42件のReleases実取得・SHA-256と、Android HTTPS転送試験は成功。旧Pagesの33件は保持し全URL HTTP 200。

署名付きApp Bundle再作成・最終検証・コミットとpush・CIとPages・Playへのアップロードと送信は、実施後に結果を追記する。Alphaの配信先178か国／地域と既存Googleグループ2件（BetaLobbyを含む）を維持する。製品版・価格・配信先は変更しない。

旧版の実機不合格を解消済みと断定せず、1.2.8（24）更新後の再確認を待つ。

[実機再確認手順](CLOSED_TEST_DEVICE_CHECKLIST_1.2.8_24.ja.md) ／ [Releases移行検証](SAMPLE_PDF_RELEASE_MIGRATION.ja.md)

## 署名付き成果物と公開main（2026-11-03）

- ソースコミット: `5f0b1ae12f4f3c1e23897a57ea9303ce3ecacdd3`（mainへpush済み）。
- `bundleRelease lintRelease assembleDebugAndroidTest`成功。Release Lintはエラー0・警告12。
- コミット対象だけの検証コピーでも単体7件、CIの文書・翻訳・スクリプト検査9種類が成功。
- bundletool構造検証・署名検証成功。packageは`com.takumayamada22.pdfutility`、versionNameは1.2.8、versionCodeは24。
- AAB SHA-256: `51359132ffec8abd6d279a215b0c7eed7a96aef208be58257c6483591e7a6434`。
- アップロード証明書SHA-256: `4D:1C:A7:13:4F:D3:9E:A3:98:D1:43:48:17:7D:F0:39:68:0F:99:93:05:FA:0D:CF:EC:89:86:8E:00:09:D4:01`（既存署名と一致）。
- AABから生成した署名付きAPK内のDEXに、Releasesの新URL42件を確認。
- そのAPKを専用エミュレーターへ上書き更新し、パスワード誤入力→正入力→描画、縦長メニューの応援到達・閉じる、スペイン語第3話の表示、右回転→取消→やり直し→再取消、標準アクセシビリティ実行からのプレビューと終了を確認。試験側の待機・スクロール・選択状態判定を修正して切り分け、アプリ本体への追加修正は不要だった。実機TalkBackの音声・操作確認は別途必要。
- [CI 41562968727](https://github.com/tyamada/MihirakiPDFUtility_Android/actions/runs/41562968727)成功。
- [Pages 41562967642](https://github.com/tyamada/MihirakiPDFUtility_Android/actions/runs/41562967642)成功。公開英語・日本語ページに新配布先を確認し、旧Pages33件のURLを維持。

## Play Console

Alpha用リリース15に24（1.2.8）をアップロード。R8マッピング添付、4言語のリリースノート、100%のクローズドテスト配信を設定。対応端末数の減少0。従来のネイティブライブラリのデバッグシンボル未添付警告1件。

送信対象がAlphaの更新1件だけであることを確認して審査送信操作を実施。Googleの事前チェック中で、正式な審査受付完了・テスターへの公開完了はまだ確認していない。管理対象の公開は既存どおりオフ。配信先178か国／地域とGoogleグループ2件は変更操作をしていない。製品版の変更は送信対象に含まれない。
