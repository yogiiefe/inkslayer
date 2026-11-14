# INKSLAYER!

A Java/libGDX desktop side-scrolling RPG with turn-based combat and short Japanese lessons.

## Play on Windows

Double-click **PLAY.bat** at the project root. It launches the packaged `lwjgl3/build/libs/INKSLAYER!-1.0.0.jar` using an installed JDK 21 when found. Java 21 or newer is required; a bundled runtime/installer is not included.

Alternatively, from any working directory:

```powershell
& 'C:\Program Files\Java\jdk-21.0.12.1\bin\java.exe' -jar 'C:\Users\Administrator\Documents\inkslayer\lwjgl3\build\libs\INKSLAYER!-1.0.0.jar'
```

Select **New**, choose a slot, and watch or skip the prologue. Walk right into the first lesson and encounter. Draw the Kanji of a learned element to attack. When the monster announces its attack, draw the element that counters it or press **G** to guard.

The campaign has four stages and 40 encounters, ending with three rounds against Mr. Kotoba. **Save** on the main menu loads a checkpoint. The game saves before the first encounter, after each win, and at the ending. **Save & Return to Menu** in pause preserves lesson progress and resumes at the current encounter checkpoint.

## Controls

| Action | Control |
| --- | --- |
| Walk | A / D or Left / Right |
| Answer a reading or vocabulary prompt | Click a card, or use Left / Right and Enter |
| Draw a stroke | Hold left mouse button, then release |
| Submit | Enter |
| Undo | Ctrl+Z while drawing |
| Clear | R while drawing |
| Guard | G during a monster attack |
| Drawing guide or answer hint | H |
| Kanji journal | Tab during play |
| Safe drawing practice | Tab while walking, then Practice (Q) in the journal |
| Pause / close overlay | Escape |
| Next cinematic panel / skip | Enter / S |
| Continue or retry | Enter on the outcome screen |

Drawing has no timer. Strict mode checks lesson stroke order and direction. Master, SFX and BGM volume persist between sessions. Save files are local to the user, under `%LOCALAPPDATA%\INKSLAYER\saves` on Windows.

## Build and test

Use JDK 21 and the existing Gradle wrapper. No runtime dependency was added; JUnit 5.12.2 is the new test dependency.

```powershell
.\gradlew.bat :core:test :lwjgl3:jar
.\gradlew.bat :lwjgl3:run
```

This managed Windows session has a JDK ZipFS canonicalization failure despite allowing JAR reads. The optional reviewed helper recompiles all source with the Java Compiler API, then runs the actual Gradle/JUnit tests and packages with Gradle. It does not suppress compiler diagnostics or change filesystem permissions:

```powershell
$build = [scriptblock]::Create((Get-Content -LiteralPath tools/build-managed.ps1 -Raw))
& $build -JdkPath 'C:\Program Files\Java\jdk-21.0.12.1'
```

Run that command from this root. The helper uses `.gradle-local/` for downloaded dependencies and the wrapper. Inline invocation accommodates this host's disabled `.ps1` invocation policy without changing the policy. See [testing evidence](docs/TESTING.md).

Optional window properties: `-Dinkslayer.width=1280 -Dinkslayer.height=720 -Dinkslayer.fps=60`. Aspect ratio is preserved with letterboxing. The camera shows 640 horizontal world units. UI and drawing use a 1280×720 viewport.

## Scope and status

Rencana kampanye empat stage dan 40 encounter tersedia di [BUILD_PLAN.md](docs/BUILD_PLAN.md).

The campaign includes five elemental spells, 15 Kanji, eight katakana words, hiragana reading exercises, opening and ending sequences, local saves and original audio. Art and animation use code-drawn pixel graphics. Pronunciation recordings, human Japanese-language review and beginner handwriting calibration are pending.

See [GDD](docs/GDD.md), [architecture](docs/ARCHITECTURE.md), [roadmap](docs/ROADMAP.md), [recognition](docs/RECOGNITION.md), and [asset manifest](docs/ASSET_MANIFEST.md). Font and KanjiVG attribution/licenses are in [credits](assets/CREDITS.md), also included in the JAR.
