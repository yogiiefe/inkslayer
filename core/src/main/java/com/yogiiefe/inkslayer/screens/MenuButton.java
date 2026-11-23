package com.yogiiefe.inkslayer.screens;

import static com.yogiiefe.inkslayer.screens.PixelArtViewport.ART_HEIGHT;
import static com.yogiiefe.inkslayer.screens.PixelArtViewport.ART_PIXEL;

/** Bounds in the supplied menu sheets, in 320x180 pixels from the top left. */
enum MenuButton {
    BACK(234, 18, 13, 11),
    ELEMENTS(85, 53, 69, 40), PETS(163, 53, 69, 40),
    WEATHER(85, 102, 69, 40), FOOD(163, 102, 69, 40),
    // Alternate the row and trash positions together, as painted on the Saves panel.
    SLOT_1(84, 55, 127, 15), SLOT_2(88, 77, 127, 15), SLOT_3(84, 99, 127, 15),
    TRASH_1(213, 55, 17, 15), TRASH_2(217, 77, 17, 15), TRASH_3(213, 99, 17, 15),
    CANCEL_DELETE(103, 100, 51, 13), CONFIRM_DELETE(166, 100, 51, 13),
    MASTER(143, 50, 92, 11), SFX(143, 63, 92, 11), BGM(143, 76, 92, 11),
    DISPLAY(100, 99, 122, 12), STROKE(100, 114, 122, 12), KEYBINDS(100, 128, 122, 12),
    LEFT(87, 41, 157, 12), RIGHT(87, 55, 157, 12), CONTINUE(87, 69, 157, 12),
    HELP(87, 83, 157, 12), CLEAR(87, 97, 157, 12), UNDO(87, 111, 157, 12),
    DEFAULT(132, 139, 60, 13),
    RESUME(115, 68, 94, 12), GLOSSARIUM(115, 83, 94, 12),
    PAUSE_SETTINGS(115, 97, 94, 12), SAVE_MENU(115, 111, 94, 12);

    static final MenuButton[] CHAPTERS = {ELEMENTS, PETS, WEATHER, FOOD, BACK};
    static final MenuButton[] SLOTS = {SLOT_1, TRASH_1, SLOT_2, TRASH_2, SLOT_3, TRASH_3, BACK};
    static final MenuButton[] SLOT_ROWS = {SLOT_1, SLOT_2, SLOT_3};
    static final MenuButton[] TRASH = {TRASH_1, TRASH_2, TRASH_3};
    static final MenuButton[] DELETE_DIALOG = {CANCEL_DELETE, CONFIRM_DELETE, BACK};
    static final MenuButton[] SETTINGS = {MASTER, SFX, BGM, DISPLAY, STROKE, KEYBINDS, BACK};
    static final MenuButton[] BINDINGS = {LEFT, RIGHT, CONTINUE, HELP, CLEAR, UNDO, DEFAULT, BACK};
    static final MenuButton[] PAUSE = {RESUME, GLOSSARIUM, PAUSE_SETTINGS, SAVE_MENU};
    static final MenuButton[] NONE = {};
    static final int TRACK_START = 146, TRACK_END = 231;
    static final int EMPTY_SLOT_EXTRA_WIDTH = 18;

    final int x, top, width, height;

    MenuButton(int x, int top, int width, int height) {
        this.x = x;
        this.top = top;
        this.width = width;
        this.height = height;
    }

    float centerX() { return (x + width / 2f) * ART_PIXEL; }
    float centerY() { return (ART_HEIGHT - top - height / 2f) * ART_PIXEL; }

    boolean contains(float uiX, float uiY, int extraWidth) {
        return GameScreen.hit(uiX, uiY, x * ART_PIXEL, (ART_HEIGHT - top - height) * ART_PIXEL,
            (width + extraWidth) * ART_PIXEL, height * ART_PIXEL);
    }

    int slotIndex() {
        return this.compareTo(SLOT_1) >= 0 && this.compareTo(SLOT_3) <= 0 ? ordinal() - SLOT_1.ordinal() : -1;
    }

    int sliderIndex() {
        return this.compareTo(MASTER) >= 0 && this.compareTo(BGM) <= 0 ? ordinal() - MASTER.ordinal() : -1;
    }
}
