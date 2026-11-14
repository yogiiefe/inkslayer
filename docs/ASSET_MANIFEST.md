# Asset manifest

Prototype art is drawn from code. No final artwork or music was generated. Existing `assets/libgdx.png` remains untouched but is no longer used.

| Path / asset | Purpose | Dimensions / frames | Format | Status |
| --- | --- | --- | --- | --- |
| `ScreenView.java` / player | Scarf-wearing protagonist placeholder | Code-drawn pixels, roughly 40×70 at 2× display scale; no sprite sheet; simulation collider 17×30 world units | Java rectangles | Implemented prototype |
| `ScreenView.java` / fire inspector | Flame silhouette, eyes, anticipation color and health label | Code-drawn, roughly 60×70 display pixels; procedural bob | Java rectangles | Implemented prototype |
| `ScreenView.java` / district | Night sky, pixel moon, skyline, lanterns, gate, terrain, motes | 640×360 world view at 2× display scale; no tileset | Java rectangles | Implemented prototype |
| `assets/fonts/NotoSansJP.ttf` | Japanese and Latin UI text | Variable outline font; runtime sizes 15/21/37/76, selected glyphs only | TTF | Implemented, unmodified source bytes, OFL 1.1 |
| `assets/fonts/OFL.txt` | Font notices/license | N/A | Text | Included in JAR |
| `assets/data/kanji/water-source.svg` | Original ordered 水 paths, provenance | 109×109 viewBox, four paths; not a sprite sheet | SVG | Preserved KanjiVG original, CC BY-SA 3.0 |
| `assets/data/kanji/water.json` | Derived cubic templates | Four ordered strokes; sampled at load | JSON | Implemented, CC BY-SA 3.0 derived data |
| `assets/data/kanji/KanjiVG-LICENSE.txt` | Preserved supplied license | N/A | Text | Included in JAR |
| `assets/data/level.json` | Level solids, spawns, projectile/enemy tuning, elemental matrix | 1800-unit horizontal level | JSON | Implemented |
| `assets/CREDITS.md` | Font/data authors, source URLs, attribution, changes and license links | N/A | Markdown | Included in JAR |
| Music and SFX | Future audio | Not specified | Not present | Planned; game works silently |

Font source: [Google Fonts Noto Sans JP](https://github.com/google/fonts/tree/main/ofl/notosansjp). Stroke source: [KanjiVG 06c34.svg](https://github.com/KanjiVG/kanjivg/blob/master/kanji/06c34.svg). See the retained notices and [credits](../assets/CREDITS.md). Runtime text receives a small white border before tinting for readability; font source bytes are unchanged.

Verified SHA256 snapshot values (2026-11-14):

| File | SHA256 |
| --- | --- |
| NotoSansJP.ttf | `c2f3b4d463500a2ddcd3849cded1fceeb9fd6d1c32e6cbecd568453ba50fc68f` |
| water-source.svg | `17025cc265555ea3172b361fee740e58f178c7ed61fb7d1c7124175358f2d41e` |
| water.json | `de9e09de4c869d44c275aac36101bd7cd82f79cd9c7d52da2c98fe8457838300` |
