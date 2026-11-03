# Utility 国・地域別の1回限りの商品価格変更報告

2026-11-03 JST。Play Console保存後の再読込で確認した登録価格を記録する。アプリ本体は無料。今回の作業は既存の価格登録だけを更新し、配信国、税設定、商品の利用可否、既購入者の権利は変更していない。

## 対象と結果

- 商品順：Bronze `tip_100`、Silver `tip_500`、Gold `tip_1000`、こよみバッジ `tip_koyomi`。
- 1商品174地域×4商品＝696価格を全件照合。実変更664件、据え置き32件。先行45地域で180件、残り125地域で484件を変更した。同額のため変更不要だった地域も照合した。
- 両アプリ合計は1,392価格の照合、1,326件変更。先行45地域360件＋残り125地域966件。見直し対象170地域では両アプリの対応商品価格が一致し、Bronzeとこよみも一致する。Bronze＜Silver＜Goldを確認。
- 新規地域・商品・購入オプションは追加していない。「新しい国/地域」の設定も変更していない。登録価格174地域はアプリ配信国数とは別の概念。
- 日本 JPY 200 / 500 / 1,000 / 200、米国 USD 1.99 / 4.99 / 9.99 / 1.99、韓国 KRW 1,900 / 4,900 / 9,900 / 1,900 は今回維持。
- ロシアはGoogle Play課金停止のため据え置き。両アプリで従来価格に差があるが変更していない。ベラルーシの無料アプリ内購入をロシアと同じ停止扱いにはしていない。
- UtilityクローズドAlpha 1.2.9（25）は16:27 JSTにテスター公開を確認済み。BetaLobbyを含む既存2グループと178か国／地域を維持し、製品版は変更していない。未使用商品 `tip_100u` は変更対象外。

## 判断根拠と適用方法

米国価格を起点とし、先行45地域では現地通貨の端数、近隣市場、税を考慮した。ユーロ圏はEUR 1.99 / 4.99 / 9.99 / 1.99、台湾はTWD 60 / 150 / 300 / 60、香港はHKD 15 / 39 / 79 / 15、マカオはMOP 16 / 40 / 80 / 16とした。2026-11-02の中央銀行為替公表などを参照した。

残り126地域を確認し、ロシアを除く125地域に適用した。世界銀行の所得区分を購買力の代理指標とし、次のUSD基準額からGoogleの換算・現地端数・既存税率による価格を生成した。これは家計の購買力平価を直接測った値や、購入実績による最適価格ではない。

| 方針 | 地域数 | Bronze / Silver / Gold / こよみ |
|---|---:|---|
| 高所得 HIC | 12 | USD 1.99 / 4.99 / 9.99 / 1.99 |
| 上位中所得 UMC | 43 | USD 1.29 / 3.29 / 6.49 / 1.29 |
| 下位中所得 LMC | 35 | USD 0.89 / 2.29 / 4.49 / 0.89 |
| 低所得 LIC | 17 | USD 0.59 / 1.49 / 2.99 / 0.59 |
| 湾岸6地域など | 10 | USD 1.49 / 3.99 / 7.99 / 1.49 |
| ウクライナ・ミャンマー・レバノン | 3 | USD 0.69 / 1.79 / 3.49 / 0.69 |
| 欧州近隣 | 4 | EUR 1.99 / 4.99 / 9.99 / 1.99 |
| リヒテンシュタイン | 1 | CHF 1.79 / 4.49 / 8.99 / 1.79 |

USD基準額は税加算前。欧州近隣4地域とリヒテンシュタインは直接指定した表示価格。税率自体は変更していない。所得区分以外の補正は運用判断であり、販売実績による優位性は未検証。各地域の根拠・適用基準と全商品価格は[検証データ](pricing/REGIONAL_PRICES_2026-11-03.json)に記録した。

## 検証と限界

各商品の編集前・保存前・保存後を比較し、保存後にConsoleを再読込した。全174地域の価格と地域集合、税設定、利用可否を独立集計で照合した。変更対象外49地域（先行45、日本・韓国・米国、ロシア）は残り125地域の操作でも維持された。

Viewerこよみのトーゴ・ベナンはGoogleの丸めによるBronzeとの差を個別修正し、再読込後に一致を確認した。期待値の書き換えだけで不一致を解消していない。

購入決済、全地域の実機ストア表示、課金後の権利付与は今回実施していない。Consoleの保存値の検証と区別する。価格設定のみのためアプリの再ビルド・再配信は不要。今回の文書コミットに対するCIとGitHub Pagesの実行結果はコミット後に確認する。今後は購入率・応援額・現地表示の実績を確認し、必要なら価格実験を行う。

## 全174地域の変更前後

価格順はBronze / Silver / Gold / こよみ。変更前は先行45地域の編集開始前、変更後は残り125地域の保存・再読込後。日本・韓国・米国は、それ以前に合意・適用済みの価格を両時点で維持した。

| 国・地域 | 対象 | 変更前 | 保存後 |
|---|---|---|---|
| アイスランド | 残り125地域 | EUR 0.69 / EUR 3.45 / EUR 6.88 / EUR 0.69 | EUR 1.99 / EUR 4.99 / EUR 9.99 / EUR 1.99 |
| アイルランド | 先行45地域 | EUR 0.69 / EUR 3.39 / EUR 6.99 / EUR 0.69 | EUR 1.99 / EUR 4.99 / EUR 9.99 / EUR 1.99 |
| アゼルバイジャン | 残り125地域 | AZN 1.07 / AZN 5.37 / AZN 10.73 / AZN 1.09 | AZN 2.19 / AZN 5.49 / AZN 10.99 / AZN 2.19 |
| アメリカ合衆国 | 維持 | USD 1.99 / USD 4.99 / USD 9.99 / USD 1.99 | USD 1.99 / USD 4.99 / USD 9.99 / USD 1.99 |
| アラブ首長国連邦 | 残り125地域 | AED 2.39 / AED 11.99 / AED 23.99 / AED 2.39 | AED 5.69 / AED 14.99 / AED 30.99 / AED 5.69 |
| アルジェリア | 残り125地域 | DZD 84.00 / DZD 425.00 / DZD 850.00 / DZD 85.00 | DZD 175.00 / DZD 450.00 / DZD 875.00 / DZD 175.00 |
| アルゼンチン | 残り125地域 | ARS 941.85 / ARS 4,724.20 / ARS 9,433.45 / ARS 960.00 | ARS 2,000.00 / ARS 5,100.00 / ARS 10,000.00 / ARS 2,000.00 |
| アルバ | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 1.99 / USD 4.99 / USD 9.99 / USD 1.99 |
| アルバニア | 残り125地域 | USD 0.76 / USD 3.79 / USD 7.57 / USD 0.76 | USD 1.55 / USD 3.95 / USD 7.79 / USD 1.55 |
| アルメニア | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 1.29 / USD 3.29 / USD 6.49 / USD 1.29 |
| アンゴラ | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 0.89 / USD 2.29 / USD 4.49 / USD 0.89 |
| アンティグア・バーブーダ | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 1.99 / USD 4.99 / USD 9.99 / USD 1.99 |
| イエメン | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 0.59 / USD 1.49 / USD 2.99 / USD 0.59 |
| イギリス | 先行45地域 | GBP 0.59 / GBP 2.89 / GBP 5.49 / GBP 0.59 | GBP 1.79 / GBP 4.49 / GBP 8.99 / GBP 1.79 |
| イスラエル | 残り125地域 | ILS 1.90 / ILS 9.60 / ILS 19.00 / ILS 2.00 | ILS 6.30 / ILS 15.90 / ILS 31.90 / ILS 6.30 |
| イタリア | 先行45地域 | EUR 0.69 / EUR 3.39 / EUR 6.99 / EUR 0.69 | EUR 1.99 / EUR 4.99 / EUR 9.99 / EUR 1.99 |
| イラク | 残り125地域 | IQD 827 / IQD 4,150 / IQD 8,250 / IQD 956 | IQD 1,950 / IQD 4,950 / IQD 9,800 / IQD 1,950 |
| インド | 先行45地域 | INR 70.00 / INR 360.00 / INR 720.00 / INR 70.00 | INR 99.00 / INR 249.00 / INR 499.00 / INR 99.00 |
| インドネシア | 先行45地域 | IDR 11,000 / IDR 56,000 / IDR 109,000 / IDR 11,000 | IDR 19,000 / IDR 49,000 / IDR 99,000 / IDR 19,000 |
| ウガンダ | 残り125地域 | USD 0.74 / USD 3.73 / USD 7.45 / USD 0.74 | USD 0.70 / USD 1.76 / USD 3.53 / USD 0.70 |
| ウクライナ | 残り125地域 | UAH 33.99 / UAH 169.99 / UAH 339.99 / UAH 33.99 | UAH 37.99 / UAH 99.99 / UAH 189.99 / UAH 37.99 |
| ウズベキスタン | 残り125地域 | UZS 8,520.00 / UZS 42,480.00 / UZS 84,840.00 / UZS 8,540.00 | UZS 11,900.00 / UZS 30,610.00 / UZS 60,020.00 / UZS 11,900.00 |
| ウルグアイ | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 1.49 / USD 3.99 / USD 7.99 / USD 1.49 |
| エクアドル | 残り125地域 | USD 0.59 / USD 3.19 / USD 6.49 / USD 0.59 | USD 1.29 / USD 3.29 / USD 6.49 / USD 1.29 |
| エジプト | 残り125地域 | EGP 36.99 / EGP 189.99 / EGP 369.99 / EGP 38.99 | EGP 55.99 / EGP 139.99 / EGP 279.99 / EGP 55.99 |
| エストニア | 先行45地域 | EUR 0.69 / EUR 3.49 / EUR 6.99 / EUR 0.69 | EUR 1.99 / EUR 4.99 / EUR 9.99 / EUR 1.99 |
| エチオピア | 残り125地域 | ETB 99.84 / ETB 500.80 / ETB 1,000.01 / ETB 100.00 | ETB 95.00 / ETB 250.00 / ETB 500.00 / ETB 95.00 |
| エリトリア | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 0.59 / USD 1.49 / USD 2.99 / USD 0.59 |
| エルサルバドル | 残り125地域 | USD 0.59 / USD 3.19 / USD 6.49 / USD 0.59 | USD 1.29 / USD 3.29 / USD 6.49 / USD 1.29 |
| オマーン | 残り125地域 | USD 0.69 / USD 3.29 / USD 6.49 / USD 0.69 | USD 1.59 / USD 4.19 / USD 8.49 / USD 1.59 |
| オランダ | 先行45地域 | EUR 0.69 / EUR 3.39 / EUR 6.49 / EUR 0.69 | EUR 1.99 / EUR 4.99 / EUR 9.99 / EUR 1.99 |
| オーストラリア | 先行45地域 | AUD 0.99 / AUD 4.89 / AUD 9.99 / AUD 0.99 | AUD 2.99 / AUD 7.99 / AUD 15.99 / AUD 2.99 |
| オーストリア | 先行45地域 | EUR 0.69 / EUR 3.29 / EUR 6.49 / EUR 0.69 | EUR 1.99 / EUR 4.99 / EUR 9.99 / EUR 1.99 |
| カザフスタン | 残り125地域 | KZT 330.00 / KZT 1,590.00 / KZT 3,290.00 / KZT 330.00 | KZT 690.00 / KZT 1,690.00 / KZT 3,390.00 / KZT 690.00 |
| カタール | 残り125地域 | QAR 2.50 / QAR 12.00 / QAR 23.00 / QAR 2.50 | QAR 5.50 / QAR 15.00 / QAR 29.00 / QAR 5.50 |
| カナダ | 先行45地域 | CAD 0.89 / CAD 4.49 / CAD 8.99 / CAD 0.89 | CAD 2.79 / CAD 6.99 / CAD 13.99 / CAD 2.79 |
| カメルーン | 残り125地域 | XAF 450 / XAF 2,200 / XAF 4,300 / XAF 450 | XAF 600 / XAF 1,600 / XAF 3,100 / XAF 600 |
| カンボジア | 残り125地域 | USD 0.59 / USD 3.19 / USD 6.49 / USD 0.59 | USD 0.89 / USD 2.29 / USD 4.49 / USD 0.89 |
| カーボベルデ | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 1.29 / USD 3.29 / USD 6.49 / USD 1.29 |
| ガボン | 残り125地域 | EUR 0.56 / EUR 2.78 / EUR 5.55 / EUR 0.56 | EUR 1.15 / EUR 2.94 / EUR 5.79 / EUR 1.15 |
| ガンビア | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 0.59 / USD 1.49 / USD 2.99 / USD 0.59 |
| ガーナ | 残り125地域 | GHS 9.00 / GHS 45.00 / GHS 90.00 / GHS 9.00 | GHS 10.00 / GHS 30.00 / GHS 65.00 / GHS 10.00 |
| キプロス | 先行45地域 | EUR 0.69 / EUR 3.29 / EUR 6.49 / EUR 0.69 | EUR 1.99 / EUR 4.99 / EUR 9.99 / EUR 1.99 |
| キルギス | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 0.89 / USD 2.29 / USD 4.49 / USD 0.89 |
| ギニア | 残り125地域 | USD 0.59 / USD 3.19 / USD 6.49 / USD 0.59 | USD 0.89 / USD 2.29 / USD 4.49 / USD 0.89 |
| ギニアビサウ | 残り125地域 | EUR 0.56 / EUR 2.78 / EUR 5.55 / EUR 0.56 | EUR 0.53 / EUR 1.33 / EUR 2.67 / EUR 0.53 |
| ギリシャ | 先行45地域 | EUR 0.69 / EUR 3.49 / EUR 6.99 / EUR 0.69 | EUR 1.99 / EUR 4.99 / EUR 9.99 / EUR 1.99 |
| クウェート | 残り125地域 | USD 0.59 / USD 3.19 / USD 6.49 / USD 0.59 | USD 1.49 / USD 3.99 / USD 7.99 / USD 1.49 |
| クロアチア | 先行45地域 | EUR 0.69 / EUR 3.49 / EUR 6.99 / EUR 0.69 | EUR 1.99 / EUR 4.99 / EUR 9.99 / EUR 1.99 |
| グアテマラ | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 1.29 / USD 3.29 / USD 6.49 / USD 1.29 |
| グレナダ | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 1.29 / USD 3.29 / USD 6.49 / USD 1.29 |
| ケイマン諸島 | 残り125地域 | USD 0.59 / USD 3.19 / USD 6.49 / USD 0.59 | USD 1.99 / USD 4.99 / USD 9.99 / USD 1.99 |
| ケニア | 残り125地域 | KES 90.00 / KES 500.00 / KES 900.00 / KES 100.00 | KES 100.00 / KES 300.00 / KES 700.00 / KES 100.00 |
| コスタリカ | 残り125地域 | CRC 280.00 / CRC 1,400.00 / CRC 2,800.00 / CRC 280.00 | CRC 670.00 / CRC 1,800.00 / CRC 3,600.00 / CRC 670.00 |
| コモロ | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 0.89 / USD 2.29 / USD 4.49 / USD 0.89 |
| コロンビア | 残り125地域 | COP 2,000 / COP 10,500 / COP 21,000 / COP 2,000 | COP 4,000 / COP 10,500 / COP 21,000 / COP 4,000 |
| コンゴ共和国(ブラザビル) | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 0.89 / USD 2.29 / USD 4.49 / USD 0.89 |
| コンゴ民主共和国(キンシャサ) | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 0.59 / USD 1.49 / USD 2.99 / USD 0.59 |
| コートジボワール | 残り125地域 | XOF 450 / XOF 2,100 / XOF 4,300 / XOF 450 | XOF 600 / XOF 1,600 / XOF 3,100 / XOF 600 |
| サウジアラビア | 残り125地域 | SAR 2.69 / SAR 13.99 / SAR 26.99 / SAR 2.69 | SAR 6.39 / SAR 16.99 / SAR 33.99 / SAR 6.39 |
| サモア | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 1.29 / USD 3.29 / USD 6.49 / USD 1.29 |
| サンマリノ | 残り125地域 | EUR 0.59 / EUR 2.79 / EUR 5.49 / EUR 0.59 | EUR 1.99 / EUR 4.99 / EUR 9.99 / EUR 1.99 |
| ザンビア | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 0.89 / USD 2.29 / USD 4.49 / USD 0.89 |
| シエラレオネ | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 0.59 / USD 1.49 / USD 2.99 / USD 0.59 |
| シンガポール | 先行45地域 | SGD 0.98 / SGD 4.48 / SGD 8.98 / SGD 0.98 | SGD 2.98 / SGD 6.98 / SGD 13.98 / SGD 2.98 |
| ジブチ | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 0.89 / USD 2.29 / USD 4.49 / USD 0.89 |
| ジブラルタル | 残り125地域 | GBP 0.49 / GBP 2.39 / GBP 4.79 / GBP 0.49 | GBP 1.49 / GBP 3.69 / GBP 7.49 / GBP 1.49 |
| ジャマイカ | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 1.29 / USD 3.29 / USD 6.49 / USD 1.29 |
| ジョージア | 残り125地域 | GEL 2.00 / GEL 9.00 / GEL 19.00 / GEL 2.00 | GEL 4.00 / GEL 9.00 / GEL 19.00 / GEL 4.00 |
| ジンバブエ | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 0.89 / USD 2.29 / USD 4.49 / USD 0.89 |
| スイス | 先行45地域 | CHF 0.50 / CHF 2.60 / CHF 5.20 / CHF 0.50 | CHF 1.79 / CHF 4.49 / CHF 8.99 / CHF 1.79 |
| スウェーデン | 先行45地域 | SEK 8.00 / SEK 39.00 / SEK 79.00 / SEK 8.00 | SEK 19.00 / SEK 49.00 / SEK 99.00 / SEK 19.00 |
| スペイン | 先行45地域 | EUR 0.69 / EUR 3.39 / EUR 6.49 / EUR 0.69 | EUR 1.99 / EUR 4.99 / EUR 9.99 / EUR 1.99 |
| スリナム | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 1.29 / USD 3.29 / USD 6.49 / USD 1.29 |
| スリランカ | 残り125地域 | LKR 200.00 / LKR 1,050.00 / LKR 2,075.00 / LKR 200.00 | LKR 425.00 / LKR 1,100.00 / LKR 2,150.00 / LKR 425.00 |
| スロバキア | 先行45地域 | EUR 0.69 / EUR 3.39 / EUR 6.99 / EUR 0.69 | EUR 1.99 / EUR 4.99 / EUR 9.99 / EUR 1.99 |
| スロベニア | 先行45地域 | EUR 0.69 / EUR 3.39 / EUR 6.99 / EUR 0.69 | EUR 1.99 / EUR 4.99 / EUR 9.99 / EUR 1.99 |
| セネガル | 残り125地域 | XOF 450 / XOF 2,100 / XOF 4,300 / XOF 450 | XOF 600 / XOF 1,600 / XOF 3,100 / XOF 600 |
| セルビア | 残り125地域 | RSD 99 / RSD 399 / RSD 799 / RSD 99 | RSD 149 / RSD 399 / RSD 799 / RSD 149 |
| セントクリストファー・ネーヴィス | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 1.99 / USD 4.99 / USD 9.99 / USD 1.99 |
| セントルシア | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 1.29 / USD 3.29 / USD 6.49 / USD 1.29 |
| セーシェル | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 1.99 / USD 4.99 / USD 9.99 / USD 1.99 |
| ソマリア | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 0.59 / USD 1.49 / USD 2.99 / USD 0.59 |
| ソロモン諸島 | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 0.89 / USD 2.29 / USD 4.49 / USD 0.89 |
| タイ | 残り125地域 | THB 23.00 / THB 110.00 / THB 225.00 / THB 23.00 | THB 47.00 / THB 125.00 / THB 225.00 / THB 47.00 |
| タジキスタン | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 0.89 / USD 2.29 / USD 4.49 / USD 0.89 |
| タンザニア | 残り125地域 | TZS 1,700.00 / TZS 8,500.00 / TZS 17,000.00 / TZS 1,700.00 | TZS 2,400.00 / TZS 6,000.00 / TZS 12,000.00 / TZS 2,400.00 |
| タークス・カイコス諸島 | 残り125地域 | USD 0.59 / USD 3.19 / USD 6.49 / USD 0.59 | USD 1.99 / USD 4.99 / USD 9.99 / USD 1.99 |
| チェコ | 先行45地域 | CZK 15.99 / CZK 79.99 / CZK 159.99 / CZK 16.99 | CZK 39.00 / CZK 99.00 / CZK 199.00 / CZK 39.00 |
| チャド | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 0.59 / USD 1.49 / USD 2.99 / USD 0.59 |
| チュニジア | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 0.89 / USD 2.29 / USD 4.49 / USD 0.89 |
| チリ | 残り125地域 | CLP 720 / CLP 3,600 / CLP 7,200 / CLP 740 | CLP 1,700 / CLP 4,600 / CLP 9,300 / CLP 1,700 |
| デンマーク | 先行45地域 | DKK 5.00 / DKK 26.00 / DKK 49.00 / DKK 5.00 | DKK 15.00 / DKK 39.00 / DKK 79.00 / DKK 15.00 |
| トリニダード・トバゴ | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 1.99 / USD 4.99 / USD 9.99 / USD 1.99 |
| トルクメニスタン | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 1.29 / USD 3.29 / USD 6.49 / USD 1.29 |
| トルコ | 残り125地域 | TRY 36.99 / TRY 184.99 / TRY 369.99 / TRY 37.99 | TRY 76.99 / TRY 194.99 / TRY 389.99 / TRY 76.99 |
| トンガ | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 1.29 / USD 3.29 / USD 6.49 / USD 1.29 |
| トーゴ | 残り125地域 | EUR 0.56 / EUR 2.78 / EUR 5.55 / EUR 0.56 | EUR 0.79 / EUR 2.04 / EUR 4.01 / EUR 0.79 |
| ドイツ | 先行45地域 | EUR 0.69 / EUR 3.29 / EUR 6.49 / EUR 0.69 | EUR 1.99 / EUR 4.99 / EUR 9.99 / EUR 1.99 |
| ドミニカ共和国 | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 1.29 / USD 3.29 / USD 6.49 / USD 1.29 |
| ドミニカ国 | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 1.29 / USD 3.29 / USD 6.49 / USD 1.29 |
| ナイジェリア | 残り125地域 | NGN 900.00 / NGN 4,510.00 / NGN 9,010.00 / NGN 910.00 | NGN 1,290.00 / NGN 3,310.00 / NGN 6,490.00 / NGN 1,290.00 |
| ナミビア | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 0.89 / USD 2.29 / USD 4.49 / USD 0.89 |
| ニカラグア | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 0.89 / USD 2.29 / USD 4.49 / USD 0.89 |
| ニジェール | 残り125地域 | EUR 0.56 / EUR 2.78 / EUR 5.55 / EUR 0.56 | EUR 0.53 / EUR 1.33 / EUR 2.67 / EUR 0.53 |
| ニュージーランド | 先行45地域 | NZD 1.29 / NZD 6.49 / NZD 12.99 / NZD 1.29 | NZD 3.99 / NZD 9.99 / NZD 19.99 / NZD 3.99 |
| ネパール | 先行45地域 | USD 0.71 / USD 3.57 / USD 7.13 / USD 0.71 | USD 0.99 / USD 2.49 / USD 4.99 / USD 0.99 |
| ノルウェー | 先行45地域 | NOK 8.00 / NOK 38.00 / NOK 75.00 / NOK 7.00 | NOK 19.00 / NOK 49.00 / NOK 99.00 / NOK 19.00 |
| ハイチ | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 0.89 / USD 2.29 / USD 4.49 / USD 0.89 |
| ハンガリー | 先行45地域 | HUF 259 / HUF 1,290 / HUF 2,590 / HUF 259 | HUF 590 / HUF 1,490 / HUF 2,990 / HUF 590 |
| バチカン市国 | 残り125地域 | EUR 0.59 / EUR 2.79 / EUR 5.49 / EUR 0.59 | EUR 1.99 / EUR 4.99 / EUR 9.99 / EUR 1.99 |
| バヌアツ | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 0.89 / USD 2.29 / USD 4.49 / USD 0.89 |
| バハマ | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 1.99 / USD 4.99 / USD 9.99 / USD 1.99 |
| バミューダ | 残り125地域 | USD 0.59 / USD 3.19 / USD 6.49 / USD 0.59 | USD 1.99 / USD 4.99 / USD 9.99 / USD 1.99 |
| バングラデシュ | 残り125地域 | BDT 90.00 / BDT 450.00 / BDT 900.00 / BDT 90.00 | BDT 120.00 / BDT 320.00 / BDT 650.00 / BDT 120.00 |
| バーレーン | 残り125地域 | USD 0.69 / USD 3.49 / USD 6.99 / USD 0.69 | USD 1.59 / USD 4.39 / USD 8.99 / USD 1.59 |
| パキスタン | 残り125地域 | PKR 170.00 / PKR 890.00 / PKR 1,750.00 / PKR 170.00 | PKR 250.00 / PKR 620.00 / PKR 1,250.00 / PKR 250.00 |
| パナマ | 残り125地域 | USD 0.59 / USD 3.19 / USD 6.49 / USD 0.59 | USD 1.49 / USD 3.99 / USD 7.99 / USD 1.49 |
| パプアニューギニア | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 0.89 / USD 2.29 / USD 4.49 / USD 0.89 |
| パラグアイ | 残り125地域 | PYG 3,734 / PYG 20,000 / PYG 40,000 / PYG 3,641 | PYG 10,000 / PYG 20,000 / PYG 40,000 / PYG 10,000 |
| フィジー | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 1.29 / USD 3.29 / USD 6.49 / USD 1.29 |
| フィリピン | 先行45地域 | PHP 44.00 / PHP 220.00 / PHP 445.00 / PHP 45.00 | PHP 69.00 / PHP 179.00 / PHP 349.00 / PHP 69.00 |
| フィンランド | 先行45地域 | EUR 0.69 / EUR 3.49 / EUR 6.99 / EUR 0.69 | EUR 1.99 / EUR 4.99 / EUR 9.99 / EUR 1.99 |
| フランス | 先行45地域 | EUR 0.69 / EUR 3.29 / EUR 6.49 / EUR 0.69 | EUR 1.99 / EUR 4.99 / EUR 9.99 / EUR 1.99 |
| ブラジル | 先行45地域 | BRL 3.29 / BRL 15.99 / BRL 32.99 / BRL 3.19 | BRL 6.90 / BRL 16.90 / BRL 34.90 / BRL 6.90 |
| ブルガリア | 先行45地域 | EUR 0.69 / EUR 3.29 / EUR 6.49 / EUR 0.69 | EUR 1.99 / EUR 4.99 / EUR 9.99 / EUR 1.99 |
| ブルキナファソ | 残り125地域 | EUR 0.56 / EUR 2.78 / EUR 5.55 / EUR 0.56 | EUR 0.53 / EUR 1.33 / EUR 2.67 / EUR 0.53 |
| ベトナム | 先行45地域 | VND 16,000 / VND 82,000 / VND 164,000 / VND 16,000 | VND 29,000 / VND 69,000 / VND 139,000 / VND 29,000 |
| ベナン | 残り125地域 | EUR 0.56 / EUR 2.78 / EUR 5.55 / EUR 0.56 | EUR 0.79 / EUR 2.04 / EUR 4.01 / EUR 0.79 |
| ベネズエラ | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 0.89 / USD 2.29 / USD 4.49 / USD 0.89 |
| ベラルーシ | 残り125地域 | USD 0.76 / USD 3.79 / USD 7.57 / USD 0.76 | USD 1.55 / USD 3.95 / USD 7.79 / USD 1.55 |
| ベリーズ | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 1.29 / USD 3.29 / USD 6.49 / USD 1.29 |
| ベルギー | 先行45地域 | EUR 0.69 / EUR 3.39 / EUR 6.49 / EUR 0.69 | EUR 1.99 / EUR 4.99 / EUR 9.99 / EUR 1.99 |
| ペルー | 残り125地域 | PEN 2.09 / PEN 10.99 / PEN 20.99 / PEN 2.19 | PEN 4.49 / PEN 10.99 / PEN 21.99 / PEN 4.49 |
| ホンジュラス | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 0.89 / USD 2.29 / USD 4.49 / USD 0.89 |
| ボスニア・ヘルツェゴビナ | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 1.29 / USD 3.29 / USD 6.49 / USD 1.29 |
| ボツワナ | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 1.29 / USD 3.29 / USD 6.49 / USD 1.29 |
| ボリビア | 残り125地域 | BOB 7.49 / BOB 37.99 / BOB 75.99 / BOB 7.49 | BOB 10.99 / BOB 27.99 / BOB 53.99 / BOB 10.99 |
| ポルトガル | 先行45地域 | EUR 0.69 / EUR 3.39 / EUR 6.99 / EUR 0.69 | EUR 1.99 / EUR 4.99 / EUR 9.99 / EUR 1.99 |
| ポーランド | 先行45地域 | PLN 2.99 / PLN 14.99 / PLN 29.99 / PLN 2.99 | PLN 6.99 / PLN 17.99 / PLN 34.99 / PLN 6.99 |
| マカオ | 先行45地域 | MOP 4.99 / MOP 25.88 / MOP 50.99 / MOP 4.99 | MOP 16.00 / MOP 40.00 / MOP 80.00 / MOP 16.00 |
| マリ | 残り125地域 | EUR 0.56 / EUR 2.78 / EUR 5.55 / EUR 0.56 | EUR 0.53 / EUR 1.33 / EUR 2.67 / EUR 0.53 |
| マルタ | 先行45地域 | EUR 0.66 / EUR 3.28 / EUR 6.55 / EUR 0.66 | EUR 1.99 / EUR 4.99 / EUR 9.99 / EUR 1.99 |
| マレーシア | 先行45地域 | MYR 2.99 / MYR 13.99 / MYR 27.99 / MYR 2.99 | MYR 5.90 / MYR 14.90 / MYR 29.90 / MYR 5.90 |
| ミクロネシア連邦 | 残り125地域 | USD 0.59 / USD 3.19 / USD 6.49 / USD 0.59 | USD 1.29 / USD 3.29 / USD 6.49 / USD 1.29 |
| ミャンマー (ビルマ) | 残り125地域 | MMK 1,000 / MMK 7,000 / MMK 13,000 / MMK 1,000 | MMK 1,000 / MMK 4,000 / MMK 7,000 / MMK 1,000 |
| メキシコ | 先行45地域 | MXN 13.00 / MXN 65.00 / MXN 129.00 / MXN 14.00 | MXN 25.00 / MXN 59.00 / MXN 119.00 / MXN 25.00 |
| モザンビーク | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 0.59 / USD 1.49 / USD 2.99 / USD 0.59 |
| モナコ | 残り125地域 | EUR 0.69 / EUR 3.29 / EUR 6.49 / EUR 0.69 | EUR 1.99 / EUR 4.99 / EUR 9.99 / EUR 1.99 |
| モルディブ | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 1.29 / USD 3.29 / USD 6.49 / USD 1.29 |
| モルドバ | 残り125地域 | USD 0.76 / USD 3.79 / USD 7.57 / USD 0.76 | USD 1.55 / USD 3.95 / USD 7.79 / USD 1.55 |
| モロッコ | 残り125地域 | MAD 7.29 / MAD 35.99 / MAD 72.99 / MAD 7.39 | MAD 9.99 / MAD 26.99 / MAD 52.99 / MAD 9.99 |
| モンゴル | 残り125地域 | MNT 2,270.00 / MNT 11,300.00 / MNT 22,700.00 / MNT 2,270.00 | MNT 4,640.00 / MNT 11,800.00 / MNT 23,300.00 / MNT 4,640.00 |
| モーリシャス | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 1.29 / USD 3.29 / USD 6.49 / USD 1.29 |
| ヨルダン | 残り125地域 | JOD 0.450 / JOD 2.250 / JOD 4.500 / JOD 0.450 | JOD 0.900 / JOD 2.350 / JOD 4.600 / JOD 0.900 |
| ラオス | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 0.89 / USD 2.29 / USD 4.49 / USD 0.89 |
| ラトビア | 先行45地域 | EUR 0.69 / EUR 3.39 / EUR 6.49 / EUR 0.69 | EUR 1.99 / EUR 4.99 / EUR 9.99 / EUR 1.99 |
| リトアニア | 先行45地域 | EUR 0.69 / EUR 3.39 / EUR 6.49 / EUR 0.69 | EUR 1.99 / EUR 4.99 / EUR 9.99 / EUR 1.99 |
| リヒテンシュタイン | 残り125地域 | CHF 0.60 / CHF 2.80 / CHF 5.60 / CHF 0.60 | CHF 1.79 / CHF 4.49 / CHF 8.99 / CHF 1.79 |
| リビア | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 1.29 / USD 3.29 / USD 6.49 / USD 1.29 |
| リベリア | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 0.59 / USD 1.49 / USD 2.99 / USD 0.59 |
| ルクセンブルク | 先行45地域 | EUR 0.69 / EUR 3.29 / EUR 6.49 / EUR 0.69 | EUR 1.99 / EUR 4.99 / EUR 9.99 / EUR 1.99 |
| ルワンダ | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 0.59 / USD 1.49 / USD 2.99 / USD 0.59 |
| ルーマニア | 先行45地域 | RON 3.59 / RON 17.99 / RON 34.99 / RON 3.59 | RON 7.99 / RON 19.99 / RON 39.99 / RON 7.99 |
| レバノン | 残り125地域 | USD 0.63 / USD 3.16 / USD 6.31 / USD 0.63 | USD 0.69 / USD 1.79 / USD 3.49 / USD 0.69 |
| ロシア | 維持 | RUB 55.00 / RUB 269.00 / RUB 499.00 / RUB 55.00 | RUB 55.00 / RUB 269.00 / RUB 499.00 / RUB 55.00 |
| 中央アフリカ共和国 | 残り125地域 | EUR 0.56 / EUR 2.78 / EUR 5.55 / EUR 0.56 | EUR 0.53 / EUR 1.33 / EUR 2.67 / EUR 0.53 |
| 北マケドニア | 残り125地域 | USD 0.74 / USD 3.73 / USD 7.45 / USD 0.74 | USD 1.52 / USD 3.88 / USD 7.66 / USD 1.52 |
| 南アフリカ | 残り125地域 | ZAR 11.99 / ZAR 59.99 / ZAR 119.99 / ZAR 11.99 | ZAR 24.99 / ZAR 62.99 / ZAR 124.99 / ZAR 24.99 |
| 台湾 | 先行45地域 | TWD 21.00 / TWD 100.00 / TWD 210.00 / TWD 22.00 | TWD 60.00 / TWD 150.00 / TWD 300.00 / TWD 60.00 |
| 日本 | 維持 | JPY 200 / JPY 500 / JPY 1,000 / JPY 200 | JPY 200 / JPY 500 / JPY 1,000 / JPY 200 |
| 英領ヴァージン諸島 | 残り125地域 | USD 0.59 / USD 3.19 / USD 6.49 / USD 0.59 | USD 1.99 / USD 4.99 / USD 9.99 / USD 1.99 |
| 韓国 | 維持 | KRW 1,900 / KRW 4,900 / KRW 9,900 / KRW 1,900 | KRW 1,900 / KRW 4,900 / KRW 9,900 / KRW 1,900 |
| 香港 | 先行45地域 | HKD 4.95 / HKD 24.90 / HKD 49.90 / HKD 4.97 | HKD 15.00 / HKD 39.00 / HKD 79.00 / HKD 15.00 |

## 参照資料

- [https://datahelpdesk.worldbank.org/knowledgebase/articles/906519-world-bank-country-and-lending-groups](https://datahelpdesk.worldbank.org/knowledgebase/articles/906519-world-bank-country-and-lending-groups)
- [https://api.worldbank.org/v2/country?format=json&per_page=400](https://api.worldbank.org/v2/country?format=json&per_page=400)
- [https://support.google.com/googleplay/android-developer/answer/6334373?hl=en](https://support.google.com/googleplay/android-developer/answer/6334373?hl=en)
- [https://support.google.com/googleplay/android-developer/answer/138000?hl=en](https://support.google.com/googleplay/android-developer/answer/138000?hl=en)
- [https://support.google.com/googleplay/android-developer/answer/11950272?hl=en](https://support.google.com/googleplay/android-developer/answer/11950272?hl=en)
- [https://cbcg.me/en/core-functions/financial-and-banking-operations/fx-reference-rates](https://cbcg.me/en/core-functions/financial-and-banking-operations/fx-reference-rates)
- [https://www.cbc.gov.tw/en/mp-2.html](https://www.cbc.gov.tw/en/mp-2.html)
- [https://www.trade.gov/country-commercial-guides/hong-kong-market-overview-hong-kong-and-macau](https://www.trade.gov/country-commercial-guides/hong-kong-market-overview-hong-kong-and-macau)
- [https://google.play/business/price-experiments/](https://google.play/business/price-experiments/)
