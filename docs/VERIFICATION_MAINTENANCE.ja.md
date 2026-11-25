# 検証記録の更新ルールと内部メモ

対象: [VERIFICATION_LIST.md](../VERIFICATION_LIST.md)

## 記載先

- VERIFICATION_LIST.mdの本文は「結果の表」と「使用上の注意（Notes）」だけに限定します。文書タイトルと表・Notesの見出しは使用できます。
- 表とNotesは英語で記載します。製品名、実際のUI表示、ファイル名などの引用は原表記を保持できます。
- 表内のResultは次の4段階だけを使用します（2026-11-25 ユーザー指定）。英語表記は「No issues（問題なし）」「Limited（制限あり）」「Unusable（使用不可）」「Not tested（未検証）」です。長い説明をセル内へ詰め込みません。結果の補足、確認範囲、制約、既知の問題、対象版・配布元に関する注意は、表の外のNotesに英語で記載します。必要に応じて注番号やモデル名で該当行と対応付けます（2026-11-25 ユーザー指定）。
- Notesは利用者が結果を理解するための説明に限定します。テストの手順、操作ごとの詳細結果、ログ、証跡、設定復元の経緯などは別の検証メモへ保存します。
- テスト結果の詳細と証跡は、別の検証メモ（docs/validation/配下など）へ保存します。更新ルール、作業手順、Git操作、実装上の説明、検証の調整経緯はこのファイルまたは別の内部メモへ保存します。

## 結果の4段階

- No issues（問題なし）: 今回実際に確認した範囲で、利用を妨げる問題や既知の使用制限が認められなかった場合。全機能の検証完了を意味しません。
- Limited（制限あり）: 利用できるものの、既知の問題、機能の制約、回避操作が必要な条件などが確認された場合。制約と必要な回避策をNotesへ記載します。
- Unusable（使用不可）: 対象版をその端末で利用できない、起動できない、または確認対象の基本動作を進められない場合。理由をNotesへ記載します。
- Not tested（未検証）: 対象端末・対象版の動作を確認できていない場合。未実施の理由や確認待ちの範囲をNotesへ記載します。
- 通常の確認範囲外の項目が残るだけでLimitedとはしません。未確認範囲をNotesへ明記し、未確認部分を問題なしと断定しません。最新の対象版自体を確認していない場合はNot testedとします。
- 開発者・利用者による手動確認も、根拠と報告範囲をNotesへ明記して記録できます。自動検証や担当エージェント自身の確認と混同しません。
- 既知の問題を判定名の整理だけで解消扱いにしません。判定の説明は表外の英語Notes、手順・ログ・証跡は別の詳細メモへ保存します。

## Maintenance rule / 更新ルール

- モデルごとに最新の検証結果だけを表に掲載します。同じモデルを再検証した場合は、その行と対応するNotesを最新結果へ置き換え、古い結果を重複して残しません。
- 表ではモデル・OS・アプリの版・配布元・確認日・結果を識別できるようにします。結果を理解するための確認範囲や未確認項目の補足は表外のNotesへ記載し、操作単位の詳細や証跡は別メモに保存します。
- 最新の実際の検証日時を基準にし、別版での合格を未検証の版へ適用しません。特に検証版と配信済みPlay版を区別します。
- 過去の確認履歴はGit履歴や別の検証メモで保持します。未解決の問題を結果の整理によって合格扱いにしません。
- Keep VERIFICATION_LIST.md limited to an English results table and concise user-facing Notes. Keep result cells brief and put explanatory details in Notes outside the table, linked to the relevant device row when needed. Keep one latest result per device model, distinguish tested builds and distribution sources, and retain relevant limitations and known issues. Store detailed test results, procedures, evidence, and internal notes in separate files.

## 過去の証跡

Older results are omitted from this latest-only list. Previously committed results remain in Git history (including 635c546 and b8f4b0e). Earlier Pixel 8a LOCAL-build evidence remains in [persistence verification](validation/local-retention-20261116/RESULT.ja.md) and [TalkBack verification](validation/talkback-local-20261116/RESULT.ja.md); those records do not establish results for the latest build. Detailed evidence from the November 25 sessions and the pre-consolidation document are retained in the local task outputs.

## 2026-11-25 内部メモ

- この文書分離では検証結果を変更せず、実装説明・テスト調整の経緯・未コミット変更保持の作業メモを検証一覧から移した。
- Pixel 8aのPlay版はファイル名修正コミット b8f4b0e を含まないため、修正の実機合格は別IDのmoto g24検証版に限定する。

- 検証と資料整理では既存の未コミット変更を保持した。
- Both Save launcher call sites now use a shared filename helper that removes existing trailing PDF extensions case-insensitively before appending one `.pdf`.
- Toolbar text measurement and rendering now explicitly use the same label style and disable soft wrapping. Width calculation reserves an additional 2dp for pixel-rounding differences between text, padding, and spacing; the automatic fitting calculation uses those same button widths.
- An initial assertion using didOverflowWidth was replaced after diagnostics showed it can flag an unconstrained no-wrap paragraph even when its glyphs fit; the final check uses glyph extents with a one-pixel rounding tolerance. One interrupted attempt while locked had no Compose hierarchy and is not counted as a passing test.
