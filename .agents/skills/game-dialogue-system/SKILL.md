---
name: game-dialogue-system
description: Implement INKSLAYER branching dialogue graphs, narrative flags, localization IDs, text reveal, choices and environmental story delivery. Use for conversation runtime and authored graph validation.
---
# Game dialogue system

## Workflow and implementation

Read `docs/LORE.md`; label proposed story beats before writing them as canon. Use a data graph with stable node/message IDs, speaker IDs, choices, conditions and typed effects. Keep narrative traversal a graphics-free state machine. Avoid adding Ink/Yarn or evaluating scripts unless their Java integration and licensing are actually justified.

Validate references, required localized strings, speaker IDs and reachable exits at load. Conditions read explicit story/quest state; effects are typed commands from a small allowlist, not arbitrary Java/reflection or expression execution. Re-entering a line, skipping text or restoring a save cannot duplicate rewards; identify committed effects with stable IDs and define transaction boundaries with saves.

Separate advance-to-finish-text from advance-to-next-node. First activation completes the visible line, the next chooses progression; do not let a held mouse button skip choices. Define pause/draw-mode conflicts and which overlay owns input. Choice focus must be visible and return safely after settings close. A backlog and quick retry path can keep study text available without replaying whole scenes.

Reveal Unicode text by grapheme-aware units; preserve kana/kanji readings and markup. Strings belong in localization data with context notes and variables, not concatenated sentences. Environmental story objects can share message IDs and flags while keeping essential progression cues accessible without a hidden interaction.

## Best practices and anti-patterns

Keep humor and linguistic review in the learning/lore workflows. Avoid reward side effects in rendering, dialogue-node indices as save IDs, infinite automatic node cycles, string-based arbitrary conditions and text clipped by English-only sizing.

## Validation and Definition of Done

Walk every branch with representative flag states; detect dangling nodes and nonterminating automatic cycles while allowing intentional player-choice loops. Test double advance, skip, missing localization, Japanese text reveal, save/restore mid-conversation and once-only rewards. Done means graph validation passes, choices have deterministic effects, input is owned by the dialogue overlay, and accepted canon/content review is recorded.
