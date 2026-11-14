# INKSLAYER — proposed roadmap

Urutan build untuk permintaan terbaru tersedia di [BUILD_PLAN.md](BUILD_PLAN.md), bagian urutan pembangunan dan kriteria selesai. Tabel berikut merekam milestone versi awal proyek.

Current delivery: an experimental playable MVP now covers the essential 水-to-fire-enemy interaction. The user's latest "Make the game" request advances beyond the earlier planning-only discussion. The full campaign remains future scope.

| Current scope | Status |
| --- | --- |
| Environment/build/desktop launch | Verified using JDK 21 and documented managed-host compiler workaround |
| Foundation and input/render lifecycle | Implemented for this slice |
| Platformer/controller and small level | Implemented; route and collision tests |
| Canvas / undo / clear / submit / guide | Implemented; actual input/GL probe |
| Water recognition | Experimental implementation; synthetic tests only, novice accuracy unverified |
| First fire-enemy combat / death / restart / victory | Implemented and integration-tested |
| Four playable elements, mini-boss, Tiled, saves, dialogue graphs, broad learning progression | Planned |

The table below retains the original milestone goals; implemented slices do not imply all of each milestone's research/playtest exit evidence has been met.

| Milestone | Exit evidence | Scope boundary |
| --- | --- | --- |
| 0. Toolchain | JDK 21 and wrapper resolve; desktop sample launches; tests can run | No framework migration |
| 1. Recognition spike (highest priority) | Mouse capture with undo/clear; offline deterministic recognizer; sourced templates; disjoint-writer confusion/latency report | Small verified Kanji set; no ML/API |
| 2. Teaching prototype | Shape acceptance distinct from count/order/direction coaching; beginner playtest; ambiguity/unknown handling | Practice room before progression gates |
| 3. Playable slice | Movement/collision, one enemy, one spell, checkpoint, safe return path | Placeholder assets, one connected encounter |
| 4. Elemental choice | Second spell/enemy interaction, data-driven counters, clear weakness cues, balanced drawing windows | No large content expansion |
| 5. Adventure systems | Save migration/recovery, dialogue graph, ability-gate reachability checks | New content only after a complete loop |
| 6. Boss and learning review | Telegraph/recovery tests, delayed-recall pilot, accessibility checks | Boss uses taught material |
| 7. Release candidate | Profiling on target desktop, packaged asset/font/audio checks, credits/licenses, regression evidence | Publishing is a separately authorized action |

For each milestone record one observable question, a bounded implementation, evidence, and a cut list. Defer procedural worlds, large skill trees, external recognition services, and production art until the slice supports the learning premise. Do not interpret this proposal as a fixed schedule or an accepted vocabulary list.
