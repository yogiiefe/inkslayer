# INKSLAYER audio

All WAV files here are original instrumental compositions and effects synthesized for INKSLAYER with `tools/ComposeGameAudio.java`. The generator uses deterministic oscillators and procedural percussion at 22,050 Hz, mono PCM. There are no third-party samples, recordings, or music.

- `bgm/` contains seven loops for the title, four stages, boss and ending.
- `sfx/` contains interface, handwriting, recognition, elemental, counter, hit and outcome cues.

Use a Java 21 JDK to regenerate the same assets from the project root.

```powershell
java tools/ComposeGameAudio.java assets/audio
```
