# Verification List

| Category | Manufacturer | Device model | OS | App version / source | Latest verification | Result | Notes |
|:--|:--|:--|:--|:--|:--|:--|:--|
| Smartphone | FCNT | arrows We2 M07 | 16 | 1.2.15 (31), Play | 2026-11-25 | Basic checks passed; known filename issue | 1 |
| Smartphone | Google | Pixel 8a | 17 | 1.2.15 (31), Play | 2026-11-25 | Basic checks passed; known filename issue | 2 |
| Smartphone | motorola | moto g24 | 14 | 1.2.15-filenamecheck (31), separate debug build | 2026-11-25 | Save filename regression passed | 3 |
| Smartphone | OPPO | Reno11 A CPH2603 | 16 | 1.2.15 (31), Play | 2026-11-25 | Basic checks passed; known filename issue | 4 |
| Smartphone | Samsung | Galaxy A54 5G SC-53D | 16 | 1.2.15-toolbarcheck (31), separate debug build | 2026-11-25 | Toolbar label regression passed | 5 |
| Smartphone | Sharp | AQUOS sense4 SH-41A | 12 | 1.2.15 (31), Play | 2026-11-25 | Basic checks passed; known filename issue | 6 |
| Smartphone | Sony | Xperia 10 VI XQ-ES44 | 16 | 1.2.15 (31), Play | 2026-11-25 | Basic operations passed; label clipping and known filename issue | 7 |
| Smartphone | Sony | Xperia 10 VIII XQ-GH44 | 17 | 1.2.15 (31), Play | 2026-11-25 | Basic checks passed; known filename issue | 8 |
| Tablet | Samsung | Galaxy Tab A SM-T510 | 9 | 1.2.15 (31), Play | 2026-11-25 | Basic checks passed; known filename issue | 9 |
| Tablet | aiwa | tab AG10 JA3-TBA1003 | 13 | 1.2.15 (31), Play | 2026-11-25 | Basic checks passed; known filename issue | 10 |
| Desktop | ASUS | Chromebook CM30 | 13 | 1.2.5 (21), source not recorded | Not recorded | Window and tablet modes reported ready | 11 |

## Notes

1. arrows We2 M07: the installed Play build suggests `.pdf.pdf` when saving. The tested toolbar labels were complete. Newer source fixes have not been verified in a distributed update on this model.
2. Pixel 8a: the installed Play build suggests `.pdf.pdf` when saving. Basic checks used a hidden toolbar and More actions; visible toolbar layouts were not verified in this session.
3. moto g24: the corrected filename was verified only in the separate filenamecheck debug build. The installed Play build remains 1.2.15 (31) without that fix.
4. Reno11 A: the installed Play build suggests `.pdf.pdf` when saving. The four visible toolbar labels were complete. Landscape and newer source fixes were not verified.
5. Galaxy A54: complete labels were verified in the separate toolbarcheck debug build, which also contains the filename fix. The installed Play build has not received these fixes. Physical rotation and manual/all-button profiles were not verified.
6. AQUOS sense4: the installed Play build suggests `.pdf.pdf` when saving. Portrait toolbar labels were complete; landscape and newer source fixes were not verified.
7. Xperia 10 VI: the installed Play build clips toolbar labels and suggests `.pdf.pdf` when saving. Full action labels are available through More actions. Source fixes have not been retested on this model.
8. Xperia 10 VIII: the installed Play build suggests `.pdf.pdf` when saving. Portrait toolbar labels were complete; landscape and newer source fixes were not verified.
9. Galaxy Tab A: the installed Play build suggests `.pdf.pdf` when saving. Portrait toolbar labels were complete; landscape and newer source fixes were not verified.
10. aiwa tab AG10: the installed Play build suggests `.pdf.pdf` when saving. Landscape labels were complete; the system picker retained some Japanese labels. Portrait and newer source fixes were not verified.
11. Chromebook CM30: window and tablet modes were reported ready by the user. The verification date and distribution source were not recorded; other functions and later builds are not covered.
