---
name: java-game-development
description: Implement or refactor INKSLAYER Java and libGDX architecture, lifecycle, game loops, Gradle dependencies, or desktop packaging. Use for engineering changes rather than gameplay design alone.
---
# Java game development

## Workflow and implementation

Inspect `build.gradle`, module builds, wrapper properties, and `docs/ARCHITECTURE.md` first. Preserve Java 21 and the existing libGDX/LWJGL3 split. Put native launcher concerns in `lwjgl3`; isolate rules in graphics-free Java under `core`. Introduce an interface at an actual boundary (clock, input, storage), not for every class. Prefer immutable records for commands/results and composition for actors.

Choose explicit update ownership: one simulation update per fixed tick; draw reads snapshots. Start with a configurable fixed step (for example 1/60 s), clamp long wall-time gaps, limit catch-up ticks, and document dropped-time behavior. Accumulate input edges until consumed once; maintain held input separately. Use seconds consistently. Keep UI animation active when simulation pauses. Rendering interpolation must not mutate collision state.

Define lifecycle owners for assets, screens, stages and subscriptions. Load resources at controlled transitions, use `AssetManager` where appropriate, and dispose exactly once. Keep GL operations on the libGDX thread. Avoid blocking file IO or recognition preprocessing in `render()`.

For builds use the checked-in wrapper, verify JDK 21, and inspect existing tasks before edits. Keep dependency changes minimal and pinned; check native classifiers and license implications. For packaging test the actual JAR outside the repository working directory, with Japanese fonts and assets. Do not replace Gradle with Maven in this project.

## Best practices and anti-patterns

Review in order: lifecycle/data loss, deterministic rules, timing/input, allocation hot spots, then structure. Refactor in behavior-preserving slices with replay evidence. Avoid a global mutable game singleton, an ever-growing `Main`, deep entity inheritance, frame-based velocity, unbounded catch-up, and allocating resources per frame. Profile with JFR/JDK tools and libGDX `GLProfiler` only after a repeatable workload identifies a bottleneck; compare p95/p99 frame times and allocation rates.

## Validation and Definition of Done

Run relevant domain tests and compile/package tasks with the required JDK; document blocked checks. Validate pause/resume, resize, focus loss and disposal. Review command consumption under 30/60/144 Hz rendering. A change is done when ownership and timing are explicit, the relevant checks pass, and observed architecture changes are documented. Read the testing skill for test harness design, not a second architecture prescription.

References: [libGDX wiki](https://libgdx.com/wiki/), [Gradle wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html).
