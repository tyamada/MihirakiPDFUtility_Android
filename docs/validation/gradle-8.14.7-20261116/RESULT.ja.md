# Gradle 8.14.7更新・検証（2026-11-16）

元ローカルmain（更新前HEAD `2ad114573a60780249bdc8c7138d673234e589a0`）で、既存の未コミット変更を保持したままGradleを8.14.5から8.14.7へ更新した。

## 変更

- `gradle-wrapper.properties` の配布URLを8.14.7へ変更し、公式配布ZIPの `distributionSha256Sum` を追加。
- Gradle 8.14.7自身のwrapperタスクでWrapperを再生成。JARを公式SHA-256と照合。起動スクリプトは従来の内容・改行コードを保持。
- 配布ZIP SHA-256: `903bfc4bc7622323124054c10f509246212a1902948bdaf26c379cca8735e856`
- Wrapper JAR SHA-256: `ad16bca46cb71bb5887c546860d93992aeee35c1fdaf4a242b0a612375d937e0`

チェックサム配布元: [ZIP](https://services.gradle.org/distributions/gradle-8.14.7-bin.zip.sha256)、[Wrapper JAR](https://services.gradle.org/distributions/gradle-8.14.7-wrapper.jar.sha256)。[公式リリースノート](https://docs.gradle.org/8.14.7/release-notes.html)。

## 検証結果

2026-11-16 05:57 JST、公式SHA-256を照合して展開したGradle 8.14.7の `bin/gradle` から、`--offline :app:assembleDebug :app:testDebugUnitTest :app:lintDebug` を実行。

- デバッグビルド成功。
- 単体テスト22件、失敗0・エラー0・スキップ0。
- Lintエラー0、警告7件。Gradle更新通知1件が解消し、更新前の8件から減少。残りは対象API警告1件、AndroidX/Core・Activity・Billing更新通知3件、Bouncy CastleのTLS警告3件。
- 更新前後のソース・未追跡ファイル・欠落状態をSHA-256で照合し、Wrapper2ファイル以外は一致。既存ステージ状態も保持。

この結果は別件の未コミット変更も含む元作業ツリーの結果であり、今回のコミットにその未コミット実装を含めるものではない。追加の端末インストール、実機試験、署名付きrelease AABビルド、Play Console操作は実施していない。検証済みGradle配布物を直接起動した結果であり、`./gradlew`による新規ダウンロードの動作試験とは区別する。

[機械可読の結果と残存警告一覧](VERIFICATION.json)。
