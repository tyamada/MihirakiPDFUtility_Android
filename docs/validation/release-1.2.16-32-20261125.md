# 1.2.16（32）Alpha候補の検証（2026-11-25）

- 対象: 保存候補名のPDF拡張子重複と、ツールバー文字切れの修正。パッケージ `com.takumayamada22.pdfutility`、minSdk 26、targetSdk 36、非デバッグRelease。
- 実装: ファイル名修正 b8f4b0e、ツールバー修正 c393c37。保留中47ファイルはビルド元に含めず保持。
- 単体テスト63件成功、失敗・エラー・スキップ0。Debug/Release Lint各エラー0・警告12。翻訳、複数形、英語残留、掲載情報、公開ページ、サンプル配布、版情報の7チェック成功。
- AAB/APKの構造・署名検証に合格。現行アップロード鍵SHA-1 `73:37:6B:CF:9B:48:57:7F:F5:E9:7D:C5:3E:AA:09:A7:DE:68:9C:1E` と一致。AAB全889ペイロードの署名を読み取り検証。自己署名証明書・タイムスタンプ未付与等のJAR警告は署名検証成功とは分けて扱う。
- APK zipalign 16KB、AAB内ネイティブ4本の全LOADセグメント16KB整列に合格。TLS警告対象のBouncy Castle 3クラスはR8で全体除去され、mappingと全DEXの型参照に残存しない。
- AAB: `MihirakiPDFUtility-1.2.16-32-release.aab`、18,516,512 bytes、SHA-256 `f60efb1e4a0586e295277e7b0963d56482286ab6f1cf038f9489da3dbe868777`。
- APK: `MihirakiPDFUtility-1.2.16-32-release.apk`、15,387,811 bytes、SHA-256 `e9f22e269d7331e7f4fecd32fc0113dd7f66feacd29826bd048b1f1bdd0f56cd`。
- 実機根拠: [詳細](latest-device-results-20261125.md)参照。moto g24の別ID検証版で保存名・保存・再読込、Galaxy A54の別ID検証版でEnglish実画面と日英36構成の文字描画・行数・重なりの実機テストに合格。
- 限界: この署名済み32成果物自体のインストール・上書き更新、TalkBackの再試験、Xperia 10 VIでの修正版実機確認は未実施。Play版31の英語基本検証と修正後検証版の結果を混同しない。4言語ノートは今回の修正のみを記載し、ネイティブ話者の再校閲済みとは扱わない。
- この記録はビルド検証の結果。Alphaアップロード・審査受付・テスター公開の状態はPlay Consoleの確認結果で別途記録する。

成果物とログ: ローカル作業出力 `outputs/release-1.2.16-32-20261125/`。秘密情報はリポジトリへ保存していない。
