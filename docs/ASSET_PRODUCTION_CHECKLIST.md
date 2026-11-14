# INKSLAYER! Asset Production Checklist

The campaign contains four stages and 40 encounters. The current build uses code-drawn pixel art, seven music loops, and 13 sound effects. Check off each asset when its final version has been reviewed in game. See the [build plan](BUILD_PLAN.md), [campaign roster](../assets/data/campaign.json), and [audio inventory](../assets/audio/CREDITS.md).

## Draw

### Characters and monsters

- [ ] **Player.** Side-view protagonist with a readable face, red scarf, and pen. Use the same design for exploration, battle, and cinematics. Draw a close-up of the hand holding the pen for the opening and ending.
- [ ] **Stage 1 · Elements.** Ember, Iron, Thorn, Clay, and Mist. Make upgraded variants for Ember II, Iron II, Thorn II, and Clay II. Give the Gate Inspector a distinct silhouette.
- [ ] **Stage 2 · Pets.** Ember Hound, Moss Cat, Iron Bird, Mist Fish, and Clay Horse. Make Sentinel variants for Hound, Cat, Bird, and Fish. Use the horse as a base for the Stable Inspector, with a distinct silhouette or costume.
- [ ] **Stage 3 · Weather.** Rain, Gale, Snow, Cloud, and Dusk Wisps. Make Sentinel variants for Rain, Gale, Snow, and Cloud. Give the Weather Inspector a distinct silhouette.
- [ ] **Stage 4 · Examination.** One Examiner base with nine readable variants using masks, hats, robes, or seals. Draw Mr. Kotoba separately, including his ledger and three exam seals.
- [ ] **Cutscene cast.** Inspectors, a small set of townspeople, and the student who receives the pen. Reuse gameplay sprites for wide shots.

This production breakdown covers all 40 encounters with 16 shared monster bases, three stage inspectors, and Kotoba. The other encounters can share base animations with palette, silhouette, or costume changes.

### Environments

- [ ] **Elements.** Inspection gate, repeatable ground strip, and stage props.
- [ ] **Pets.** Animal district, repeatable ground strip, and stage props.
- [ ] **Weather.** Road through the storm, repeatable ground strip, and stage props.
- [ ] **Examination.** Kotoba's hall, repeatable ground strip, and stage props.
- [ ] **Four battle treatments.** One per stage. Frame the player on the left and the larger monster on the right, with a readable floor line and a clear handwriting area below.

### Menus, combat UI, and lessons

- [ ] Title logo with the exclamation mark, title background, and app icon.
- [ ] Menu selection and focus states for **New**, **Save**, **Settings**, and **Exit**.
- [ ] Empty, occupied, selected, and unavailable save-slot states.
- [ ] Master, SFX, and BGM sliders, plus the display mode selector.
- [ ] Pause and journal panels, answer cards, and victory and retry states.
- [ ] Five elemental icons with distinct shapes for 水, 火, 金, 木, and 土. Create matching attack telegraphs and counter indicators.
- [ ] Lesson illustrations for pen, ink, pet, cage, hamster, coat, boots, and towel. Reuse animal and weather art where it clearly represents the vocabulary.
- [ ] Handwriting canvas frame, tracing highlight, stroke-start marker, and accepted and retry states. Use the existing Japanese font and [sourced stroke paths](../assets/data/kanji/TEMPLATES.md) for glyphs and guides.

### Cutscene boards

Draw six opening and six ending panels. Reuse backgrounds with new crops, poses, and props where practical. Leave room at the bottom for captions and **Next / Skip**.

| Panel | Opening | Ending |
| --- | --- | --- |
| 1 | ☐ City at dawn and the language decree | ☐ Kotoba turns the last page |
| 2 | ☐ Inspectors make their rounds with ledgers | ☐ The final seal cracks and Kotoba puts down his pen |
| 3 | ☐ The inspector passes the protagonist's door | ☐ The gates open and townspeople gather |
| 4 | ☐ A hand traces 水 from a scrap of paper | ☐ Inspection notices come down and a class sign goes up |
| 5 | ☐ A glimpse of the animal district and storm road | ☐ A student watches the protagonist write 水 |
| 6 | ☐ The protagonist pockets the pen and heads to the first gate | ☐ The pen changes hands |

## Animate

- [ ] **Player.** Idle, walk, draw or cast, guard, successful counter, hit reaction, defeat, and victory. Give the scarf a simple secondary motion shared across poses.
- [ ] **Monster bases.** Idle, attack anticipation, attack release, recovery, hit reaction, and defeat. Give hounds and horses a grounded stance, birds a flap, fish a swim or hover, and Wisps a slow drift. Variants can share these loops.
- [ ] **Mr. Kotoba.** Ledger idle, page turn, exam seal reveal, spell wind-up and release, hit reaction, round transition, final seal break, and defeat. Use one casting pose with five different elemental effects.
- [ ] **Water.** Telegraph, travelling arc, impact splash, and counter break.
- [ ] **Fire.** Telegraph, travelling flame, impact burst, and counter break.
- [ ] **Metal.** Telegraph, travelling shards, impact, and counter break.
- [ ] **Wood.** Telegraph, branching growth, impact, and counter break.
- [ ] **Earth.** Telegraph, rising earth, impact, and counter break.
- [ ] **Writing and lessons.** Stroke-direction marker following the sourced paths, ink appearing under the cursor, guide reveal, answer acceptance, and retry feedback.
- [ ] **Stage loops.** Gate lights, rain, drifting snow, and moving exam seals where appropriate.
- [ ] **Cinematics.** Camera pans, layered motion, page turns, gate opening, the water spill, and the pen handoff.

The design viewport is **1280×720**, with a **584×250** handwriting area. Test one player sprite and one large enemy at their actual display sizes before producing the roster. Keep a consistent baseline at the feet. Keep telegraphs and spell impacts above the writing area.

## Compose and record

### Music

- [ ] Title loop for the main menu and opening.
- [ ] Stage 1 · Elements loop.
- [ ] Stage 2 · Pets loop.
- [ ] Stage 3 · Weather loop.
- [ ] Stage 4 · Examination loop.
- [ ] Mr. Kotoba boss loop.
- [ ] Ending track.

A short opening cue or a variation of the title theme is optional.

### Sound effects

These 13 cues already have playback hooks in the build.

- [ ] Menu action.
- [ ] Pen stroke.
- [ ] Recognition accepted.
- [ ] Recognition retry.
- [ ] Water spell.
- [ ] Fire spell.
- [ ] Metal spell.
- [ ] Wood spell.
- [ ] Earth spell.
- [ ] Successful counter.
- [ ] Hit.
- [ ] Victory.
- [ ] Defeat.

Optional polish cues include a guard impact, a monster wind-up cue, Kotoba's page turn, the seal crack, and cutscene sounds that carry a story beat. Listen to the busiest battle with music and effects together. Check that the incoming attack still reads visually with audio muted.

### Japanese pronunciation

Record 23 clips using the exact readings in the lesson data. Have a fluent Japanese speaker review the recordings before they ship. Keep the clips separate from BGM and effects in the source files. See the [lesson data](../assets/data/campaign.json) and [language references](../assets/data/learning/SOURCES.md).

| Set | Clips |
| --- | --- |
| Elements | ☐ 水（みず）, ☐ 火（ひ）, ☐ 金（きん）, ☐ 木（き）, ☐ 土（つち） |
| Pets | ☐ 犬（いぬ）, ☐ 猫（ねこ）, ☐ 鳥（とり）, ☐ 魚（さかな）, ☐ 馬（うま） |
| Weather | ☐ 雨（あめ）, ☐ 風（かぜ）, ☐ 雪（ゆき）, ☐ 雲（くも）, ☐ 空（そら） |
| Katakana | ☐ ペン, ☐ インク, ☐ ペット, ☐ ケージ, ☐ ハムスター, ☐ コート, ☐ ブーツ, ☐ タオル |

## First production pass

Start with the player, Ember, the Stage 1 arena, Water and Fire effects, one music loop, and the cast, counter, and retry sounds. Review this encounter in game for sprite size, animation timing, writing readability, and audio balance before scaling up.
