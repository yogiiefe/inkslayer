# INKSLAYER!

A Java/libGDX desktop side-scrolling RPG with turn-based combat and short Japanese lessons.

## Play on Windows

Install JDK 21, then build the game once from the project root with `.\gradlew.bat :lwjgl3:jar`. Double-click **PLAY.bat** to launch the resulting `lwjgl3/build/libs/INKSLAYER!-1.0.0.jar`.

To launch the JAR directly from the project root:

```powershell
java -jar 'lwjgl3/build/libs/INKSLAYER!-1.0.0.jar'
```

Select **New** and choose **Elements**, **Pets**, **Weather**, or **Food**. Each chapter opens with seven cinematic slides using the supplied bitmap alphabet and typewriter text. Click anywhere to complete the current sentence, then click again to advance. The animated **Skip** button in the top-right corner or S skips the cinematic. Choosing Elements starts with **The Unchecked Door**. Walk right, draw 水, and submit to open the water gate. Elements saves its first checkpoint after the tutorial. A later chapter starts at its first encounter and saves that checkpoint when selected. If all slots are occupied, the new run plays without saving.

In battle, read the English clue and draw the missing Kanji anywhere below the floor line. A correct answer completes the sentence and casts a spell. Encounters proceed directly through combat and victory feedback. Finishing a chapter opens the next chapter's cinematic, and completing the campaign opens the six-slide epilogue.

All cinematics use the supplied TEMPOREX instrumental with one-second fade-in and fade-out. Skip plays a short original ascending chime. Music and effects follow the Master, BGM and SFX settings.

The campaign has 39 different Kanji quizzes across Elements, Pets, Weather, and Food. Mr. Kotoba tests the katakana readings of four Kanji introduced earlier. **Save** on the main menu loads a checkpoint. The game saves before the first encounter, after each win, and at the ending. **Save & Return to Menu** in pause resumes at the current encounter checkpoint.

## Controls

In Settings, select **Display** to switch between Windowed, Borderless, and Fullscreen. Select **Keybinds** to change the gameplay controls listed below. Display mode and keybinds are saved for the next launch.

| Action | Control |
| --- | --- |
| Walk | A / D or Left / Right |
| Compose a Kotoba answer | Click katakana tiles, or select with Left / Right and add with Space. Backspace removes the last tile. |
| Draw a stroke | Hold left mouse button, then release |
| Submit | Enter |
| Undo | Ctrl+Z while drawing |
| Clear | R while drawing |
| Replay kanji drawing guide | H |
| Kanji journal | Open Glossarium from Pause (or press Tab while paused) |
| Safe drawing practice | Pause while walking, open Glossarium, then select Practice (Q) on the Kanji tab |
| Pause / close overlay | Escape |
| Complete cinematic text / next panel | Left click outside Skip |
| Skip cinematic | Click Skip, S or Escape |
| Continue or retry | Enter on the outcome screen |

The guide draws the kanji once when a handwriting prompt starts. Press Help to replay it. Drawing has no timer. Strict mode checks lesson stroke order and direction. Master, SFX and BGM volume persist between sessions. Save files are local to the user, under `%LOCALAPPDATA%\INKSLAYER\saves` on Windows.

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

The Fill the Blank build uses monochrome pixel placeholders and DotGothic16 pixel text for Latin and Japanese. See the [quiz list](docs/FILL_THE_BLANK_QUIZZES.md) and [battle layout](docs/FILL_THE_BLANK_LAYOUT.md). Pronunciation recordings, human Japanese-language review and beginner handwriting calibration are pending.

The Glossarium has tabs for all 39 tested Kanji, their contextual hiragana readings, and Kotoba's four katakana answers plus eight campaign vocabulary words. Switch tabs with 1–3 and browse with Left / Right.

See [GDD](docs/GDD.md), [architecture](docs/ARCHITECTURE.md), [roadmap](docs/ROADMAP.md), [recognition](docs/RECOGNITION.md), and [asset manifest](docs/ASSET_MANIFEST.md). Font and KanjiVG attribution/licenses are in [credits](assets/CREDITS.md), also included in the JAR.
