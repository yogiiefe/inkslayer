# Deterministic recognition contract

## Capture and lifecycle

Proposed data contract: `Point(x,y,timeNanos)`, `Stroke(points)`, `Drawing(strokes,revision)`. Store normalized drawing-surface coordinates with one documented Y convention, independent of camera/world transforms. Convert through the UI viewport, including letterboxing and HiDPI; freeze the transform during an active drawing or cancel that stroke on resize. Primary pointer owns the stroke until release. Focus loss cancels the active stroke; never connect a later drag to it.

Undo removes the last completed stroke, clear removes all and both increment revision; cancel active input explicitly. Invalidate cached candidates on any edit. Choose limits for strokes, points and capture duration; reject excess safely with a user message. Deduplicate consecutive coincident points, but retain raw data for diagnostics and preserve short intentional dot strokes. Do not log user drawings by default; diagnostic collection is local/opt-in with a retention limit.

## Geometry pipeline

Reject nonfinite coordinates. Compute one bounding box over the complete character, `w=maxX-minX`, `h=maxY-minY`. If `max(w,h)` is below an explicit meaningful-size threshold, return insufficient input. Map each point by subtracting the common box center and dividing both axes by `max(w,h)`, then translating into the unit square. A horizontal/vertical stroke has zero size on one axis but still a valid nonzero scale. Never divide independently by width and height. Do not rotate to an indicative angle; orientation distinguishes writing. Do not normalize each stroke separately because relative position is evidence.

Resample each nonzero-length stroke to a configurable N (start with 32 for a small corpus), evenly spaced along its cumulative arc length, including both endpoints. Never interpolate across pen-up. Handle segments of length zero without infinite loops or division by zero. For near-zero dot strokes use an explicit dot representation/length flag rather than normalizing noise into a long line. Keep the original stroke count and endpoints alongside resampled points.

## Matching and result

Baseline for equal-count candidates: mean point distance between corresponding resampled strokes, aggregated with documented stroke weights. Retain relative-length, endpoint and bounding-box features as inspectable evidence; do not let one long stroke erase a missing short stroke. Multiple reviewed examples can represent permitted handwriting variants. A count mismatch is a diagnostic, not a reason to relabel arbitrary scribbles.

An optional tolerant shape matcher may use minimum-cost stroke assignment with per-stroke forward/reverse distances (bounded polynomial assignment), or a point-cloud distance; order and reversal evidence must then be evaluated separately from the preserved input. Any extra/missing-stroke penalty and candidate retrieval rule requires validation against nearby Kanji. Top K and margins must compare distinct characters, not two templates of the same character.

For a normalized distance d, a bounded monotonic display similarity may be `max(0,1-d/s)` for a documented scale s; it is not calibrated probability. Require distance threshold AND an adequate second-character margin; with only one candidate, use negative/open-set evaluation rather than inventing a margin. Return unknown/ambiguous without casting. Keep diagnostic count/order/direction findings separate from shape scores and spell policy.

Suggested result fields: drawing revision, candidate IDs and distances, best distinct-character margin (optional), acceptedShape or rejection code, template database version, recognizer version, elapsed time. No combat costs or Japanese teaching messages belong inside the geometry service.

## Template database

Version a schema containing a stable Kanji ID, Unicode code point(s), ordered strokes in a documented coordinate space, permitted variant ID, source URL/revision, license/attribution, reviewer status, and preprocessing version. Keep meanings/readings in separately sourced educational metadata and spell mappings in game data. Reject duplicate IDs, missing strokes, empty paths, out-of-bounds/nonfinite points and unsupported schema versions at load. Imported SVG must be parsed as data without external entities or remote resources; flatten curves with a recorded error tolerance before resampling. Source paths are diagrams, not empirical distributions of novice handwriting.

## Evaluation protocol

Use consented beginner mouse drawings from multiple writers; split by writer, not perturbed copies. Calibration and evaluation sets remain disjoint. Report denominators: false acceptance on invalid/unknown/other-character inputs, false rejection on supported valid drawings, and wrong-character substitution separately. Include ambiguous pairs within and outside the spell set, wrong count, swapped order, reversed strokes, dots, scribbles, Latin letters, mirror/rotated shapes, shaky curves, scale/translation changes and slow/fast sampling. Synthetic transforms exercise invariants but cannot establish beginner accuracy.

Measure geometry and pedagogical validator independently. Replay immutable fixtures for determinism; do not change thresholds merely to satisfy one example. Record CPU, JVM, dataset/template/policy versions, preprocessing cost, matching time, worst configured workload and memory caps. An initial latency budget is a proposal requiring measurement, not an already achieved property.
