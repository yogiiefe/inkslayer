# INKSLAYER audio

The synthesized WAV files here are original instrumental compositions and effects created for INKSLAYER with `tools/ComposeGameAudio.java`. The generator uses deterministic oscillators and procedural percussion at 22,050 Hz, mono PCM.

- `bgm/` contains six synthesized WAV loops for the title, four stages and boss, plus the user-supplied `TEMPOREX - Numbers (Instrumental).mp3` for chapter-opening and epilogue cinematics.
- `sfx/` contains cues for interface actions, volume sliders, handwriting, recognition acceptance, recognition retry, victory and Skip.
- `sfx/slider.wav` is an original 55 ms tick for the Master, SFX and BGM sliders. Other menu buttons share `sfx/ui.wav` with the title menu.
- `sfx/skip.wav` is an original 340 ms ascending three-note chime, authored for the cinematic Skip controls.
- `sfx/typewriter.wav` is an original 28 ms mechanical click for the cinematic text reveal.
- `sfx/huss.wav` is the PCM conversion of the user-supplied `Huss Huss Huss.mp3` (WebM/Opus), used for the two "HUSS HUSS HUSS" cinematic slides.

The story track loops across cinematic slides. Music entering and leaving a cinematic fades over one second per fade, multiplied by the saved Master and BGM levels.

Use a Java 21 JDK to regenerate the synthesized assets from the project root.

```powershell
java tools/ComposeGameAudio.java assets/audio
```
