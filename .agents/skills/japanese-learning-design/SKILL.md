---
name: japanese-learning-design
description: Design or review INKSLAYER beginner Japanese curriculum, verified Kanji metadata, reading and meaning instruction, spaced recall, localization, and educational feedback. Use when teaching content or learning progression changes.
---
# Japanese learning design

## Workflow and implementation

Define a measurable learning objective for each encounter: identify meaning, recall a contextual reading, recognize form, or reproduce strokes. These are different outcomes; combat victory does not prove them all. Start from a small vocabulary with pedagogical utility and visual discriminability rather than selecting characters only to fit elements.

Read the recognition skill's [source policy](../kanji-handwriting-recognition/references/sources.md) before adding entries. For each stable ID record character/code point, contextual kana reading, gloss, example word if taught, stroke count/order source, permitted variants, source revision/license and review status. Distinguish on/kun readings and word-specific pronunciations; do not present every dictionary reading as a beginner requirement. Require human Japanese-literate review before calling shipping content verified.

Sequence introduction with meaning/context → stroke demonstration → supported tracing → cued recall → unassisted recall → delayed mixed review. Fade hints based on observed success, not just elapsed time. Track prompt level and attempts separately from mastery; a traced success cannot count as independent recall. Use simple scheduling from last review, independent successes and errors before adding a complex learner model.

Provide one actionable correction, replay/undo and a low-pressure practice option. Treat unclear drawing, wrong character and noncanonical order differently. Use kana support and optional romaji scaffolding with a fade plan; never require knowing kana before it has been taught. Keep humor directed at fiction rather than learner mistakes.

Localize through stable message IDs, Unicode text, reviewed fonts and content-sized layouts. Distinguish player interface language from Japanese study content. Furigana or readings must remain associated with the correct word when wrapping; code-point iteration must not split surrogate pairs or combining sequences during text reveal.

## Best practices and anti-patterns

Separate motor skill, recognition errors and knowledge in measurements. Avoid unsourced AI-generated stroke tables, punishing speed as literacy, ambiguous glosses without context, color-only teaching cues and claiming educational efficacy from a short satisfaction test.

## Validation and Definition of Done

Review every shipped teaching entry against traceable sources; test hint fading, resumed learning state and mixed delayed recall with novices. Include motor/vision assistance and untimed practice. Done means no unreviewed facts are labeled verified, each gate has prerequisites and recovery, mastery signals distinguish assisted from independent recall, and reported learning results include methods and limitations.
