---
name: kanji-stroke-order-validation
description: Validate or teach Kanji stroke count, direction, and order from recorded strokes against sourced canonical variants. Use for handwriting coaching and strict practice, not general shape recognition.
---
# Kanji stroke order validation

## Workflow and implementation

Accept the raw ordered stroke sequence plus normalized points and a candidate Kanji ID. Reuse preprocessing from the recognition skill; preserve the recording order and endpoints. Read its [source policy](../kanji-handwriting-recognition/references/sources.md) before defining canonical paths. Do not regenerate stroke order from visual intuition or universal top-to-bottom slogans.

Load versioned canonical/explicitly permitted variant sequences with provenance. Validate count first and return expected/observed counts; do not index a short input blindly. Then align observed stroke shapes/positions to canonical strokes using bounded assignment with an unmatched penalty. Retain ambiguity where multiple alignments are near equal. Matching canonical index i to observed position j makes swapped strokes diagnosable without falsely reporting every later stroke as wrong.

For each confidently aligned stroke compare forward versus reverse resampled-path distance, endpoint regions and tangent evidence. Curved strokes need path evidence; an overall endpoint vector is insufficient. A reviewed canonical dot-like stroke may mark direction `notApplicable`; inadequate evidence on a directional stroke is `inconclusive`, which cannot pass strict direction validation. Derive order correctness from the observed sequence of canonical indices. A strict valid result requires count, all applicable direction checks, order and identifiable shape; a familiar shape alone cannot pass.

Return structured findings: canonical variant ID, count result, per-stroke alignment, reversal findings, first confident order deviation, ambiguity, and source version. The spellcasting policy decides consequences. Teach one actionable correction at a time, highlight the next expected stroke, allow slow demonstration, undo and retry. Do not erase a learner's drawing automatically.

## Best practices and anti-patterns

Prefer explicit permitted variants reviewed by a Japanese-literate reviewer over broad heuristic exceptions. Keep motor tolerance separate from pedagogical rules. Avoid sorting strokes geometrically before validation, applying $N permutations then declaring order correct, accepting any shape with the right count, or punishing every coaching issue during combat.

## Validation and Definition of Done

Test canonical sequences, accepted variants, adjacent swaps, reversed strokes, missing/extra strokes, similar short strokes and curved paths. A swapped drawing may retain its shape score while failing order; a reversed stroke must be diagnosed independently. Test ambiguous alignment and inconclusive dot direction without confident false feedback. Done means sourced variants, explainable findings, independent count/order/direction fixtures, and novice-readable correction text reviewed with the learning skill.
