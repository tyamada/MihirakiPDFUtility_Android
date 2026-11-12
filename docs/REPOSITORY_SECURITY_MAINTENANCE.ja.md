# 公開リポジトリの保守・セキュリティ対策

2026-11-03

- 生成キャッシュ、過去のコンパイラエラー、動画確認用中間画像はGitに登録しません。既存のテスト用PDFと公開旧URLは維持します。
- READMEにSDK/JDK、Debugビルド・インストール、独自のRelease鍵の作成方法を記載しています。
- アプリ起動時、前回のプロセス終了で残ったアプリ所有の一時PDFを削除します。利用者の選択した原本やダウンロード済みサンプルは削除しません。起動前や削除失敗時まで平文残留を完全に防ぐ方式ではありません。
- Utilityでは文書の保存場所を固定カテゴリで記録し、フォルダ名・任意のprovider名・例外メッセージ・スタックトレースをアプリログに含めないよう修正しました。初回更新起動時に旧ログを削除します。OSや外部ライブラリが独自に出力するログまで保証するものではありません。
- スクリプト`validate_public_pages.py`は、公開HTMLに加えて英日READMEのローカルリンクと画像参照を検証します。

## 依存ライブラリのLint警告

PDFBoxの推移依存`org.bouncycastle:bcpkix-jdk15to18:1.72`に`TrustAllX509TrustManager`警告が3件あります。アプリのサンプル取得は標準の`HttpsURLConnection`を使用し、独自のTrustManagerやSSLSocketFactoryを設定していません。警告を抑制せず残しています。依存JARに警告が存在する事実と、アプリの通信経路で利用される脆弱性は区別する必要があります。今回、依存の一括更新や全CVEの監査は行っていません。

実機での確認項目: パスワードPDFの表示・終了、プロセス強制終了後の再起動、サンプル再ダウンロード、ログ共有前の内容確認。JVMテストだけで端末上の全動作を保証するものではありません。

## 旧撮影・配布スクリプト

`capture_store_screenshots.py`と`play_console_screenshots.py`に残っているViewer用撮影設定は、UtilityのapplicationIdと一致しないため撮影・アップロード前に停止します。設定・端末試験・素材の再検証なしに有効化しないでください。原稿はUtility自身の`store_listing/metadata_*.txt`のみを読みます。未作成言語の原稿をViewerから流用しません。

`run_long_running_test.py`、`run_ui_endurance_test.py`、`transfer_testdata.py`はSDK/JDKの環境変数を利用し、`--serial DEVICE_SERIAL`が必須です。実機操作を行うため専用試験端末で使用してください。転送スクリプトは対象アプリの端末内testdataを置換します。
