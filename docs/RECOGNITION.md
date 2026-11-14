# Recognition implementation

Status: implemented experimental prototype, 水 only. Not validated as a general Japanese handwriting recognizer or learning assessment.

Canvas capture stores separate ordered strokes in UI-local Y-down coordinates, with a maximum of 12 strokes and 2048 points per stroke. Mouse movement is sampled by input events, not physics ticks. Consecutive near-duplicate points are filtered. Undo removes active/last stroke; clear and cancel discard input. Resize/focus loss cancels active capture. UI viewport conversion accounts for letterboxing. Recognition is synchronous: no worker/callback can cast an obsolete request.

The pure-Java recognizer requires four strokes, finite input, meaningful nonzero stroke lengths and a bounding-box span of at least 12 canvas units. It normalizes the whole character using translation and one common scale, without rotation, reflection or per-stroke recentering. Each stroke is resampled to 32 points along arc length, retaining pen-up boundaries.

Matching uses a bounded four-stroke minimum-cost assignment (16 subset states) with forward/reverse path distances. The mean and worst stroke distances must both pass gates: initial mean threshold 0.145; worst limit 1.65 times that. These are provisional synthetic-test parameters, not calibrated beginner thresholds. Shape matching may tolerate reordered/reversed strokes, but independent order/direction findings remain available. Gentle combat coaching accepts identifiable shapes; strict practice also requires the canonical order/direction. Result distance is shown only in debug mode; it is not a probability.

There is only one character candidate, so no invented top-two margin is used. Unknown rejection is evaluated with negative fixtures. Multiple-character ambiguity/margin logic remains future work. No ML/API, arbitrary stroke permutations, cloud service or background thread was introduced.

Templates are sampled from the preserved KanjiVG SVG's four cubic paths, transcribed to absolute cubic coordinates in JSON. Source/derived data remain CC BY-SA 3.0; see [credits](../assets/CREDITS.md). The SVG is retained as data and is not loaded through an XML parser or allowed to fetch its external DTD. Source curves are not a corpus of novice handwriting.

Next evidence needed: consented beginner mouse samples split by writer, nearby-character negatives, rejection/false-accept/confusion counts, and measured latency on declared hardware. Current tests establish behavior on their fixtures only.
