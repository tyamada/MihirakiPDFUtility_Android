# Verification List

This is a list of verification results compiled from developer and user information.

## Maintenance rule / 更新ルール

- モデルごとに最新の検証結果だけを掲載します。同じモデルを再検証した場合は、表の行と対応する注記・詳細を最新の結果に置き換え、古い結果を重複して残しません。
- 最新の実際の検証日時を基準にし、端末のモデル・OS・アプリの版・配布元（Play版／検証版）・確認範囲・未確認項目・残る問題を明記します。別の版での合格を、未検証の版にも適用しません。
- 過去の確認履歴はGit履歴や個別の検証記録で参照します。最新の検証で未解決の問題は、確認範囲とともに残します。
- Keep only the latest verification result per device model. On retesting, replace that model's table row and associated notes/details rather than accumulating older entries. Record the actual verification date, OS, app version, distribution source, tested scope, untested items, and outstanding issues. Preserve historical evidence in Git history or separate verification records; do not infer success for an untested build.

The table and model details below contain one latest verification session per model. A later focused verification replaces an earlier broad check; its pass result applies only to the recorded build and scope. Dates are JST. “Not recorded” means that the source record did not identify that value.

| Category | Manufacturer | Device model | OS | App version / source | Latest verification | Result | Detail |
|:--|:--|:--|:--|:--|:--|:--|:--|
| Smartphone | FCNT | arrows We2 M07 | 16 | 1.2.10 (26), Play Alpha | 2026-11-06 | Update and badge checks passed | [1](#1-arrows-we2-m07) |
| Smartphone | Google | Pixel 8a | 17 | 1.2.15 (31), Play | 2026-11-25 | Basic checks passed; known filename issue | [2](#2-pixel-8a) |
| Smartphone | motorola | moto g24 | 14 | 1.2.15-filenamecheck (31), separate debug build | 2026-11-25 | Save filename regression passed | [3](#3-moto-g24) |
| Smartphone | OPPO | Reno11 A CPH2603 | 16 | 1.2.10 (26), Play Alpha | 2026-11-06 | Update and badge checks passed | [4](#4-reno11-a-cph2603) |
| Smartphone | Samsung | Galaxy A54 5G SC-53D | 16 | 1.2.15-toolbarcheck (31), separate debug build | 2026-11-25 | Toolbar label regression passed | [5](#5-galaxy-a54-5g-sc-53d) |
| Smartphone | Sharp | AQUOS sense4 SH-41A | 12 | 1.2.10 (26), Play Alpha | 2026-11-06 | Update and badge checks passed | [6](#6-aquos-sense4-sh-41a) |
| Smartphone | Sony | Xperia 10 VI XQ-ES44 | 16 | 1.2.15 (31), Play | 2026-11-25 | Basic operations passed; label clipping and known filename issue | [7](#7-xperia-10-vi-xq-es44) |
| Smartphone | Sony | Xperia 10 VIII XQ-GH44 | 17 | 1.2.10 (26), Play Alpha | 2026-11-06 | Update and badge checks passed | [8](#8-xperia-10-viii-xq-gh44) |
| Tablet | Samsung | Galaxy Tab A SM-T510 | 9 | 1.2.15 (31), Play | 2026-11-25 | Basic checks passed; known filename issue | [9](#9-galaxy-tab-a-sm-t510) |
| Tablet | aiwa | tab AG10 JA3-TBA1003 | 13 | 1.2.15 (31), Play | 2026-11-25 | Basic checks passed; known filename issue | [10](#10-aiwa-tab-ag10-ja3-tba1003) |
| Desktop | ASUS | Chromebook CM30 | 13 | 1.2.5 (21), source not recorded | Not recorded | Window and tablet modes reported ready | [11](#11-chromebook-cm30) |

## Latest results by model

### 1. arrows We2 M07

On November 6, 2026, verified on arrows We2 that the Google Play closed testing (Alpha) build updated in place from 1.2.9 (25) to 1.2.10 (26). The Koyomi Badge acquisition dialog appeared on the first launch after the update. After fully closing and relaunching the app, the badge was still visible on the Version screen.

Scope is limited to the recorded update, first badge dialog, and badge retention after app relaunch. Other functions and a full device reboot were not recorded.

### 2. Pixel 8a

Verified the existing Google Play build 1.2.15 (31) on Pixel 8a / Android 17 with the device system language set to English (United States). Original language order was Japanese (Japan), then English (United States), `ja-JP,en-US`; changed it through system Settings to `en-US,ja-JP` and confirmed English app text. At completion, restored `ja-JP,en-US`, primary locale `ja-JP`, and the original region Japan through Settings and verified the displayed values. No other device settings were changed.

Confirmed the English onboarding labels and, with the existing toolbar-hidden configuration unchanged, used More actions to Open an independently generated three-page English PDF through the Android file picker. Verified three page thumbnails, selection of page 1, Rotate Right visibly rotating only that page, Undo restoring its upright orientation, Save to a separate local file, and reopening that saved PDF with all three pages upright. English menu labels and system file-picker labels were visible and usable. No crash or blocking error was observed during these operations.

The installed Play build is the unchanged, pre-fix 1.2.15 (31): the known default filename issue reproduced as `edited_english-pixel8a-20261125.pdf.pdf`. Saving and reopening still succeeded. The source fix committed as b8f4b0e and its separate moto g24 verification do not update this installed Play build; this run is not a Pixel 8a verification of that fix. No install, uninstall, data clearing, or store operation was performed; app version, first-install time, and update time matched before/after. Test input/output PDFs were retained in Downloads. This was a representative portrait-mode basic check; landscape, TalkBack, options changes, merge/split/delete, password-protected PDFs, full-device reboot, and badge acquisition/persistence were not tested here. Existing uncommitted changes were preserved.

### 3. moto g24

moto g24 / Android 14 / English (United States): verified the filename fix in the separate debug verification build `com.takumayamada22.pdfutility.filenamecheck`, version `1.2.15-filenamecheck` (31). The installed Google Play build remains 1.2.15 (31) and does not yet contain this source fix.

Both Save launcher call sites now use a shared filename helper that removes existing trailing PDF extensions case-insensitively before appending one `.pdf`. With `english-verification-20261125.pdf` open, the Android Save picker suggested exactly `edited_english-verification-20261125.pdf`. Saved without editing that suggestion and reopened the resulting file successfully with all three pages displayed. The previous `.pdf.pdf` filename issue did not reproduce on this path. Unit regression cases cover lowercase/uppercase PDF extensions, no extension, repeated extensions, embedded dots/Japanese characters, and null/empty base names. All 63 unit tests passed (including six filename regression tests); debug APK build and Lint completed, with zero Lint errors and 12 warnings. Other filename edge cases were covered by unit tests rather than individually exercised on the device.

The existing Play installation was preserved: all five APK SHA-256 values, version, first-install time, and update time matched before/after. The device already used `en-US,ja-JP`; the same system-language order was confirmed after verification, so no language change or restoration action was required. No other device settings were changed. The separate verification app and test PDFs were retained. This verifies the source fix in a separate debug build, not an updated Play release; no store operation was performed.

### 4. Reno11 A CPH2603

On November 6, 2026, verified on OPPO Reno11 A that the app updated in place through Google Play from 1.2.6 (22) to the 1.2.10 (26) closed testing (Alpha) build. The Koyomi Badge acquisition dialog appeared on the first launch after the update. After fully closing and relaunching the app, the badge was still visible on the Version screen.

Other functions and a full device reboot were not recorded.

### 5. Galaxy A54 5G SC-53D

Retested on Galaxy A54 5G SC-53D / Android 16 using the separate debug build `com.takumayamada22.pdfutility.toolbarcheck`, version 1.2.15-toolbarcheck (31). This source fix has not been distributed through Play.

Toolbar text measurement and rendering now explicitly use the same label style and disable soft wrapping. Width calculation reserves an additional 2dp for pixel-rounding differences between text, padding, and spacing; the automatic fitting calculation uses those same button widths. With the actual system language English (United States), original font scale 1.1, and 450 dpi, visually confirmed full portrait labels Open, Open Folder, Sample PDF, Save, Export Selected, and Undo within two rows. Opened the existing independently generated three-page PDF, selected page 1, and confirmed the enabled Export Selected label remained complete. Save opened the system picker; cancelled without writing. The candidate name had a single `.pdf` because this verification build also contains the earlier committed filename fix.

The enhanced ToolbarLayoutTest now checks actual label glyph extents, ellipsis, one-line layout, height overflow, row count, and button overlap, rather than button bounds alone. It passed on this device across 36 combinations: Japanese/English, font scales 1.0/1.1/2.0, Standard/Simple/Compact, and portrait/landscape configuration values at a fixed 320dp container width. These configuration cases are not physical rotation tests. An initial assertion using didOverflowWidth was replaced after diagnostics showed it can flag an unconstrained no-wrap paragraph even when its glyphs fit; the final check uses glyph extents with a one-pixel rounding tolerance. One interrupted attempt while locked had no Compose hierarchy and is not counted as a passing test. Final instrumentation result: OK (1 test). Unit tests: 63 passed, zero failures/errors. Debug APK and instrumentation APK builds and Lint succeeded; Lint reported zero errors and 12 warnings.

Restored original Japanese-only system language (`ja-JP`) by removing the temporary English entry. Screen-off timeout was temporarily extended from 30000ms to 600000ms to prevent re-locking, then restored to 30000ms. Font scale remained 1.1; no other device settings were changed. All existing Play APK SHA-256 values matched before/after. The separate verification app and test APK were retained. TalkBack, physical landscape rotation, manual/all-button profiles, every action, and arbitrary labels wider than the viewport were not verified in this follow-up.

The installed Play 1.2.15 (31) has not received either source fix. Success here applies to the separate debug build only.

### 6. AQUOS sense4 SH-41A

On November 6, 2026, verified on AQUOS sense4 that the Google Play closed testing (Alpha) build updated in place from 1.2.9 (25) to 1.2.10 (26). The Koyomi Badge acquisition dialog appeared on the first launch after the update. After fully closing and relaunching the app, the badge was still visible on the Version screen.

Other functions and a full device reboot were not recorded.

### 7. Xperia 10 VI XQ-ES44

Verified the existing Google Play build 1.2.15 (31) on Xperia 10 VI XQ-ES44 / Android 16 with system language English (United States). Original language order was Japanese (Japan), then English (United States), `ja-JP,en-US`. Moved English first and confirmed `en-US,ja-JP` and English app text. At the end moved Japanese back first, confirmed the language-change dialog, and verified the original `ja-JP,en-US` and primary locale `ja-JP` were restored. No other device settings were changed.

Confirmed English onboarding/start labels, Open PDF through the Android file picker, three page thumbnails from an independently generated English PDF, selection of page 1, Rotate Right through More actions visibly rotating that page, Undo restoring its upright orientation, Save to a separate local file, and reopening that output with all three pages upright. No crash or blocking error was observed during the checked operations. In the current portrait configuration, toolbar labels visibly clipped as follows: Open appeared as “Ope”, Save as “Sav”, Undo as “Und”, and Open Folder / Sample PDF / Export Selected lost text after their first word. Full labels were available in More actions. These display issues are not counted as an overall clean result.

The installed pre-fix 1.2.15 (31) also reproduced the known save suggestion `edited_english-xperia10vi-20261125.pdf.pdf`; saving and reopening succeeded. Source fixes and the separate Galaxy A54 toolbar verification do not update this Play installation; neither fix was retested in a separate build on this Xperia. App version, first-install time (2026-10-19 19:22:38), and update time (2026-11-22 04:48:46) matched before/after. No install, uninstall, app-data clearing, or store operation was performed. Test input/output PDFs were retained in Downloads. Landscape, TalkBack, options changes, merge/split/delete, password-protected PDFs, full-device reboot, and badge acquisition/persistence were not tested. Existing uncommitted changes and earlier records were preserved.

### 8. Xperia 10 VIII XQ-GH44

On November 6, 2026, verified on Xperia XQ-GH44 that the Google Play closed testing (Alpha) build updated in place from 1.2.9 (25) to 1.2.10 (26). The Koyomi Badge acquisition dialog appeared on the first launch after the update. After fully closing and relaunching the app, the badge was still visible on the Version screen.

Other functions and a full device reboot were not recorded.

### 9. Galaxy Tab A SM-T510

Verified the existing Google Play build 1.2.15 (31) on Galaxy Tab A SM-T510 / Android 9 with system language English (United States). Original language list was Japanese (Japan) only (`ja-JP`). Temporarily added English as default (`en-US,ja-JP`), confirmed English application text, then removed only that added English after verification. Confirmed the original Japanese-only list and primary locale `ja-JP` were restored. Screen-off timeout remained 30000ms; no other device settings were changed.

Confirmed English onboarding/start labels, Open PDF through the Android file picker, three page thumbnails from an independently generated English PDF, selection of page 1, Rotate Right through More actions visibly rotating that page, Undo restoring its upright orientation, Save to a separate local file, and reopening that output with all three pages upright. Toolbar labels visible in this portrait configuration were complete; no comparable clipping to the Galaxy A54 observation was seen. No crash or blocking error was observed during these operations.

The installed pre-fix 1.2.15 (31) reproduced the known default save suggestion `edited_english-tab-a-20261125.pdf.pdf`; saving and reopening succeeded. This does not test the source filename or toolbar fixes in an updated Play release. Version, first-install time (2026-11-06 08:17:51), and update time (2026-11-22 19:25:40) matched before/after; no install, uninstall, data clearing, or store operation was initiated. Input/output test PDFs were retained in Downloads. Landscape, TalkBack, options changes, merge/split/delete, password-protected PDFs, full-device reboot, and badge acquisition/persistence were not tested. Existing uncommitted source changes and prior verification entries were preserved.

### 10. aiwa tab AG10 JA3-TBA1003

Verified the existing Google Play build 1.2.15 (31) on aiwa tab AG10 JA3-TBA1003 / Android 13 with system language English (United States). Original language list was Japanese (Japan) only (`ja-JP`). Temporarily added English and moved it to first (`en-US,ja-JP`), confirmed English app text, then removed only that temporary English entry through Settings. Confirmed the original Japanese-only list and primary locale `ja-JP` were restored. No other device settings were changed.

In the existing landscape configuration, confirmed English onboarding and the wide start-screen layout, Open PDF through the Android file picker, three thumbnails from an independently generated English PDF, selection of page 1, Rotate Right through the toolbar visibly rotating that page, Undo restoring its upright orientation, Save to a separate local PDF, and reopening the saved file with all three pages upright. The desktop-style File/Edit/Insert/View/Options/Help menu bar and toolbar labels were visible in English; no comparable toolbar clipping to the Galaxy A54 observation was seen. The system file picker retained some Japanese labels (for example Recent/Downloads) alongside English controls despite the English system-language order; this was observed in the system picker rather than app text. No crash or blocking error was observed during the checked operations.

The installed pre-fix 1.2.15 (31) reproduced the known save suggestion `edited_english-aiwa-20261125.pdf.pdf`; saving and reopening succeeded. This is not a verification of the source filename or toolbar fixes in a distributed update. No install, uninstall, app-data clearing, or store operation was initiated by the agent. Version and first-install time (2026-10-23 09:24:29) remained the same, but lastUpdateTime changed from 2026-11-21 03:14:23 to 2026-11-25 10:34:59 during this session; the cause was not established, so unchanged APKs/installation metadata are not claimed. Test input/output PDFs were retained in Downloads. Portrait, physical rotation, TalkBack, options changes, merge/split/delete, passwords, full-device reboot, and badge acquisition/persistence were not tested. Existing uncommitted changes and previous verification entries were preserved.

### 11. Chromebook CM30

User-reported verification of window mode and tablet (full-screen) mode for 1.2.5 (21) / OS 13. The original record does not identify the verification date or distribution source. Other functions, badge behavior, language, and later builds are not covered.

## Historical evidence

Older results are omitted from this latest-only list. Previously committed results remain in Git history (including 635c546 and b8f4b0e). Earlier Pixel 8a LOCAL-build evidence remains in [persistence verification](docs/validation/local-retention-20261116/RESULT.ja.md) and [TalkBack verification](docs/validation/talkback-local-20261116/RESULT.ja.md); those records do not establish results for the latest build. Detailed evidence from the November 25 sessions and the pre-consolidation document are retained in the local task outputs.
