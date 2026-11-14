# Asset manifest

## Active character art

`ActorArt.java` draws original 64×64 transparent monochrome pixel sprites at startup. Sprites use six gray values, nearest filtering, and a shared baseline. The traveler has a neutral pose and two stride poses, displayed at 3× in gameplay and mirrored on turns. Enemies render at 4×. Elements, Pets, Weather, and Food each have distinct sprite families and encounter variants. Mr. Kotoba has a crown and ledger. Weather spirits use a short stepped hover. These poses and effects are visual only.

The scenery and title screen are drawn in code. The title screen shows a monochrome moonlit landscape with animated mist and water.

The table includes assets from the original prototype that are still used by the campaign or its regression tests.

| Path / asset | Purpose | Dimensions / frames | Format | Status |
| --- | --- | --- | --- | --- |
| `ScreenView.java` / player | Scarf-wearing protagonist placeholder | Code-drawn pixels, roughly 40×70 at 2× display scale; no sprite sheet; simulation collider 17×30 world units | Java rectangles | Implemented prototype |
| `ScreenView.java` / fire inspector | Flame silhouette, eyes, anticipation color and health label | Code-drawn, roughly 60×70 display pixels; procedural bob | Java rectangles | Implemented prototype |
| `ScreenView.java` / district | Night sky, pixel moon, skyline, lanterns, gate, terrain, motes | 640×360 world view at 2× display scale; no tileset | Java rectangles | Implemented prototype |
| `assets/fonts/DotGothic16-Regular.ttf` | Japanese and Latin pixel UI text | Selected glyphs | TTF | Included in JAR |
| `assets/fonts/DotGothic16-OFL.txt` | Font notices/license | N/A | Text | Included in JAR |
| `assets/data/kanji/water-source.svg` | Original ordered 水 paths, provenance | 109×109 viewBox, four paths; not a sprite sheet | SVG | Preserved KanjiVG original, CC BY-SA 3.0 |
| `assets/data/kanji/water.json` | Derived cubic templates | Four ordered strokes; sampled at load | JSON | Implemented, CC BY-SA 3.0 derived data |
| `assets/data/kanji/KanjiVG-LICENSE.txt` | Preserved supplied license | N/A | Text | Included in JAR |
| `assets/data/level.json` | Level solids, spawns, projectile/enemy tuning, elemental matrix | 1800-unit horizontal level | JSON | Implemented |
| `assets/CREDITS.md` | Font/data authors, source URLs, attribution, changes and license links | N/A | Markdown | Included in JAR |
| `assets/audio/` | Music and sound effects | 22,050 Hz mono PCM | WAV | Included in JAR |

Font and stroke sources are documented in the [credits](../assets/CREDITS.md).

Verified SHA256 snapshot values (2026-11-14):

| File | SHA256 |
| --- | --- |
| water-source.svg | `17025cc265555ea3172b361fee740e58f178c7ed61fb7d1c7124175358f2d41e` |
| water.json | `de9e09de4c869d44c275aac36101bd7cd82f79cd9c7d52da2c98fe8457838300` |
