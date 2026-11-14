---
name: kanji-handwriting-recognition
description: Build or evaluate INKSLAYER mouse-stroke capture, normalization, resampling, deterministic multistroke Kanji matching, rejection thresholds, confidence, and template data. Use for shape recognition; stroke pedagogy is separate.
---
# Kanji handwriting recognition

Highest-priority technical subsystem. Read [recognition-contract.md](references/recognition-contract.md) when changing capture, geometry, templates, scoring or benchmarks. Read [sources.md](references/sources.md) before importing templates or linguistic data.

## Workflow

1. Define supported Kanji, template provenance and a writer-disjoint evaluation corpus before selecting thresholds. Keep a reject/unknown outcome. Do not claim general Japanese handwriting recognition from a tiny spell vocabulary.
2. Capture one ordered stroke per primary-button press/drag/release inside a stable drawing viewport. Preserve pen-up boundaries and timestamps. UI consumes drawing input; it must not leak into world attacks. Explicit submit ends a character; idle time alone must not guess beginner completion.
3. Keep raw samples immutable; normalize and resample a snapshot. Apply the contract's common bounding box and per-stroke arc-length resampling. Preserve direction and stroke identity even if the shape matcher later uses a tolerant correspondence.
4. Implement a lightweight ordered multistroke distance baseline in plain Java. If combat tolerance requires order-insensitive shape comparison, compare a bounded assignment variant or a point-cloud baseline while retaining the original stroke stream for the independent validator. Measure confusion before adding complexity. Never enumerate all `n! * 2^n` stroke permutations.
5. Return ranked candidates, raw distances, top-two margin, rejection reason and template/policy version. Similarity is not probability. Calibrate per-character or cohort thresholds and margin gates on held-out data; freeze before final evaluation.
6. Cache normalized templates, shortlist by safe coarse features, bound work by template/stroke/point counts, and benchmark warm and cold latency on target desktop hardware. Keep recognition off the render critical path if measured cost requires it; revision IDs prevent late results from casting stale drawings.

## Best practices and anti-patterns

Translation and uniform scale tolerance help handwriting; arbitrary rotation/reflection, per-stroke recentering and independent X/Y stretching can erase Kanji distinctions. Never force a best match, calibrate on test data, call a distance a confidence percentage, or silently remove meaningful dots. Keep engine/UI types outside the recognizer.

## Validation and Definition of Done

Use the reference's positive/negative corpus and report false acceptance, false rejection, confusion matrix, ambiguity rate and p50/p95/p99 latency separately. Test blank, tap, repeated points, malformed nonfinite input, resize/focus loss, undo/clear and stale results. Done means reproducible metrics with dataset provenance, explicit unknown handling, bounded CPU/memory, reviewed source licenses, and a stable recognition result contract. No fixed accuracy claim is valid before representative beginner testing.
