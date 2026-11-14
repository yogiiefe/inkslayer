---
name: pixel-art-pipeline
description: Prepare and integrate INKSLAYER pixel sprites, tiles, sprite sheets, animation metadata, atlases and readable VFX in libGDX. Use for asset production and import rather than recognizer templates.
---
# Pixel art pipeline

## Workflow and implementation

Inspect existing assets before setting a style. Propose a base logical resolution, pixels-per-world-unit, tile size, palette, outline/light rules and animation timing; record accepted choices in the GDD. Do not assume a 16px tile or generate replacements for established art without the task requiring it.

Maintain source art separately from exported runtime resources. Each asset needs a stable ID, source/license/attribution, pivot, logical bounds, frame names/durations and export settings. Use nearest filtering for pixel sprites and integer scaling where possible; allow Japanese text a separate legible scaling policy. Keep collision boxes in gameplay data, not inferred from transparent sprite extents.

Pack deterministic atlases with padding/extrusion appropriate to sampling. Validate region names, trim offsets, pivots and alpha; avoid generating duplicate texture copies. Use `TextureAtlas`/`Animation` or the existing equivalent; drive state animation from simulation state with explicit looping and transition rules. Gameplay hit timing cannot depend solely on a frame's display callback.

Make VFX communicate element, hit, miss and vulnerability using shape and motion as well as hue. Limit brightness, screen shake, particle count and flashing with accessibility settings. The handwriting panel and telegraphs must remain readable during effects. Integrate a small asset family in a representative scene before scaling production.

## Best practices and anti-patterns

Use explicit disposal/asset ownership from the Java skill. Avoid inconsistent per-sprite pixel density, fractional camera shimmer, white fringes from wrong alpha handling, frame-rate-based animation, silent atlas region renames and unlicensed font/sprite downloads.

## Validation and Definition of Done

Inspect exports on light/dark backgrounds at supported scales, animation pivots at transitions, atlas seams, mirrored frames and packaging from outside the source tree. Verify Japanese glyph coverage and fallback readability. Done means reproducible export settings, complete provenance, correct region/timing metadata, readable in-game results and bounded asset memory. Generated images alone are not an integrated animation pipeline.
