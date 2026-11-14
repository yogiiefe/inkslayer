---
name: metroidvania-world-design
description: Design INKSLAYER room graphs, ability and Kanji gates, shortcuts, checkpoints, exploration rewards, and softlock checks. Use for progression structure rather than individual movement physics.
---
# Metroidvania world design

## Workflow and implementation

Represent rooms/checkpoints as nodes and traversals as directed edges with requirements, consumed resources, return policy and story flags. Inventory traversal abilities separately from learned Kanji and combat upgrades. Label hard gates, optional challenges, shortcuts and one-way drops. Use measured movement limits from the platformer skill when laying out jumps.

For each unlock, run reachability over `(room, abilities, persistent flags)` states, acquiring reachable unlocks until fixed point. A required unlock cannot be exclusively behind its own gate. Consumable keys require remaining-key state, not a simple ability set. For one-way transitions verify a reachable checkpoint/exit with the capabilities actually available at that point, including death/respawn rules.

Before a mandatory Kanji gate provide teaching, safe practice and a readable reminder; ensure assistance can recover a learner who forgot it. Gate progress on supported learned characters, not on an undocumented stroke-order exception. Reserve optional routes for harder recall tests. Use loops/shortcuts to revisit vocabulary without repeating long traversal after each recognition error.

Greybox each room with entry view, destination cue, encounter purpose, safe landing and camera framing. Environmental storytelling should support a specific inference with an accessible alternate clue; update accepted facts in `docs/LORE.md`. Mark critical paths through landmarks as well as map colors.

## Best practices and anti-patterns

Keep one progression graph authoritative. Avoid local tile edits that bypass the graph review, irreversible spending of a mandatory key without recovery, mandatory sequence breaks, return routes relying on abilities acquired later, and map expansion before the first complete loop is playable.

## Validation and Definition of Done

Test minimal-capability traversal, every one-way edge, save/respawn on both sides of gates, consumable-key depletion and revisits after unlock. Enumerate intended sequence breaks separately from exploits. Done means the critical path is reachable from a fresh state, mandatory upgrades are not self-gated, all committed drops have recovery, and a beginner understands what to learn next.
