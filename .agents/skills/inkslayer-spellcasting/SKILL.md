---
name: inkslayer-spellcasting
description: Integrate drawing UI, recognition results, beginner feedback, spell selection, and one-time cast commands in INKSLAYER. Use for draw-mode behavior and acceptance policy, not recognizer mathematics or damage formulas.
---
# INKSLAYER spellcasting

## Workflow and implementation

Specify the draw-mode clock policy (pause, slow-time or real-time), input ownership and cancel/cost behavior before wiring systems. Treat these as design choices to test, not already implemented facts. Model explicit states such as Idle, Drawing, Evaluating, Feedback and Casting; define valid transitions and interruption behavior for death, focus loss, room transition, pause and UI close.

Primary-button down/up separates strokes; submit freezes an immutable drawing snapshot. Undo/clear/cancel increments a revision and invalidates pending results. Death, respawn, room transition and draw-mode close also invalidate the request and advance a lifecycle generation token. Accept a result only for the current generation, request ID, drawing revision, mode and equipped/known spell set. A repeated submit or late worker result cannot spend resources or emit a second cast, even if the player has already respawned into the same mode with the same revision value.

Keep `shape recognition → stroke findings → acceptance policy → cast command` separate. Combat may tolerate a recognizable character with coaching, while strict practice validates canonical count/order/direction. Both reject unknown/ambiguous shapes. Use a single policy decision with a reason code and policy version. Never choose the element by reading a mutable UI label.

Use stable Kanji-to-spell mappings in game data. Confirm the spell is learned, the player can act and resources are available at command commit; debit exactly once with the accepted cast. Define whether a target is locked at submission or selected at commit. Domain combat resolves hits later. A recognition failure provides retry guidance without silently selecting the nearest spell.

Draw the handwriting panel in stable UI space; world shake, camera motion and hit-stop must not warp input. Provide undo, clear, cancel, explicit submit, a practice route, labeled element cues and an intelligible rejection reason. Feedback should distinguish a shape ambiguity from a teaching correction.

## Best practices and anti-patterns

Keep practice pressure configurable and preserve player work during feedback. Avoid timeout-only submission, stale callbacks casting after death, multiple listeners spending mana twice, confidence percentages without calibration, and effects obscuring the next stroke.

## Validation and Definition of Done

Test double submit, submit then undo/clear, delayed results after death/room change, failed affordability at commit, cancellation, zero strokes, and UI clicks leaking into combat. Play through recognize → cast → elemental hit with fake recognizer results before integrating geometry. Done means one accepted request produces at most one debit and cast, unknown results never cast, controls work, and clock/teaching policy is documented and beginner-tested.
