# Skill validation evidence

Date: 2026-11-14. This report concerns skill/documentation setup, not game correctness.

## Checks

Executed the project-local validator as inline PowerShell because this host disables `.ps1` file invocation; the execution policy was not changed.

| Check | Observed result |
| --- | --- |
| Required skills, folder/name agreement, scalar metadata, body sections and local links | PASS — all 17 skill folders |
| Manifest inventory and SHA256 verification | PASS — all 20 skill files, including 2 recognition references and 1 validator script |
| Original project preservation | PASS — all 25 pre-existing project files retain identical SHA256 hashes; source, assets and build/wrapper configuration unchanged |
| Validator failure behavior in an isolated temporary copy | PASS — invalid name, broken relative link and changed locked file were rejected; 4 diagnostics, including the missing requested valid skill name |
| Restored isolated fixture | PASS — metadata/links/inventory/hashes pass again |
| Ownership/overlap review | PASS — recognition, pedagogy, acceptance/casting and damage have distinct owners; shared geometry/source rules are linked |

The scalar-frontmatter check validates the deliberately restricted form authored here, not arbitrary YAML. The manifest is a drift detector rather than a cryptographic trust signature. The temporary negative-test workspace is outside the project and no existing file was removed or overwritten there.

## Independent forward test

A read-only independent agent used the recognition, stroke-validation and spellcasting skills to reason through swapped strokes, reversed curved strokes, viewport resize and results arriving after clear/death. It confirmed the shape/pedagogy/casting separation and identified two concrete omissions. Both were corrected: canonical dot strokes can have direction `notApplicable` (distinct from unreliable evidence), and lifecycle generation invalidates results across death/respawn/room changes/close.

Expected behavioral contract: an identifiable swapped drawing may cast once with combat coaching, but fails strict order practice; reversed curved strokes use full-path direction evidence; mixed viewport transforms are prohibited; stale results never debit/cast, including after respawn. This is an instruction-level evaluation, not an executed recognizer/game test or proof of recognition accuracy.

## Blocked checks

- Official Python quick validator: Python alias cannot execute; PyYAML availability unverified. A restricted local scalar-frontmatter validator is supplied and tested instead.
- Native discovery on installed Codex CLI 0.169.0: `codex debug prompt-input 'List available skill metadata only.'` exits with `Could not find home directory` before discovery. Runtime discovery remains unverified.
- Game build/runtime: intentionally not run for this setup; shell Java resolves to 8, project targets 21, and resource processing writes an asset list. No gameplay behavior, graphics, audio, Japanese-content accuracy or performance is claimed tested.

## Maintenance

`docs/skills-lock.json` inventories the skill files and hashes, not game implementation. Regenerate only after reviewing intended changes. All custom skills remain implicitly discoverable by default; use `/skills` or `$` at the project root on the next turn, restarting Codex if needed once the host home-directory issue is resolved.
