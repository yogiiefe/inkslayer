---
name: java-game-testing
description: Test, debug, review and verify INKSLAYER Java gameplay, recognition regressions, integration boundaries, performance evidence and desktop release artifacts. Use for quality work and bug reproduction.
---
# Java game testing

## Workflow and implementation

Inspect the actual Gradle test dependencies and tasks; currently no dedicated game test suite is established. When authorized to implement tests, add the smallest compatible JUnit setup using the wrapper/JDK 21 and test platform configuration. Do not introduce dependencies during documentation-only setup. Keep plain-Java tests independent of `Gdx.*`, native libraries and graphics contexts.

For a defect record initial state, input/tick sequence, expected outcome, actual outcome and versions. Inject clocks and seeded RNG into domain boundaries; preserve minimal replay fixtures. Prefer rule invariants (one cast debit, bounded damage, save survives failure) over assertions mirroring private method structure. Use fakes at input/storage/renderer boundaries and a few integration tests for the whole spell pipeline.

Recognition regression needs a versioned corpus and held-out writers; use the recognition skill's evaluation contract rather than duplicating thresholds here. Collision and AI use fixed-tick replays; save tests inject interrupted writes; dialogue tests traverse graphs. Randomized/property-style checks are useful when they test real invariants and print reproducible seeds.

Use a libGDX headless backend only for engine services it supports; it does not prove GL rendering, font appearance or audible mixing. Desktop smoke checks cover resize/HiDPI, focus loss, pause, input ownership, Japanese text, sound settings and resource cleanup. A packaged JAR must run from a different working directory with the runtime assets and native libraries it needs.

Profile a repeatable scene on declared hardware/build/JVM after warm-up. Record p50/p95/p99 timings, allocations/GC and workload sizes. JFR supports CPU/allocation diagnosis; libGDX GLProfiler helps draw-call/state-change analysis. Headless timings do not establish GPU performance. Before/after results require the same workload and machine.

## Review and anti-patterns

Rank findings by data loss, crashes, wrong casts/learning feedback, gameplay defects and performance. Give file/location, reproduction, consequence and concrete remedy; distinguish confirmed findings from hypotheses. Avoid coverage quotas as proof, sleeps in timing tests, mock-only end-to-end claims, tuning against the test corpus and claiming a build passed when the toolchain failed.

## Validation and Definition of Done

Run the relevant focused tests, then necessary module/package checks once. Record exact commands, pass/fail/blocked status and material limitations. Regression fixtures must fail on the original bug and pass after the fix when feasible. Done means acceptance criteria have observable evidence, release smoke checks match the intended platforms, and untested graphics/audio/human learning claims remain explicitly unverified.

For skill-maintenance work only, run [scripts/validate-skills.ps1](scripts/validate-skills.ps1) from PowerShell. It checks the deliberately restricted scalar frontmatter used here, directory/name agreement, local links, required custom-skill sections, and inventory hashes if present. It is not a general YAML parser or a substitute for Codex discovery. Use the official skill-creator validator when a functioning Python/PyYAML environment is available.
