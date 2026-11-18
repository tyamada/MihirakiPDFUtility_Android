# サンプルPDFの制作資料Releaseへの移行

2026-11-12。対象は MihirakiPDFUtility_Android。新しい配布元は [manga-prompt-kit の固定Release](https://github.com/tyamada/manga-prompt-kit/releases/tag/manga-pdf-20261103-v1)。

## 互換性

全51件のID、言語キー、表示名、話数、サイズ、SHA-256、ページ数を維持した。取得URLを固定Releaseへ移した。フィリピン語は言語キーfil、保存名は第1・2話の_fil.pdfを維持し、取得先だけ_tl.pdfへ変更した。既存のverified/<SHA-256>/<保存名>と旧ルートの取得済みファイルを再利用できる。取得処理・削除処理・設定や履歴の形式は変更していない。

READMEと公開index18ページの918リンクを新Releaseへ更新した。作者・CC BY 4.0・第三者素材の条件は維持した。旧配布元の証拠資料は履歴と照合のため保持した。

## 検証

結果を検証完了後に記録する。

## 配信準備

この作業でストア申請・配信・旧配布先の廃止は行わない。現在の開発設定は1.2.10 (26)で、既配信と同じ番号を使った検証ビルド。更新配信時には未使用のversionCodeと適切なリリースノートが必要。
