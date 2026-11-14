# INKSLAYER project instructions

INKSLAYER is a Java desktop 2D action-adventure platformer with pixel art, dark comedy, fantasy, and Japanese learning for complete beginners. Players draw Kanji with a mouse to cast elemental counters. Mr. Kotoba is the main antagonist.

## Repository facts and boundaries

- Existing stack: Java 21, libGDX 1.14.2, LWJGL3, Gradle wrapper. Inspect build files before changing dependencies or APIs. Preserve this stack unless the task explicitly includes evaluating a replacement.
- `core/` contains shared game code; `lwjgl3/` owns desktop startup and packaging; `assets/` contains runtime resources. A playable one-Kanji water-casting MVP now exists; see `docs/ARCHITECTURE.md` and `docs/TESTING.md` for implemented systems and evidence.
- Read `docs/GDD.md` for product intent, `docs/LORE.md` for established canon, `docs/ARCHITECTURE.md` for observed/proposed technical boundaries, and `docs/ROADMAP.md` for milestones. Label proposals; do not silently turn them into canon or implemented features.
- Use `.agents/skills/` for specialized workflows. `docs/SKILLS.md` is the inventory and ownership map. Load only the skills relevant to the current task.
- Keep recognition and combat rules testable without graphics. Prioritize deterministic Kanji recognition before ML or external services. Stroke pedagogy and combat acceptance are separate policies.
- Japanese content requires traceable character, reading, meaning, and stroke references. A game element association is fiction, not a universal Japanese linguistic rule. Do not invent readings or stroke orders.
- Mouse handwriting is the core input; provide undo, clear, explicit submission, readable feedback, and a safe practice path. Avoid color-only weakness signals and time pressure as the only teaching method.
- Keep changes within the requested scope. The earlier skill-setup task is complete. Gameplay work may modify source, assets and build configuration as needed for the current milestone; preserve unrelated working features and user assets.
- Never execute downloaded install scripts or copy private project files to recognition services. Audit selected third-party skill files, dependencies, relative references, and licenses before installation. Record source revision and hashes when vendoring.

## Development verification

Use the wrapper from the project root with a suitable JDK (the build targets Java 21): `./gradlew.bat :core:test` for future unit tests, `./gradlew.bat :lwjgl3:run` for desktop checks, and `./gradlew.bat :lwjgl3:jar` for a distributable JAR. Inspect tasks before assuming test configuration exists. Current `processResources` generates `assets/assets.txt`; do not run builds during documentation-only work that promises unchanged assets.

Report checks actually run and distinguish headless logic evidence from rendering, audio, and human learning evidence. Record unresolved toolchain failures instead of claiming a successful build. No automatic publishing or framework migration is implied.
