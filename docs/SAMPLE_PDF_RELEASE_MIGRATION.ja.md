# サンプルPDFのGitHub Releases移行・検証記録

検証日: 2026-11-03（日本時間）

## 実施結果

- ユーティリティ: [sample-manga-v1](https://github.com/tyamada/MihirakiPDFUtility_Android/releases/tag/sample-manga-v1)をPDF配布用に公開。
- 14言語×3話、PDF42件（1,021,951,329 bytes）と`SHA256SUMS`を添付。
- 全42件を認証なしでダウンロードし、HTTP 200、PDF形式、サイズ、SHA-256がローカルの元PDFと一致。
- 公開された`SHA256SUMS`もローカルの一覧と完全一致。
- カタログの全URLを固定タグ付きReleases URLへ変更。サンプルID・ファイル名は維持し、ダウンロード済みPDFの保存先は変更しない。
- READMEと公開HTMLの漫画PDFリンクをReleasesへ変更。作者・既存ライセンスの表示を維持し、Releasesにも明記。
- CIに`python3 scripts/verify_sample_release_assets.py`を追加。ローカルPDFのハッシュ・カタログURL・公開ページのPDFリンク先を検証。通常CIではネットワーク取得しない。

## 旧アプリの互換性

公開mainにある旧Pages用漫画PDFは33件ともバイト単位で変更なし。既存Pages URLは全件HTTP 200を確認。`testdata/`の追跡解除、PDF削除、履歴書き換えは実施していない。

旧PagesのPDFは保持するため、この準備段階では既存PDFによるPages公開容量は減らない。既存アプリは引き続きPagesを使用し、新しいURLを組み込んだアプリを配布した後も、旧版の取り扱いを決めるまで既存URLを維持する。

## テスト結果

- `./gradlew assembleDebug testDebugUnitTest lintDebug`: 成功。
- 単体テスト7件、失敗0、エラー0。
- Debug Lint: エラー0、警告12。警告を隠す設定変更なし。
- Androidエミュレーターの標準`HttpsURLConnection`で、両リポジトリの英語第1話を実ダウンロード。転送後HTTP 200・HTTPS・サイズ・SHA-256一致（1テスト内で2件検証）。アプリ本体のUI操作試験とは別の通信互換性試験。
- 公開ページのローカル参照検証: エラー0。外部PDFリンクのファイル名・配布先は専用スクリプトで検証し、その配布先の全42件は実ダウンロード検証済み。

## 再検証

```sh
python3 scripts/verify_sample_release_assets.py
python3 scripts/verify_sample_release_assets.py --remote --output sample-release-verification.json
python3 scripts/validate_public_pages.py
./gradlew assembleDebug testDebugUnitTest lintDebug
```

`--remote`は約975 MiBをダウンロードし、一時ファイルを照合後に削除する。認証情報は使用しない。固定タグのPDF内容は差し替えず、内容更新時は新しいタグとチェックサムを作成する。

## 今後のアプリ配信時の確認

1. リンク変更を含むソース・文書・CI変更と必要な新規ファイルをコミットする。コミット・push後のCI・Pages結果は配信状況報告書に記録する。
2. 新URLを含む署名付きApp Bundleを再作成する。この変更より前に作成したBundleには新URLは含まれない。
3. 新しいアプリで言語切替・未取得PDFのダウンロード・進捗・表示・取得済みファイルの再表示を確認する。
4. 旧版アプリでもPagesから取得できることを確認する。

PDF配布用Releasesは公開済み。アプリ本体のPlay配信・公開ページのデプロイは、この作業では実施していない。

## ユーティリティ固有の注意

既存Pages用33件は保持。新たな第3話9件は公開済み旧版の参照対象ではなく、`docs/_config.yml`でPages公開対象から除外し、Releasesで配布する。暫定的なraw.githubusercontent.com配布案は廃止し、全42件をReleasesへ統一した。9件のローカルPDF・チェックサム・設定・検証スクリプトをコミット対象に含める必要がある。その他の未追跡テストPDFはこの移行の対象外。
