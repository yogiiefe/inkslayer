package com.yogiiefe.inkslayer.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import com.yogiiefe.inkslayer.audio.GameAudio.Channel;
import com.yogiiefe.inkslayer.screens.KeyBindings.Action;

import static com.yogiiefe.inkslayer.screens.PixelArtViewport.*;
import static com.yogiiefe.inkslayer.screens.TitleMenu.drawRegion;

/** Renders the supplied paper panels and draws changing controls on their pixel grid. */
final class MenuView implements Disposable {
    private enum State {
        IDLE(0), HOVER(0), PRESSED(1);
        final int shift;
        State(int shift) { this.shift = shift; }
    }

    private static final Color PAPER = Color.valueOf("f5f5f5"), GRAY = Color.valueOf("b4b4b4");
    private static final Color OUTLINE = Color.valueOf("464646"), EDGE = Color.valueOf("5c5c5c");
    private static final Color ALERT = Color.valueOf("ac252c"), BRUSH = Color.valueOf("ed1c24");
    private static final Color BRUSH_HOVER = Color.valueOf("bc1b27"), DIM = new Color(0, 0, 0, .72f);
    private static final Color SHADOW = Color.valueOf("404040");

    private final Texture chapter = TitleMenu.load("newGameLayout.png");
    private final Texture chapterArt = chapterArt();
    private final Texture settings = TitleMenu.load("settingsLayout.png");
    private final Texture keybinds = TitleMenu.load("keybindLayout.png");
    private final Texture keyBrush = keyBrush();
    private final Texture keyHover = keyHover();
    private final Texture saves = TitleMenu.load("savesLayout.png");
    private final Texture pause = TitleMenu.load("pauseLayout.png");
    private final SpriteBatch batch;
    private final Texture pixel;
    private final MenuLettering lettering = new MenuLettering();
    private final GameScreen game;

    MenuView(GameScreen game, SpriteBatch batch, Texture pixel) {
        this.game = game;
        this.batch = batch;
        this.pixel = pixel;
    }

    void draw() {
        switch (game.mode) {
            case CHAPTER_SELECT -> chapters();
            case SLOTS -> saves();
            case SETTINGS -> settings();
            case KEYBINDS -> keybinds();
            case PAUSE -> pause();
            default -> throw new IllegalStateException("No menu for " + game.mode);
        }
    }

    private void chapters() {
        sheet(chapter);
        rect(84, 52, 149, 92, PAPER);
        for (int i = 0; i < 4; i++) {
            var card = MenuButton.CHAPTERS[i];
            var state = button(card);
            int offset = state.shift;
            int x = card.x + 2, top = card.top + 1;
            batch.setColor(ink(state));
            drawRegion(batch, chapterArt, x, top, card.width - 4, card.height - 4, x + offset, top + offset);
            // The medal illustrations keep their source colors in all three states.
            batch.setColor(Color.WHITE);
            drawRegion(batch, chapterArt, card.x + 2, card.top + 20, 18, 13,
                card.x + 2 + offset, card.top + 20 + offset);
        }
        back();
    }

    private static Texture chapterArt() {
        try (var source = new MenuPixmap("newGameLayout.png"); var artwork = new MenuPixmap()) {
            for (int i = 0; i < 4; i++) {
                var card = MenuButton.CHAPTERS[i];
                // The reference shows Weather already pressed. Store its ink at the idle origin.
                int sourceShift = card == MenuButton.WEATHER ? 1 : 0;
                int background = source.pixmap.getPixel(card.x + 4 + sourceShift, card.top + 12 + sourceShift) >>> 24;
                for (int y = card.top + 1; y < card.top + card.height - 3; y++) {
                    for (int x = card.x + 2; x < card.x + card.width - 2; x++) {
                        int rgba = source.pixmap.getPixel(x + sourceShift, y + sourceShift);
                        int difference = Math.abs((rgba >>> 24) - background);
                        if (difference < 12 && rgba >>> 24 == (rgba >>> 16 & 255)
                            && rgba >>> 24 == (rgba >>> 8 & 255)) continue;
                        boolean medal = x < card.x + 20 && y >= card.top + 20 && y < card.top + 33;
                        if (medal) artwork.pixmap.drawPixel(x, y, rgba);
                        else artwork.pixmap.drawPixel(x, y, Color.rgba8888(1, 1, 1, Math.min(1f, difference / 65f)));
                    }
                }
            }
            return new Texture(artwork.pixmap);
        }
    }

    private static Texture keyBrush() {
        // Retain the brush silhouette, including the uneven edges, under the dynamic key names.
        try (var source = new MenuPixmap("keybindLayout.png"); var artwork = new MenuPixmap()) {
            for (int y = 47; y < 59; y++) {
                int first = 234, last = 182;
                for (int x = 182; x < 234; x++) {
                    int rgba = source.pixmap.getPixel(x, y);
                    if ((rgba >>> 24) - (rgba >>> 16 & 255) > 90) {
                        artwork.pixmap.drawPixel(x, y, 0xffffffff);
                        first = Math.min(first, x);
                        last = x;
                    }
                }
                for (int x = Math.max(first, 197); x <= Math.min(last, 220); x++) {
                    artwork.pixmap.drawPixel(x, y, 0xffffffff);
                }
            }
            return new Texture(artwork.pixmap);
        }
    }

    private void settings() {
        sheet(settings);
        rect(113, 98, 96, 44, PAPER);
        for (int i = 0; i < 3; i++) slider(MenuButton.SETTINGS[i], game.audio.volume(Channel.values()[i]));
        label(MenuButton.DISPLAY, "DISPLAY: " + game.displayMode.label);
        label(MenuButton.STROKE, game.strict ? "STROKE: STRICT" : "STROKE: COACHING");
        label(MenuButton.KEYBINDS, "KEYBINDS");
        message(game.settingsMessage, 151, 155);
        back();
    }

    private void slider(MenuButton control, int value) {
        int start = MenuButton.TRACK_START, end = MenuButton.TRACK_END;
        var state = state(control);
        int y = control.top + 5;
        int knob = start + Math.round(value * (end - start) / 100f);
        rect(control.x, control.top, control.width, control.height, PAPER);
        // The reference rail has single-pixel end caps and a hollow centre.
        rect(start, y - 1, end - start + 1, 1, OUTLINE);
        rect(start, y + 1, end - start + 1, 1, OUTLINE);
        rect(start - 1, y, 1, 1, OUTLINE);
        rect(end + 1, y, 1, 1, OUTLINE);
        rect(start, y, knob - start + 1, 1, EDGE);
        rect(knob - 2, y - 1, 5, 3, OUTLINE);
        rect(knob - 1, y - 2, 3, 5, OUTLINE);
        rect(knob - 1, y - 1, 3, 3, switch (state) {
            case IDLE -> EDGE;
            case HOVER -> GRAY;
            case PRESSED -> Color.WHITE;
        });
    }

    private static Texture keyHover() {
        try (var source = new MenuPixmap("keybindLayout.png")) {
            source.pixmap.setBlending(Pixmap.Blending.None);
            for (int y = 47; y < 134; y++) {
                for (int x = 87; x < 143; x++) {
                    int pixel = source.pixmap.getPixel(x, y);
                    if (pixel == 0x464646ff) source.pixmap.drawPixel(x, y, 0x787878ff);
                }
            }
            return new Texture(source.pixmap);
        }
    }

    private void keybinds() {
        sheet(keybinds);
        rect(86, 27, 52, 18, PAPER);
        rect(86, 46, 150, 89, PAPER);
        rect(131, 143, 62, 15, PAPER);
        batch.setColor(Color.WHITE);
        drawRegion(batch, keybinds, 86, 27, 52, 18, 86, 19);
        for (var action : Action.values()) {
            var control = MenuButton.BINDINGS[action.ordinal()];
            var state = state(control);
            int dx = state.shift, top = control.top + dx;
            boolean capturing = game.bindingKey && game.selectedBinding == action;
            Texture sheet = state == State.IDLE && !capturing ? keybinds : keyHover;
            batch.setColor(Color.WHITE);
            int sourceTop = 47 + 15 * action.ordinal();
            drawRegion(batch, sheet, control.x, sourceTop, 56, control.height, control.x + dx, top);
            String value = capturing ? "PRESS A KEY" : game.bindings.keycap(action);
            batch.setColor(capturing || state == State.PRESSED ? ALERT : state == State.HOVER ? BRUSH_HOVER : BRUSH);
            // Extend the painted brush equally on both sides of the shared column centre.
            int grow = Math.max(0, (lettering.measure(value) + 6 - 49 + 1) / 2);
            int brushX = 191 + dx - 26 - grow;
            drawRegion(batch, keyBrush, 182, 47, 26, 12, brushX, top);
            if (grow > 0) batch.draw(keyBrush, (brushX + 26) * ART_PIXEL, (ART_HEIGHT - top - 12) * ART_PIXEL,
                grow * 2 * ART_PIXEL, 12 * ART_PIXEL, 208, 47, 1, 12, false, false);
            drawRegion(batch, keyBrush, 208, 47, 26, 12, brushX + 26 + grow * 2, top);
            int textX = brushX + 2, textTop = top + 4, textWidth = 49 + grow * 2;
            for (int y = -1; y <= 1; y++) {
                for (int x = -1; x <= 1; x++) {
                    if (x != 0 || y != 0) caption(value, textX + x, textTop + y, textWidth, 5, OUTLINE, true);
                }
            }
            caption(value, textX, textTop, textWidth, 5, PAPER, true);
        }
        label(MenuButton.DEFAULT, "DEFAULT");
        if (!game.bindingMessage.isEmpty()) lettering.wrapped(batch, game.bindingMessage, 81, 124, 158, ALERT);
        back();
    }

    private void saves() {
        sheet(saves);
        rect(83, 54, 152, 82, PAPER);
        for (int i = 0; i < game.slots.size(); i++) {
            var slot = game.slots.get(i);
            String title = "EMPTY";
            if (slot.loadable()) {
                var progress = slot.progress();
                title = progress.completed() ? "COMPLETED"
                    : "CHAPTER " + (progress.stage() + 1) + ": " + game.content.stages.get(progress.stage()).name();
            } else if (slot.occupied()) title = "UNREADABLE SAVE";
            boolean empty = !slot.occupied();
            label(MenuButton.SLOT_ROWS[i], title, empty, empty ? MenuButton.EMPTY_SLOT_EXTRA_WIDTH : 0);
            if (slot.occupied()) {
                var control = MenuButton.TRASH[i];
                var state = state(control);
                int sourceX = state == State.IDLE ? 213 : 217;
                int sourceY = switch (state) { case IDLE -> 99; case HOVER -> 77; case PRESSED -> 121; };
                batch.setColor(Color.WHITE);
                drawRegion(batch, saves, sourceX, sourceY, control.width - state.shift, control.height - state.shift,
                    control.x + state.shift, control.top + state.shift);
            }
        }
        message(game.saveMessage, 137, 155);
        back();
        if (game.confirmDelete) {
            rect(0, 0, ART_WIDTH, ART_HEIGHT, DIM);
            rect(89, 63, 142, 55, OUTLINE);
            rect(90, 64, 140, 53, PAPER);
            caption("DELETE SLOT " + (game.slotSelection + 1) + "?", 96, 71, 128, 9, OUTLINE, true);
            lettering.wrapped(batch, "This deletes the checkpoint and its backup.", 93, 83, 134, OUTLINE);
            label(MenuButton.CANCEL_DELETE, "CANCEL");
            label(MenuButton.CONFIRM_DELETE, "DELETE");
            back();
        }
    }

    private void pause() {
        batch.setColor(Color.WHITE);
        drawRegion(batch, pause, 91, 41, 140, 96, 91, 41);
        rect(114, 67, 97, 57, PAPER);
        label(MenuButton.RESUME, "RESUME");
        label(MenuButton.GLOSSARIUM, "GLOSSARIUM");
        label(MenuButton.PAUSE_SETTINGS, "SETTINGS");
        label(MenuButton.SAVE_MENU, "SAVE/MENU");
        caption(game.saveMessage, 101, 124, 120, 8, ALERT, true);
    }

    private void sheet(Texture texture) {
        batch.setColor(Color.WHITE);
        // The supplied alternate sprites and lettering tests lie outside the paper panel.
        drawRegion(batch, texture, 68, 12, 184, 156, 68, 12);
    }

    private void back() {
        var button = MenuButton.BACK;
        var state = state(button);
        int sourceX = switch (state) { case IDLE -> button.x; case HOVER -> 256; case PRESSED -> 280; };
        rect(button.x, button.top, button.width, button.height, PAPER);
        batch.setColor(Color.WHITE);
        drawRegion(batch, settings, sourceX, button.top + state.shift, button.width - state.shift, button.height - state.shift,
            button.x + state.shift, button.top + state.shift);
    }

    private State state(MenuButton control) {
        if (game.keyboardPress == control) return State.PRESSED;
        if (game.menuPress == control && (game.menuHover == control || control.sliderIndex() >= 0)) return State.PRESSED;
        return game.menuHover == control || game.showMenuFocus && game.menuFocus == control
            ? State.HOVER : State.IDLE;
    }

    private State button(MenuButton control) {
        return button(control, 0);
    }

    private State button(MenuButton control, int extraWidth) {
        var state = state(control);
        int x = control.x + state.shift, y = control.top + state.shift;
        int w = control.width + extraWidth, h = control.height;
        // The face covers its one-pixel drop shadow when held, as on the reference sheets.
        if (state != State.PRESSED) rect(x + 1, y + 1, w - 1, h - 1, SHADOW);
        rect(x, y, w - 1, h - 1, frameInk());
        rect(x + 1, y + 1, w - 3, h - 3, state == State.IDLE ? PAPER : GRAY);
        return state;
    }

    private void label(MenuButton control, String label) { label(control, label, true, 0); }

    private void label(MenuButton control, String label, boolean centered, int extraWidth) {
        var state = button(control, extraWidth);
        caption(label, control.x + 3 + state.shift, control.top + state.shift,
            control.width + extraWidth - 6, control.height - 1, ink(state), centered);
    }

    private Color frameInk() {
        return game.mode == GameScreen.Mode.SETTINGS || game.mode == GameScreen.Mode.PAUSE ? EDGE : OUTLINE;
    }

    private Color ink(State state) {
        if (state == State.HOVER) return game.mode == GameScreen.Mode.SETTINGS ? Color.WHITE : PAPER;
        return game.mode == GameScreen.Mode.CHAPTER_SELECT ? SHADOW : frameInk();
    }

    private void message(String value, int top, int bottom) {
        if (value.isEmpty()) return;
        rect(82, top, 156, bottom - top, PAPER);
        caption(value, 83, top, 154, bottom - top, ALERT, true);
    }

    private void caption(String value, int x, int top, int width, int height, Color color, boolean centered) {
        lettering.draw(batch, value, x, top, width, height, color, centered);
    }

    private void rect(int x, int top, int width, int height, Color color) {
        batch.setColor(color);
        batch.draw(pixel, x * ART_PIXEL, (ART_HEIGHT - top - height) * ART_PIXEL,
            width * ART_PIXEL, height * ART_PIXEL);
    }

    @Override public void dispose() {
        chapter.dispose();
        chapterArt.dispose();
        settings.dispose();
        keybinds.dispose();
        keyBrush.dispose();
        keyHover.dispose();
        lettering.dispose();
        saves.dispose();
        pause.dispose();
    }

    private static final class MenuPixmap implements AutoCloseable {
        final Pixmap pixmap;
        MenuPixmap(String path) { pixmap = new Pixmap(Gdx.files.internal(path)); }
        MenuPixmap() {
            pixmap = new Pixmap(ART_WIDTH, ART_HEIGHT, Pixmap.Format.RGBA8888);
            pixmap.setBlending(Pixmap.Blending.None);
        }
        @Override public void close() { pixmap.dispose(); }
    }
}
