# INKSLAYER — skill inventory and setup report

Setup date: 2026-11-14. Project root: `C:\Users\Administrator\Documents\inkslayer`.

## Result and installation paths

17 original project-local skills created: all 15 requested capabilities plus libGDX-specific UI/accessibility and audio coverage. Every entry lives at `.agents/skills/<name>/SKILL.md`; no global skill, plugin, framework or game dependency was installed. No third-party skill package was installed. Public research is not an installation result.

All skills use required `name` and `description` YAML frontmatter, specific activation conditions, implementation workflows, anti-patterns, validation and Definition of Done. Optional `agents/openai.yaml` files are omitted; automatic matching remains at the default. Most skills are instruction-only. The recognition skill has two focused references; testing has one local read-only validation script.

| Skill | Purpose and ownership |
| --- | --- |
| [java-game-development](../.agents/skills/java-game-development/SKILL.md) | Java 21/libGDX boundaries, lifecycle, fixed loop, OOP/composition, refactoring, Gradle and desktop packaging |
| [2d-platformer-development](../.agents/skills/2d-platformer-development/SKILL.md) | Movement metrics, collision, jump responsiveness, platforms and camera |
| [kanji-handwriting-recognition](../.agents/skills/kanji-handwriting-recognition/SKILL.md) | Highest priority: mouse capture, normalization/resampling, multistroke shape matching, confidence/rejection, database and benchmarks |
| [kanji-stroke-order-validation](../.agents/skills/kanji-stroke-order-validation/SKILL.md) | Independent count, curved-stroke direction, order, variants and beginner coaching |
| [inkslayer-spellcasting](../.agents/skills/inkslayer-spellcasting/SKILL.md) | Draw-mode input and clocks, acceptance policy, stale-result protection and exactly-once casting |
| [elemental-combat-system](../.agents/skills/elemental-combat-system/SKILL.md) | Weakness matrix, pure damage resolution, statuses, hit identity, balancing and readable feedback |
| [enemy-ai-development](../.agents/skills/enemy-ai-development/SKILL.md) | Perception, finite state machines, bounded navigation and telegraphed enemy behavior |
| [boss-battle-design](../.agents/skills/boss-battle-design/SKILL.md) | Boss phases, fairness, novice casting windows and retry cleanup |
| [metroidvania-world-design](../.agents/skills/metroidvania-world-design/SKILL.md) | Level graph, ability/learning gates, environmental clues, shortcuts and softlock analysis |
| [japanese-learning-design](../.agents/skills/japanese-learning-design/SKILL.md) | Verified educational content, narrative feedback, localization, assisted versus independent recall and learning progression |
| [pixel-art-pipeline](../.agents/skills/pixel-art-pipeline/SKILL.md) | Sprite sheets, atlas metadata, animation, pixel scaling, VFX and asset licenses |
| [game-dialogue-system](../.agents/skills/game-dialogue-system/SKILL.md) | Narrative graph, typed conditions/effects, story flags, Unicode reveal and once-only rewards |
| [game-save-system](../.agents/skills/game-save-system/SKILL.md) | Checkpoints, DTO schemas, migrations, bounded loading, atomic writes and recovery |
| [java-game-testing](../.agents/skills/java-game-testing/SKILL.md) | Unit/integration/regression testing, debugging, review, JFR/GL profiling and release smoke evidence |
| [inkslayer-project-context](../.agents/skills/inkslayer-project-context/SKILL.md) | Onboarding, scope, risk priorities, document/canon reconciliation and milestone planning |
| [inkslayer-ui-accessibility](../.agents/skills/inkslayer-ui-accessibility/SKILL.md) | Scene2D HUD/menu layout, input focus, text, HiDPI and accessibility |
| [inkslayer-audio-design](../.agents/skills/inkslayer-audio-design/SKILL.md) | libGDX audio categories, mixing, spell cues, pronunciation review and accessible alternatives |

Scope management lives in project-context/ROADMAP; release engineering in Java/testing; narrative canon in LORE and learning/graph workflows. These are deliberate owners rather than duplicate specialized skills.

## Public repository research and installation decisions

| Source | Verified findings and decision |
| --- | --- |
| [openai/skills](https://github.com/openai/skills) | Repository reachable; README now marks it deprecated and points to the OpenAI Plugins repository. System installer/creator already available locally. Curated tree did not expose a usable listing through browser output; attempted `develop-web-game` raw path returned 404. Do not infer the entire curated catalog is absent. No suitable Java skill was verified, so nothing installed. Individual upstream skill licenses would require review. |
| [bindaksu/gamedev-skills](https://github.com/bindaksu/gamedev-skills) | Actual default branch is `master`, not `main`. README documents shell installation and project-local options; that untrusted script was not executed. Read `skills/gamedev-reviewer/SKILL.md`: MIT frontmatter, live-service emphasis, Unity/mobile default and routing to absent specialists. Linked rubric could not be retrieved; no complete package audit possible. Skipped for default/dependency mismatch and incomplete verification. |
| [abagames/agentic-gamedev-skills](https://github.com/abagames/agentic-gamedev-skills) | Repository reachable; README describes canonical `.agents/skills` and plugin bundles. Read `designing-minimal-game-rules/SKILL.md`: explicitly excludes real-time physics/precision action and targets untuned discrete-state games, so rejected for this project. README's Godot/web/one-button bundles do not establish Java compatibility. Guessed `testing-browser-games` path returned 404; not treated as an existing skill. MIT shown by repository metadata; no code copied. |
| [gamedev-skills/awesome-gamedev-agent-skills](https://github.com/gamedev-skills/awesome-gamedev-agent-skills) | Repository reachable. Apache-2.0 license and README reviewed. Read `audio-design`, `game-ui-ux`, `performance-optimization` entrypoints; also the linked adaptive-music and layout-and-flow references. Useful cross-engine concepts, but concrete APIs/examples and optional related skills are for other engines; performance trigger lists those engines, not libGDX. No bulk router/engine pack installed. Original libGDX workflows cover the relevant concepts. Full dependency/package trees were not downloaded or audited. |

The installer instructions and helper interfaces were inspected. Its Python helpers could not run because `python.exe` is an inaccessible Windows alias; shell HTTPS also failed (`Invoke-RestMethod` TLS receive error and curl Schannel `SEC_E_NO_CREDENTIALS`). The skill requires escalation for sandbox networking; this session disallows sandbox escalation, so no helper was executed with elevated permissions. Manual original skill creation is supported by [official local-skill documentation](https://learn.chatgpt.com/docs/build-skills). No third-party package is asserted safe or installed based only on a README. No credentials, install scripts, npm installers or private files were sent to external services.

## Security, compatibility and overlap review

- Original skill files contain no download/bootstrap commands, credential requirements, remote recognition calls or destructive helpers. The included PowerShell validator reads files, checks metadata/links/hashes and reports failures; it does not mutate project/game data or access the network.
- No third-party source, dataset, scripts or assets were vendored; no third-party license was silently assigned to custom text. Future source/data imports need their own revision-pinned provenance and licensing review.
- Recognition source references distinguish ordered geometry, shape tolerance, pedagogical validation and combat policy. Standard $N and point-cloud methods must not be mistaken for stroke-order validators. KanjiVG's documented CC BY-SA 3.0 terms need attention before derived templates are shipped. KANJIDIC entries/current terms were not verified; no linguistic table or unreviewed Kanji paths were fabricated.
- Project instructions remain in AGENTS; mutable design/canon belongs in docs; specialized algorithms belong in skills. Recognition owns preprocessing; stroke validation owns pedagogy; spellcasting owns consequences; combat owns damage. UI and audio consume outcomes. Cross-links intentionally share contracts instead of duplicating them.
- Compatibility is based on the observed libGDX/Java/Gradle stack. There is no Unity/Unreal/Godot/web framework migration, external recognition dependency or new game dependency.

## Validation and discovery

Run the local validator from the project root:

```powershell
& .\.agents\skills\java-game-testing\scripts\validate-skills.ps1
```

This host disables `.ps1` file invocation. The reviewed validator was therefore run as inline PowerShell without changing the execution policy:

```powershell
$validator = [scriptblock]::Create((Get-Content -LiteralPath '.agents\skills\java-game-testing\scripts\validate-skills.ps1' -Raw))
& $validator -ProjectRoot (Get-Location).Path
```

It validates this pack's deliberately limited single-line scalar YAML form, required skill names, folder agreement, nonempty descriptions, useful body sections, local links and (when present) SHA256 inventory in `docs/skills-lock.json`. It does not claim to parse arbitrary YAML. After intentional skill edits, review the changes and regenerate the inventory hashes; do not suppress hash failures to conceal drift. The official `skill-creator/scripts/quick_validate.py` was inspected but cannot run without working Python/PyYAML here.

Detected binary: `codex-cli 0.169.0`. A read-only native discovery probe, `codex debug prompt-input 'List available skill metadata only.'`, failed before loading a session with `Could not find home directory`. Therefore actual discovery by this binary is **not verified**. Folder format and placement match official conventions, but static validation is not proof of runtime discovery. This host's inaccessible home directory must be resolved outside this setup; no global configuration was changed to mask it.

Launch/open Codex with this project as the working directory. The folder is not currently a Git repository, so do not rely on scanning from nested module folders up to a Git root; start at the project root. On the next turn, check `/skills` or `$` selection for `kanji-handwriting-recognition`. Current official docs say changes are detected automatically; restart/reopen Codex at this root if entries do not appear. No restart was performed on the user's running application.

The final static check results, preservation evidence and independent behavioral review are recorded in [SKILL-VALIDATION.md](SKILL-VALIDATION.md).

## Recommended activation workflow

Use natural-language matching for small tasks, or explicitly invoke the owning skill. Keep the current question narrow and load supporting references only when needed.

1. Onboarding/scope: `$inkslayer-project-context` and the relevant milestone.
2. Recognition spike: `$kanji-handwriting-recognition`; add `$kanji-stroke-order-validation` when evaluating pedagogy and `$java-game-testing` when building the harness.
3. Draw-to-cast integration: `$inkslayer-spellcasting` plus `$elemental-combat-system`; leave shape mathematics in the recognizer contract.
4. Playable slice: movement/AI skills, then world design, UI/audio/art as the task requires.
5. Review/release: `$java-game-testing` and `$java-game-development`, with actual JDK/build/rendering evidence.

Example: `Use $kanji-handwriting-recognition to implement an offline mouse-stroke prototype with undo/clear and an unknown outcome; do not add ML or alter combat yet.`

## Remaining limitations

No third-party skills installed; repository audit is candidate-level, not a full security audit. Native discovery and the official Python validator remain blocked by host environment issues. PATH Java is 8 while the project targets 21; JDK 21/wrapper resolution and game runtime are not verified. No gameplay build was run because setup must preserve source/assets (a configured resource task rewrites `assets/assets.txt`). No dataset, Kanji readings/order table, recognizer implementation or accuracy claim is delivered by this documentation setup. Human Japanese-content review, novice testing, actual profiling and packaged-platform checks remain development tasks.
