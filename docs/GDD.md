# INKSLAYER — game design baseline

Rencana kampanye terbaru dengan lima elemen, empat stage, counter bertahan, kana, save, audio dan cinematic tersedia di [BUILD_PLAN.md](BUILD_PLAN.md). Rincian implementasi prototipe di bawah menjadi baseline pengembangan.

The desktop campaign implements four stages, five spell elements, 15 Kanji, hiragana reading and eight katakana words. See [BUILD_PLAN.md](BUILD_PLAN.md) for the campaign design and [TESTING.md](TESTING.md) for verification and recognition limitations. The prototype notes below describe the earlier one-encounter build.

## Established direction

Java desktop 2D side-scrolling adventure with turn-based combat. Pixel art, dark comedy, fantasy, and Japanese education for complete beginners. Exploration, walking, combat, and storytelling share the play loop. Mr. Kotoba is the antagonist.

Core loop: encounter a monster → understand its elemental weakness → draw a learned Kanji with the mouse → submit → receive understandable recognition feedback → cast the associated spell → explore and revisit the learning through play.

Every monster has an elemental weakness. The master brief establishes a provisional fictional cycle: Water > Fire > Wood > Earth > Water. Only Water and a Fire enemy are playable in the MVP. The complete prototype matrix is stored in `assets/data/level.json`: counter 2×, reverse counter/same element 0.5×, other pairs 1×. This convention is game fiction, not a Japanese cultural or linguistic claim. There is no mana cost. The Fire monster has 120 HP and requires two Water casts. It counters a nonlethal cast for 1 HP after 0.75 seconds.

## Proposed first playable slice

One practice space, one connected combat room, one enemy, one learned Kanji/spell, a checkpoint, and a return route. Use placeholder art. Prove input capture, tolerant recognition, teaching feedback, and one elemental interaction before expanding content. A later slice introduces a second element and a meaningful choice.

Implemented spell UI: approach the Fire monster to start combat. The player draws during their turn with undo, clear, cancel and explicit submission. Invalid submissions retain the drawing and the turn. The monster acts once after a nonlethal accepted cast. Q opens safe practice while walking.

## Learning and accessibility

Teach meaning, a context-appropriate reading, and the drawing separately; guide tracing → supported recall → unassisted recall. Require review before opening mandatory Kanji gates. Allow practice without damage or resource loss. Beginner mode should offer time assistance, stroke demonstrations, and calm, actionable feedback. Use text/icon/shape in addition to elemental colors; make UI and Japanese glyphs scalable and legible.

Combat recognition can accept an identifiable shape while coaching noncanonical order. A strict practice exercise may require canonical count, direction, and order. Never describe lenient combat acceptance as proof of correct handwriting.

## Evaluation and unresolved choices

Measure novice completion, retries, undo/clear use, recognition false accepts/rejects, recognition latency, combat readability, and delayed recall without prompts. Targets require a representative pilot; no recognition accuracy or educational efficacy is established yet.

Current prototype metrics: 155 world units/s walk speed, 60 Hz simulation, 640 world units of horizontal visibility and a 1280×720 UI viewport. Windows desktop target.
