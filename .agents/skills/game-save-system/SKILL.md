---
name: game-save-system
description: Implement or repair INKSLAYER local save slots, checkpoints, learning progress, version migrations, atomic writes and corruption recovery. Use when persistence or restore behavior changes.
---
# Game save system

## Workflow and implementation

Specify persistent state versus transient state: stable room/checkpoint IDs, abilities/spells, story effects, settings and learning progress may persist; textures, scene actors, input buffers, pending recognition results and live object references do not. Define the restore contract for health, enemies and boss phases. Save only after a coherent simulation boundary, not halfway through a cast debit.

Use an explicit versioned DTO/schema and a strict size-limited data parser. Avoid Java object deserialization of save files. Validate numeric bounds, enum/stable IDs and graph references before applying; unknown future versions must not be overwritten by an older client. Keep migrations as pure version-to-version transforms with fixtures.

Take an immutable snapshot, serialize to a temporary sibling on the same filesystem, flush/close, validate, then replace the target using platform-supported atomic rename where available. Retain a known-good backup; document the fallback when atomic replacement is unavailable. Serialize writes through one owner so autosave/manual save cannot interleave. A checksum detects accidental corruption, not tampering prevention.

On load parse and validate before changing live state. Prefer an intact primary, then validated backup; explain recovery to the user and retain the damaged file. A new-game fallback must not silently destroy progress. Keep learning prompt level/review timestamps separate from mastery claims. Store under the user's writable application data location through the platform adapter, not inside packaged assets.

## Best practices and anti-patterns

Avoid saving during rendering callbacks, saving localized names as IDs, trusting file content, deleting both copies before writing and unbounded retry loops on disk errors. Keep credentials and sensitive telemetry out of saves.

## Validation and Definition of Done

Test round trips, every supported migration, truncated files, invalid IDs/numbers, future versions, simulated write failures at each stage, concurrent save requests and backup recovery. Run cross-version fixtures and packaged-app path checks. Done means failure leaves a usable prior save, restore is coherent, migrations are reproducible, and limitations of filesystem guarantees are documented.
