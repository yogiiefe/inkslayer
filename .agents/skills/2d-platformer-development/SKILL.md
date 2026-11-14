---
name: 2d-platformer-development
description: Build or tune INKSLAYER movement, collision, jumping, moving platforms, camera follow, checkpoints, and platforming responsiveness. Excludes world progression graph design.
---
# 2D platformer development

## Workflow and implementation

Define player metrics before code: world unit, collider dimensions, horizontal speed, acceleration/deceleration, jump height, time to apex, and maximum fall speed. For upward-positive coordinates, derive gravity magnitude `g = 2*h/(t*t)` and initial jump speed `v = 2*h/t`; confirm discrete-step results. Keep collider geometry separate from sprite bounds and camera pixels.

Use the fixed simulation clock specified by the architecture. For a small tile-based slice prefer a kinematic controller unless existing physics needs justify a rigid-body engine. Resolve broad-phase candidates then swept collision or bounded substeps; specify contact epsilon, slope policy, one-way-platform direction/drop-through behavior, and maximum displacement. Resolve grounded state from supported contacts, not a falling-speed guess. Carry riders by platform displacement before resolving their own motion.

Represent jump input as an edge with a timestamp. Configure coyote time, jump buffering and variable-height jump release in seconds. Ground contact replenishes a jump only once; buffered input must not replay on multiple ticks. Let a designer tune constants in data. Treat knockback and intentional movement as explicit combined velocity rules.

Camera deadzone/look-ahead should expose landing space and telegraphs. Snap only the rendered pixel-art view as appropriate, not the physics position. On respawn reset velocity, contacts, buffered input, invulnerability and camera state according to the checkpoint contract.

## Best practices and anti-patterns

Tune a greybox room before animation. Avoid variable-delta collision, sprite-sized colliders, using keyboard repeat as jump edges, broad overlap correction at high speed, and mixing screen Y-down with world Y-up. Screen shake must not move the handwriting surface or its input coordinates.

## Validation and Definition of Done

Test seam/corner contacts, ceiling bumps, high-speed falling, one-way platforms, platform riders, ledge jump timing, pause/resume and spawn overlap. Replay one input sequence at different render rates with the same tick outcomes. Done means measured jump height/range match the documented metrics within a stated tolerance, collision has no known tunneling in supported speeds, and checkpoint recovery is predictable. World gate reachability belongs to `metroidvania-world-design`.
