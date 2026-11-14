---
name: inkslayer-audio-design
description: Design and integrate INKSLAYER libGDX music, spell and enemy sounds, pronunciation playback, volume categories and accessible audio cues. Use for game audio behavior and mixing.
---
# INKSLAYER audio design

## Workflow and implementation

Inventory cues and their gameplay meanings, then route them through application-owned categories (master, music, SFX, UI and voice). libGDX `Sound` handles short effects and `Music` streams longer content; inspect installed API capabilities before assuming mixer buses or sample-accurate scheduling. An application category gain layer is sufficient before adopting middleware.

Keep user gain and temporary ducking separate: effective gain is the product of master, category, clip and duck factors, clamped to the supported range. Zero must mute. Track playing sound IDs where live category updates require per-instance volume. Persist user settings, not transient duck envelopes. Stop/dispose according to asset ownership; cap concurrent repetitive effects.

Differentiate cast accepted, recognition retry, counter hit and danger without drowning out learning feedback. Pronunciation audio, if introduced, must be reviewed for the actual taught reading; do not randomly pitch-shift language samples. Subtitle essential voice information and pair critical audio-only threats with visual cues. Use restrained variant pools for repeated effects and record provenance/licenses for every clip.

## Best practices and anti-patterns

Mix representative worst-case combat at intended user volumes and leave headroom. Evaluate ducking/recovery with headphones and speakers. Avoid resetting user music volume after dialogue, spawning unbounded sounds, claiming libGDX has built-in middleware buses, critical audio-only teaching, and making an external music service a runtime requirement.

## Validation and Definition of Done

Test mute/category sliders during active sounds, pause/resume, repeated cast bursts, lost focus, screen transitions and disposal. Meter captured output for clipping when capture tools are available; listening is still required. Done means settings behave consistently, essential information has a nonaudio path, licenses are tracked and any untested mixing/pronunciation claims are disclosed.
