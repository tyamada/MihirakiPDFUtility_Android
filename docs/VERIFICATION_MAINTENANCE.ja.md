# 検証記録の更新ルールと内部メモ

対象: [VERIFICATION_LIST.md](../VERIFICATION_LIST.md)

## 記載先

- VERIFICATION_LIST.mdの本文は「結果の表」と「使用上の注意（Notes）」だけに限定します。文書タイトルと表・Notesの見出しは使用できます。
- 表とNotesは英語で記載します。製品名、実際のUI表示、ファイル名などの引用は原表記を保持できます。
- Notesには利用者に必要な制約、既知の問題、対象版・配布元に関する注意などを簡潔に記載します。テストの手順、個々の確認結果、ログ、証跡、設定復元の経緯などの詳細は記載しません。
- テスト結果の詳細と証跡は、別の検証メモ（docs/validation/配下など）へ保存します。更新ルール、作業手順、Git操作、実装上の説明、検証の調整経緯はこのファイルまたは別の内部メモへ保存します。

## Maintenance rule / 更新ルール

- モデルごとに最新の検証結果だけを表に掲載します。同じモデルを再検証した場合は、その行と対応するNotesを最新結果へ置き換え、古い結果を重複して残しません。
- 表ではモデル・OS・アプリの版・配布元・確認日・結果を識別できるようにします。確認範囲や未確認項目の詳細は別メモに保存し、利用上の判断に必要な制限だけをNotesへ簡潔に反映します。
- 最新の実際の検証日時を基準にし、別版での合格を未検証の版へ適用しません。特に検証版と配信済みPlay版を区別します。
- 過去の確認履歴はGit履歴や別の検証メモで保持します。未解決の問題を結果の整理によって合格扱いにしません。
- Keep VERIFICATION_LIST.md limited to an English results table and concise user-facing Notes. Keep one latest result per device model, distinguish tested builds and distribution sources, and retain relevant limitations and known issues. Store detailed test results, procedures, evidence, and internal notes in separate files.

## 過去の証跡

Older results are omitted from this latest-only list. Previously committed results remain in Git history (including 635c546 and b8f4b0e). Earlier Pixel 8a LOCAL-build evidence remains in [persistence verification](validation/local-retention-20261116/RESULT.ja.md) and [TalkBack verification](validation/talkback-local-20261116/RESULT.ja.md); those records do not establish results for the latest build. Detailed evidence from the November 25 sessions and the pre-consolidation document are retained in the local task outputs.

## 2026-11-25 内部メモ

- この文書分離では検証結果を変更せず、実装説明・テスト調整の経緯・未コミット変更保持の作業メモを検証一覧から移した。
- Pixel 8aのPlay版はファイル名修正コミット b8f4b0e を含まないため、修正の実機合格は別IDのmoto g24検証版に限定する。

- 検証と資料整理では既存の未コミット変更を保持した。
- Both Save launcher call sites now use a shared filename helper that removes existing trailing PDF extensions case-insensitively before appending one `.pdf`.
- Toolbar text measurement and rendering now explicitly use the same label style and disable soft wrapping. Width calculation reserves an additional 2dp for pixel-rounding differences between text, padding, and spacing; the automatic fitting calculation uses those same button widths.
- An initial assertion using didOverflowWidth was replaced after diagnostics showed it can flag an unconstrained no-wrap paragraph even when its glyphs fit; the final check uses glyph extents with a one-pixel rounding tolerance. One interrupted attempt while locked had no Compose hierarchy and is not counted as a passing test.
