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

## TLS警告3件のrelease成果物での除去確認（2026-11-16）

06:13 JST、HEAD `85b9aa2c08ecd343b524cfdd4e8713af06730391` の元ローカルmainで、既存の未コミット変更を保持したまま、検証済みGradle 8.14.7から `--offline :app:assembleRelease` を実行。R8タスク `minifyReleaseWithR8` とrelease APK生成が成功した。

対象はPDFBox Android 2.0.27.0経由のBouncy Castle bcpkix-jdk15to18 1.72。警告は `JcaJceUtils$1` の空のcheckClientTrusted/checkServerTrustedと、`JcaJceUtils$2` の空のcheckClientTrustedの計3メソッド。

|対象クラス（org.bouncycastle.est.jcajce配下）|R8 usage.txtでクラス全体を除去|mapping.txtで残存・改名の記載|APK内全DEXでクラス定義・型参照|
|---|---|---|---|
|JcaJceUtils$1|あり|なし|なし|
|JcaJceUtils$2|あり|なし|なし|
|JcaJceUtils（親クラス）|あり|なし|なし|

APK内の全DEXをZIPから抽出してDEXヘッダー、type_ids、class_defsを解析し、元のクラス名だけでなくR8の改名情報とも照合した。今回のAPKはclasses.dex 1件・クラス定義5,583件。Bouncy Castle全体を依存から除外したわけではなく、別の351クラスは改名対応とDEXを照合して残存を確認した。

- APK: `app/build/outputs/apk/release/app-release.apk`、15,331,495 bytes
- APK SHA-256: `1c5cc4449ce897254c3712dfd70b1dfee9d663dd26518102e836b7003289348e`
- DEX SHA-256: `c411711b416e1f7d7bee17e158ef599cb149975241c0a1efca9b65566260a0e6`
- 成果物上の版は1.2.10（26）。配布済み版の差し替え・新規提出を意図した成果物ではない。

**判断:** 今回生成したrelease APKでは警告対象クラスが除去されており、この成果物について対象コードが実行されない根拠を確認した。Lintは縮小前の依存JARを検査するため、これによって警告3件自体が解消したわけではない。警告の一括抑制や依存除外は実施していない。今後、依存・keep設定・ビルド条件が変わる際は同じ確認を行う。TLS全般・依存全体に脆弱性がないことを証明する検査ではない。

検証前後でソース・未追跡ファイル・欠落状態・Git作業差分・ステージ状態が一致。端末導入・アップロード・Play Console操作は実施していない。この記録コミットにも検証時の別件の未コミット実装は含めない。ローカルに保持したR8記録・DEXクラス一覧・ビルドログのSHA-256、および詳細結果はVERIFICATION.jsonのtls_release_verificationに記録した。
