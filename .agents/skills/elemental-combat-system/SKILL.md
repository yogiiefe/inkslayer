---
name: elemental-combat-system
description: Implement or balance INKSLAYER elemental weaknesses, damage resolution, status effects, hit rules, and combat feedback. Use for combat domain rules after a spell command exists.
---
# Elemental combat system

## Workflow and implementation

Start with an explicit attack-element versus defense/weakness matrix approved in the GDD. No particular element set, cycle or multiplier is established yet. Validate all IDs and matrix entries at data load; every monster definition must expose at least one intended exploitable weakness. Separate base stats, elemental response, armor, invulnerability and status interactions.

Implement one pure damage resolver with a documented order, for example base damage → attack modifiers → elemental multiplier → defense → clamp/round. Specify integer rounding, minimum damage, immunity, healing and stacking instead of relying on accidental negative arithmetic. Return a structured hit outcome for UI/audio; it must not directly trigger rendering.

Give attacks stable IDs and track target-hit policy (once per swing/projectile or explicitly periodic). Resolve simultaneous death, invulnerability windows, projectile piercing, cancellation and status expiry on simulation time. Use stable ordering for simultaneous events and seeded randomness if needed. Hit-stop affects presentation/time policy; it cannot repeat damage or freeze a menu indefinitely.

Build a balancing sheet or small deterministic simulation measuring hits-to-kill and time-to-kill including realistic drawing, recognition and recovery time. Compare correct-element reward, wrong-element survivability, enemy telegraph windows and novice error/retry costs. Provide weaknesses through icons/text/animation as well as color; sound and VFX reinforce an outcome rather than changing it.

## Best practices and anti-patterns

Keep data-driven counters inspectable and avoid an elemental `if` cascade in every enemy. Do not map Japanese meanings to fictional elements as linguistic facts. Avoid overlapping hitboxes applying damage every render frame, animation callbacks as sole damage authority, and tuning solely from experienced players' casting speeds.

## Validation and Definition of Done

Table-test every matrix cell, missing IDs, rounding boundaries, stacked modifiers, immunity, repeated hit IDs, simultaneous kills and pause/status expiry. Test a wrong spell and a counter spell through the UI. Done means all monster definitions have a validated weakness, damage outcomes are deterministic, costs/hit timing are explicit, and the counter is readable without relying on color.
