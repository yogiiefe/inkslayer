---
name: inkslayer-ui-accessibility
description: Implement or evaluate INKSLAYER Scene2D HUDs, menus, handwriting-panel layout, Japanese text readability, focus and accessibility settings. Use for interface changes rather than dialogue graph logic.
---
# INKSLAYER UI and accessibility

## Workflow and implementation

Inspect current UI and viewport policy before adding a framework; libGDX Scene2D/VisUI are available. Use `Table`/containers and a separate UI viewport for responsive HUD layout. Recompute on resize, mapping screen input through that same viewport. The drawing panel transform stays fixed while capturing a stroke, with an explicit resize cancellation policy.

Model overlays with a stack and give the top modal input ownership. Initialize displayed state when subscribing to gameplay changes, then update through events. Unsubscribe on disposal. Distinguish hover, keyboard focus and pressed state; restore focus after closing a modal. If controller navigation is added, it must not imply replacing mouse-based Kanji input.

Provide scalable reviewed Japanese fonts, adjustable text size, readable panel contrast, icon/text element labels, remappable non-drawing controls where supported, reduced shake/flashing and volume controls. Keep learning assistance such as untimed practice discoverable. Preserve game-state independence: a paused world cannot freeze the settings UI or lose the drawing controls.

## Best practices and anti-patterns

Separate pixel-art integer scaling from text legibility. Avoid hardcoded screen coordinates, color-only weakness indicators, input events passing through modals, broad resource reloads on every HUD event and text sized only for English. Implement actual libGDX APIs rather than copying Godot/Unity snippets.

## Validation and Definition of Done

Inspect small window, 16:9, ultrawide, resize and HiDPI cases, enlarged text, long localized strings and every supported Japanese glyph. Test keyboard/mouse navigation, modal focus, pause and undo/clear/submit accessibility. Done means essential controls remain reachable, handwriting coordinates match display, threats remain readable and no unsupported input mode is promised.

Reference: [libGDX Scene2D UI](https://libgdx.com/wiki/graphics/2d/scene2d/scene2d-ui).
