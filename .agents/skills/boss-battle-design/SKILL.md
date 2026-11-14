---
name: boss-battle-design
description: Design and implement INKSLAYER boss encounters, phase changes, telegraphs, safe casting windows, fairness, and retry pacing, including proposed Mr. Kotoba encounters.
---
# Boss battle design

## Workflow and implementation

Define the encounter's learning goal and taught spell prerequisites before attacks. Mr. Kotoba's role as antagonist is canon; exact powers, biography and fight phases are proposals until accepted in the lore/GDD. Design an arena with verified movement escape paths, readable drawing windows and a retry checkpoint.

Make an attack/phase table: trigger, tell, startup, active geometry, recovery, safe response, elemental opportunity, interruptions and next-state constraints. Phase thresholds fire once and have a documented policy for overkill damage and pending attacks. Keep encounter state, animation and collision timing separately inspectable; clean up projectiles/hazards on reset.

Budget a casting window from measured beginner drawing plus submit/recognition/reaction time. Telegraph the elemental opportunity before requiring a response, and avoid introducing an unlearned Kanji under unavoidable damage. Sequence seeded attack patterns with cooldown constraints to prevent impossible combinations. Escalate one demand at a time (space, timing or recall) rather than shortening every window together.

Offer practice/retry without replaying long dialogue. Use optional assistance according to accessibility settings, and distinguish a combat failure from a recognizer rejection in diagnostics. VFX, music and dialogue must leave threat geometry and the handwriting panel legible.

## Best practices and anti-patterns

Prototype one complete attack with greyboxes and novice timing before a multi-phase spectacle. Avoid invulnerable downtime without information, arbitrary hidden weakness swaps, damage during noninteractive transitions, random untelegraphed counters and health scaling as the sole difficulty curve.

## Validation and Definition of Done

Test threshold crossings including skipped thresholds, death during transitions, restart from each phase, hazard cleanup, draw-mode clocks and worst permitted attack combinations. Playtest avoidance and casting using observed beginner times. Done means each attack has an available readable response, retries restore a known clean state, phase rules are deterministic, and progression uses material already taught.
