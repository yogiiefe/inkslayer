# Testing and build evidence

## Fill the Blank build after asset cleanup

On 15 November 2026, a clean JDK 21 build passed all 27 JUnit tests and packaged the desktop JAR. The JAR contains the INKSLAYER window icon, DotGothic16, quiz data, and menu music. The desktop probe passed the prologue, tutorial, display modes, all 40 encounters, 43 quiz answers, checkpoint reload, and the ending using an isolated save directory.

## Campaign build

The managed Windows build helper compiled the campaign with Java 21, ran 26 JUnit tests with zero failures, and packaged `lwjgl3/build/libs/INKSLAYER!-1.0.0.jar`. The suites cover all 25 elemental damage pairs, the distinction between a monster's main element and its incoming attack, the three boss rounds, template recognition, and save round-trip and backup recovery. The older results below describe earlier prototypes.

On 14 November 2026 the packaged JAR ran from the parent directory with an isolated saves directory. The opt-in probe opened Settings, adjusted Master volume, started a new slot, opened the prologue, practiced Kanji, and sent template-based mouse strokes through the production input processor. It cleared 40 battles, submitted 81 defensive counters and 91 lesson answers, reloaded a checkpoint, resized to 960×540, and saved the completed ending. The probe moves the player to each encounter boundary to keep the run short. Walking is exercised in domain tests.

The final probe exited with code 0. It wrote framebuffers and an isolated completed save under `build/campaign-smoke-171356/`.

```text
[SMOKE] PASS: New, opening, settings, all 40 battles, 81 defensive counters, 91 lesson answers, checkpoint reload, ending and resize
```

For a repeat, pass `-Dinkslayer.smoke=true`, `-Dinkslayer.saves=<new empty directory>` and `-Dinkslayer.screenshots=<output directory>` before `-jar`. Use a separate test directory from your campaign slots.

Music and effects shipped as 20 authored WAV files and were exercised through the desktop audio API. Listening and mix review, reviewed pronunciation recordings, Japanese-language human review, and beginner handwriting and learning evaluations remain pending.

Environment: Windows, Oracle JDK 21.0.12.1 at `C:\Program Files\Java\jdk-21.0.12.1`, existing Gradle 9.8.0 wrapper, libGDX 1.14.2, JUnit 5.12.2. Date: 2026-11-14.

## Side-scrolling and turn-based build

The latest managed build packaged the desktop JAR and passed 15 JUnit tests with zero failures. The current world tests cover horizontal encounter entry, movement lock during combat, one monster response per nonlethal cast, victory without a final counter, defeat, and reset. The earlier platformer results below describe the previous build.

The packaged OpenGL probe passed title input, invalid-submission turn preservation, mouse drawing, alternating turns, victory, settings, and resize. The battle framebuffer in `build/turn-check/casting.png` was visually inspected. The probe sets the player's position at the encounter boundary. Walking to the encounter is covered by the world tests.

## Host issue and reproducible workaround

Normal Gradle compilation fails with `AccessDeniedException` in Windows `toRealPath` when the JDK ZipFS closes dependency JARs. A direct diagnostic could read those exact JAR bytes but could not canonicalize their paths. This is not a source compilation diagnostic. Extracting classpaths did not solve it and was discarded.

`tools/CompileOnManagedWindows.java` invokes the JDK compiler task, checks its success, disables annotation processors and exits the helper process without explicitly closing the affected file manager. Process teardown releases its resources. Compiler errors still fail the build. It is a host-specific workaround; ordinary machines should use the standard Gradle build.

`tools/build-managed.ps1` resolves declared dependencies, compiles all core source, builds the core JAR, compiles desktop and test source against that JAR, then runs Gradle `:core:test :lwjgl3:jar` while excluding only the already executed compiler tasks. The final Gradle invocation alone is not evidence of compilation; the complete helper execution is required. Runtime dependency versions and game code are not changed by this workaround. Download cache stays under ignored `.gradle-local/`.

## Automated unit/integration tests

`core/src/test/java/com/yogiiefe/inkslayer/GameTest.java` covers canonical shape, translation/scale, jitter, empty/wrong count, order coaching, reversed curved strokes, nonfinite/tap input, unrelated lines, a fixed-seed 100-scribble negative corpus, undo/clear/cancel, elemental multipliers, ground/jump, wall/ceiling, coyote time, buffered landing jumps, projectile expiry/cooldown, recognized-water-to-fire-defeat integration, damage/invulnerability/death/reset, route traversal and tiny-input rejection.

Initial 18 tests passed both through the official JUnit Console launcher and the actual Gradle JUnit task. Two added tests cover complete gap traversal and tiny characters. Final complete managed-helper execution compiled all 9 core sources, 2 desktop sources and 1 test source successfully, then the actual Gradle JUnit task passed **20 tests, 0 failures, 0 errors, 0 skipped**. Evidence: `core/build/test-results/test/TEST-com.yogiiefe.inkslayer.GameTest.xml`, timestamp `2026-11-14T05:05:41.869Z`. No OpenGL context is used for these tests. Synthetic positives/negatives are not beginner accuracy evidence.

## Real OpenGL desktop probe

The packaged JAR was launched from `C:\Users\Administrator\Documents`, outside the project/assets directory. With explicit `-Dinkslayer.smoke=true`, `DesktopSmoke` captures actual framebuffers and injects mouse events through the production input processor. It checks four independent strokes, recognition/cast, projectile enemy defeat, fall death, reset, settings and resize. It teleports to the encounter for that portion; full route reachability is checked separately by a physics test. The probe is inactive during ordinary play.

```powershell
& 'C:\Program Files\Java\jdk-21.0.12.1\bin\java.exe' '-Dinkslayer.smoke=true' '-Dinkslayer.screenshots=C:/Users/Administrator/Documents/inkslayer/build/screenshots' -jar 'C:\Users\Administrator\Documents\inkslayer\lwjgl3\build\libs\INKSLAYER!-1.0.0.jar'
```

Observed output on the final packaged prototype build: `[SMOKE] PASS: GL rendering, mouse strokes, recognition, cast, enemy defeat, death, reset, settings, resize`. Exit code 0. Title/gameplay/canvas and 960×540 settings screenshots were visually inspected and showed no clipping. The probe writes screenshots to the ignored `build/screenshots/` directory by default. This is scripted desktop integration; manual playtesting and learning evaluation are separate. Process-window enumeration on this managed desktop did not expose a handle, so the probe captured the framebuffer directly inside the GL application.

Final packaged artifact: `lwjgl3/build/libs/INKSLAYER!-1.0.0.jar`, 27,369,638 bytes. Java runtime is not bundled. `PLAY.bat` launches that artifact using a JDK 21 installation when found. Original Java/libGDX version and wrapper were preserved. The 17 skill folders and all 20 locked skill files also passed the existing metadata/link/hash validator after documentation updates.

## Remaining checks

Human playtesting, beginner handwriting corpus evaluation, real-world latency profiling, audio mixing, save migration, Tiled import and release installer/bundled-JRE validation remain unperformed or out of scope. No recognition accuracy percentage, educational efficacy or minimum-hardware performance claim is made.
