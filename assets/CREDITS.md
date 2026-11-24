# INKSLAYER! credits

Original prototype code-drawn scenery and characters: INKSLAYER project.

The cinematic alphabet in `fonts/cinematic.png` is the user-supplied `Font.png`, imported unchanged. `fonts/cinematic.fnt` maps A–Z from rows 108–114, preserving their pixel shapes and spacing. Story text uses the sample line's centered position at row 77 of the 320×180 reference. Punctuation and Japanese characters use the included DotGothic16 font as a fallback.

The cinematic Skip animation uses the user-supplied `SkipAnim.png`, imported unchanged. Its two arrow poses occupy rows 4–10 and 14–20. Hover motion and label dimming share the main menu's timing, with a fixed label and a two-pixel arrow travel on the 320×180 art grid.

The Settings, Saves, Keybinds, New Game and Pause art uses the five user-supplied `*Layout.png` sheets. The game crops the illustrated panels and draws live values and control states on their 320×180 art grid.
The scrolling Keybinds panel uses `keybindsLayout.png` for its header, labels, brush shape and button artwork.

- [Campaign stroke templates and KanjiVG provenance](data/kanji/TEMPLATES.md)
- [Campaign Japanese reading and vocabulary references](data/learning/SOURCES.md)
- [Original music and sound effects](audio/CREDITS.md)
- [Fill the Blank stroke template provenance](data/kanji/FILL_TEMPLATES.md)
- [DotGothic16 license](fonts/DotGothic16-OFL.txt). Pixel font by the DotGothic16 Project Authors, SIL OFL 1.1. Original TTF from [Google Fonts revision fa826bb](https://github.com/google/fonts/tree/fa826bb2a3e26df8c1bee99351fc0d039aeed7d2/ofl/dotgothic16). TTF SHA-256: `3ad9af88726d42b40f7f365f0dcac785af73cf20ea6f1d5b44e57cc21150b8f1`. License SHA-256: `b6630c61ea078cacd7fabe37d14ffe557a0b45b06683374a9aa9e24262993e33`.

KanjiVG water stroke paths: Copyright (C) 2009/2010/2011 Ulrich Apel.
https://kanjivg.tagaini.net/
Source: https://github.com/KanjiVG/kanjivg/blob/master/kanji/06c34.svg
License: Creative Commons Attribution-ShareAlike 3.0, https://creativecommons.org/licenses/by-sa/3.0/
Original SVG and notices are retained in data/kanji/water-source.svg. The derived water.json transcribes relative/SVG smooth cubic commands into absolute control-point arrays; those derived template data remain CC BY-SA 3.0. License text: data/kanji/KanjiVG-LICENSE.txt. No warranty; see retained license.

Water reading/stroke-count cross-check: https://www.kanjipedia.jp/kanji/0003723800/
English meaning/reading cross-check: https://www.tuttlepublishing.com/content/docs/9784805314586/JLPT%20KANJI_Flashcard.pdf
Dictionary pages were consulted; their text and illustrations are not redistributed.

libGDX and declared extensions retain their upstream licenses. The generated desktop StartupHelper retains its Apache-2.0 notice in source.
