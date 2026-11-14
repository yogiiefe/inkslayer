# INKSLAYER — architecture

## Campaign implementation

- `Main` loads and cross-checks `assets/data/campaign.json` and the KanjiVG-derived templates before creating the screen.
- `CampaignContent` validates encounter counts, lesson references, unlock order and Kotoba’s three rounds. `CampaignRun` owns fixed-step walking, offensive damage, the announced counter window and encounter progression. The earlier `World` and `GameContent` remain available for the one-encounter regression suite.
- `GameScreen` routes menus, lesson prompts, spell input, the journal, safe practice and cinematic panels. `ScreenView` draws the 1280×720 UI, stage themes and spell feedback with the same viewport used for mouse input.
- `KanjiRecognizer` compares supported shapes of equal stroke count and rejects ambiguous and unavailable matches. Shape, order and direction stay separate. Learning saves record assisted and independent successes, errors and last review.
- `SaveStore` writes three versioned local slots with checkpoint backup. `GameAudio` owns looping music, SFX and category volume. The desktop launcher configures audio preferences beside the save directory.
- `DesktopSmoke` exercises the packaged campaign using scripted mouse strokes through the production input processor. Domain tests cover combat and persistence without graphics.

## Original baseline (before gameplay implementation, 2026-11-14)

- Java source/target 21 in Gradle configuration.
- `core`: libGDX 1.14.2; FreeType, TypingLabel, VisUI, ShapeDrawer and RegExodus are already declared. Their presence does not require using them for every feature.
- `lwjgl3`: desktop launcher, LWJGL 3.4.3 constraints, runnable JAR and Construo packaging configuration; optional native-image path disabled by default.
- Gradle wrapper distribution configured as 9.8.0; daemon JVM criteria request 21. Distribution/toolchain resolution has not been validated.
- Originally `Main` was an `ApplicationAdapter` rendering a sample texture. The current implementation below replaces that sample.

## Current implementation

- `Main` extends libGDX `Game`, loads JSON content and installs `GameScreen`.
- `GameScreen` owns input, a bounded fixed-step accumulator and mutually exclusive title/play/cast/journal/pause/settings/end modes. `ScreenView` owns SpriteBatch, the gameplay drawing area, a white pixel texture and selected-glyph FreeType atlases. The arena uses a flat background, a floor line and two code-drawn actors. Disposal is explicit.
- `World` is graphics-free Java simulation. Walking to the encounter triggers a player turn. A valid Water cast resolves elemental damage and gives a surviving monster one counterattack before control returns.
- `Drawing`, `InkPoint` and `KanjiRecognizer` are engine-independent. Shape and order/direction findings are separate. Recognition is synchronous and bounded to the four-stroke vocabulary; no worker or pending result exists across close/death/reset.
- `GameContent` is a JSON adapter for level solids, spawns, tuning, elemental matrix and sampled cubic Kanji geometry. It uses libGDX JsonReader without requiring a graphics context.
- `DesktopSmoke` is an opt-in actual-OpenGL/input integration probe controlled by `-Dinkslayer.smoke=true`; inactive in ordinary play. JUnit domain tests need no graphics context.
- Walking and monster turns use 60 Hz ticks with at most six catch-up ticks. The drawing panel uses fixed UI coordinates and the player turn has no countdown.
- Native/desktop startup remains in `lwjgl3`. Runtime dependencies were preserved. JUnit 5.12.2 was added for tests. Initial art remains code-drawn placeholders; imported font/data notices are packaged with assets.

No general dialogue graph, asset/audio manager, save system, Tiled importer, ECS or multi-character recognizer is claimed implemented. Add those boundaries when required by subsequent slices.

## Proposed future boundaries

Use plain Java value types and services for handwriting geometry, recognition, stroke pedagogy, damage resolution, enemy decisions, dialogue traversal, and save DTO validation. Keep libGDX adapters around input, rendering, sound, file access, and desktop lifecycle. Avoid building an ECS or dependency injection framework until requirements justify it. Prefer composition and explicit ownership to inheritance-heavy entity hierarchies.

Pipeline: mouse events → immutable stroke snapshot → shape recognizer → independent stroke validator → spellcasting policy → single cast command → combat resolver → feedback events. Render/UI consume state; they do not decide damage or recognition thresholds. Read the three handwriting/spellcasting skills for the detailed contracts.

Use stable IDs for Kanji, spell, enemy, room, dialogue node, and progression flags. Store gameplay spell mappings separately from sourced Japanese metadata. Template versions and acceptance-policy versions belong in test/telemetry output.

Proposed runtime uses a fixed simulation step with bounded catch-up, independent UI time, and interpolated rendering. Input edges are consumed once per simulation decision. Pause, draw mode, and hit-stop must have explicit clock semantics. Select collision implementation after testing movement needs; no Box2D dependency is currently declared.

Graphics objects stay on the rendering thread. Dispose `Texture`, `SpriteBatch`, stages, fonts, audio, and managers according to a clear owner. Never make domain tests depend on an OpenGL context. Use immutable snapshots and revision IDs if recognition is later moved off-thread.

## Environment findings from skill setup (historical)

Codex CLI 0.169.0 is available. Shell `java -version` resolves to Java 8, which is insufficient for this configured build; a usable JDK 21 was not verified. Python aliases cannot execute. Shell HTTPS downloads fail with TLS/Schannel errors; browser research works. No build or game runtime test was run during skill setup, avoiding the build task that rewrites `assets/assets.txt`.

Gameplay implementation subsequently found JDK 21.0.12.1 and successfully downloaded Gradle/dependencies via Java networking. Normal JDK compilation hits a managed-Windows ZipFS canonicalization issue. The optional compiler helper compiles all sources without suppressing errors; actual Gradle tests/packaging and a real-GL desktop probe pass. See TESTING.md for commands, final counts and limits. No global PATH or permissions were changed. The wrapper version/framework were preserved.

## Authoritative API references

- [libGDX documentation](https://libgdx.com/wiki/)
- [Scene2D UI](https://libgdx.com/wiki/graphics/2d/scene2d/scene2d-ui)
- [Gradle wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html)
- [JUnit user guide](https://docs.junit.org/current/user-guide/)

Consult documentation matching the installed versions before implementing an API. A current documentation URL is a reference, not authorization to upgrade.
