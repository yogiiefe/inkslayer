---
name: enemy-ai-development
description: Build INKSLAYER enemy perception, finite state machines, patrol and pursuit, telegraphed attacks, navigation limits, and recovery. Use for ordinary enemies rather than boss phase design.
---
# Enemy AI development

## Workflow and implementation

Specify a compact state transition table (such as Patrol, Alert, Chase, Telegraph, Attack, Recover, Stunned, Dead) with entry/exit actions and interruption priority. Dead overrides all, while stun and attack interruption rules are explicit. Use a pure decision step taking a perception snapshot, elapsed simulation time and seeded RNG, returning movement/attack intentions.

Perception needs ranges, facing/line-of-sight policy, memory duration and hysteresis so range boundaries do not chatter. Distinguish visible player, last-known position and no target. Navigation must use the controller's reachable gaps/heights and avoid walking off ledges unless that enemy explicitly can. Begin with waypoint patrol and bounded pursuit before implementing generalized pathfinding.

Telegraph startup, active hit window and recovery are data. Attacks emit a domain attack ID once and cannot skip telegraphs because the render frame was long. Define behavior when the player enters drawing mode; enemy simulation follows the shared clock policy rather than independently reading wall time. Keep bosses' encounter scripts out of ordinary enemy inheritance chains.

## Best practices and anti-patterns

Expose current state, target and transition reason in a debug overlay. Make difficulty from readable spacing/timing rather than perfect input-reading. Avoid nested booleans, per-frame random decisions, transitions recursively calling transitions, unbounded A* work in a tick, and continuing attacks after death or despawn.

## Validation and Definition of Done

Test every transition and forbidden transition, timer boundaries, target loss, ledges, unreachable player, stun/death during attack and repeated spawn/despawn. Seeded replay must reproduce decisions. Playtest escape and drawing opportunities with novice casting times. Done means no stuck states, state-owned resources are cleaned up, attacks remain telegraphed under timing stress, and enemy weaknesses are supplied through combat data.
