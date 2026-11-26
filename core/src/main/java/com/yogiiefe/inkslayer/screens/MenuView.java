package com.yogiiefe.inkslayer.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.Disposable;
import com.yogiiefe.inkslayer.audio.GameAudio.Channel;
import com.yogiiefe.inkslayer.screens.GameScreen.GlossaryTab;
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
    private static final String KEYBINDS_LAYOUT = "keybindsLayout.png";
    private static final int KEY_BRUSH_WIDTH = 50, KEY_BRUSH_HEIGHT = 9;
    private static final int KEY_BRUSH_LEFT_X = 166, KEY_BRUSH_RIGHT_X = 184;
    private static final int[] SHAKE_OFFSETS = {2, -2, 1, -1};
    private static final int GLOSSARY_CENTER_Y = (MenuButton.KANJI.top + MenuButton.KANJI.height
        + MenuButton.PRACTICE.top) / 2;
    private static final int GLOSSARY_CONTENT_WIDTH = 142, GLOSSARY_WRITING_HEIGHT = 40;
    private static final int GLOSSARY_LABEL_HEIGHT = 5;

    private final Texture chapter = TitleMenu.load("newGameLayout.png");
    private final Texture chapterArt = chapterArt();
    private final Texture settings = TitleMenu.load("settingsLayout.png");
    private final Texture keybinds = TitleMenu.load(KEYBINDS_LAYOUT);
    private final Texture keyBrush = keyBrush();
    private final Texture saves = TitleMenu.load("savesLayout.png");
    private final Texture pause = TitleMenu.load("pauseLayout.png");
    private final Texture glossary = TitleMenu.load("glossariumLayout.png");
    private final GlossaryWriting writing;
    private final SpriteBatch batch;
    private final Texture pixel;
    private final MenuLettering lettering = new MenuLettering();
    private final Rectangle keybindList = new Rectangle(MenuButton.LEFT.x * ART_PIXEL,
        (ART_HEIGHT - MenuButton.KEYBIND_LIST_END) * ART_PIXEL,
        MenuButton.LEFT.width * ART_PIXEL, MenuButton.KEYBIND_LIST_HEIGHT * ART_PIXEL);
    private final Rectangle keybindClip = new Rectangle();
    private final GameScreen game;

    MenuView(GameScreen game, SpriteBatch batch, Texture pixel, GlossaryWriting writing) {
        this.game = game;
        this.batch = batch;
        this.pixel = pixel;
        this.writing = writing;
    }

    void draw() {
        switch (game.mode) {
            case CHAPTER_SELECT -> chapters();
            case SLOTS -> saves();
            case SETTINGS -> settings();
            case KEYBINDS -> keybinds();
            case PAUSE -> pause();
            case JOURNAL -> glossary();
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
        // Extract the Right row's brush shape and fill the area where the sample key was painted.
        try (var source = new MenuPixmap(KEYBINDS_LAYOUT); var artwork = new MenuPixmap(KEY_BRUSH_WIDTH, KEY_BRUSH_HEIGHT)) {
            for (int y = 0; y < KEY_BRUSH_HEIGHT; y++) {
                int first = KEY_BRUSH_WIDTH, last = -1;
                for (int x = 0; x < KEY_BRUSH_WIDTH; x++) {
                    int rgba = source.pixmap.getPixel(KEY_BRUSH_RIGHT_X + x, 61 + y);
                    if ((rgba >>> 24) - (rgba >>> 16 & 255) > 90) {
                        artwork.pixmap.drawPixel(x, y, 0xffffffff);
                        first = Math.min(first, x);
                        last = x;
                    }
                }
                for (int x = Math.max(first, 18); x <= Math.min(last, 37); x++) {
                    artwork.pixmap.drawPixel(x, y, 0xffffffff);
                }
            }
            var texture = new Texture(artwork.pixmap);
            texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            return texture;
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
        rect(knob - 1, y - 1, 3, 3, sliderInk(state));
    }

    private Color sliderInk(State state) {
        return switch (state) {
            case IDLE -> EDGE;
            case HOVER -> GRAY;
            case PRESSED -> Color.WHITE;
        };
    }

    private void keybinds() {
        sheet(keybinds);
        rect(87, MenuButton.LEFT.top, 158, MenuButton.KEYBIND_LIST_HEIGHT, PAPER);
        rect(117, 116, 92, 14, PAPER); // The lettering sample in the artwork is replaced with live feedback.
        batch.flush();
        game.viewport.calculateScissors(batch.getTransformMatrix(), keybindList, keybindClip);
        if (ScissorStack.pushScissors(keybindClip)) {
            for (var action : Action.values()) keybindRow(action);
            batch.flush();
            ScissorStack.popScissors();
        }
        keybindRail();
        keybindDefault();
        String message = game.bindingMessage;
        if (message.isEmpty() && game.bindingKey) message = "PRESS A KEY. ESC TO CANCEL.";
        if (!message.isEmpty()) lettering.wrapped(batch, message, 81, 116, 158, ALERT);
        back(keybinds);
    }

    private void keybindRow(Action action) {
        var control = MenuButton.BINDINGS[action.ordinal()];
        var state = state(control);
        boolean capturing = game.bindingKey && game.selectedBinding == action;
        int shift = state.shift, top = control.top - game.keybindScroll + shift;
        if (action == Action.UNDO) caption("UNDO", 91 + shift, top + 5, 49, 5, OUTLINE, false);
        else {
            batch.setColor(Color.WHITE);
            int sourceTop = control.top + (action == Action.CLEAR ? 6 : 5);
            drawRegion(batch, keybinds, 91, sourceTop, 49, 5, 91 + shift, top + 5);
        }
        String value = capturing ? "..." : game.bindings.keycap(action);
        batch.setColor(capturing || state == State.PRESSED ? ALERT : state == State.HOVER ? BRUSH_HOVER : BRUSH);
        // Extend the centre of the brush so long key names still fit the column.
        int grow = Math.clamp((lettering.measure(value) + 9 - KEY_BRUSH_WIDTH) / 2, 0, 17);
        int half = KEY_BRUSH_WIDTH / 2;
        int brushWidth = KEY_BRUSH_WIDTH + 2 * grow;
        int baseX = action.ordinal() % 2 == 0 ? KEY_BRUSH_LEFT_X : KEY_BRUSH_RIGHT_X;
        // Leave two pixels for the shake even when a long key name expands toward the rail.
        int brushX = Math.min(baseX - grow, control.x + control.width - brushWidth - 2)
            + shift + keyShakeOffset(action);
        int brushTop = top + 2;
        drawRegion(batch, keyBrush, 0, 0, half, KEY_BRUSH_HEIGHT, brushX, brushTop);
        if (grow > 0) batch.draw(keyBrush, (brushX + half) * ART_PIXEL,
            (ART_HEIGHT - brushTop - KEY_BRUSH_HEIGHT) * ART_PIXEL, grow * 2 * ART_PIXEL,
            KEY_BRUSH_HEIGHT * ART_PIXEL, half, 0, 1, KEY_BRUSH_HEIGHT, false, false);
        drawRegion(batch, keyBrush, half, 0, half, KEY_BRUSH_HEIGHT, brushX + half + grow * 2, brushTop);
        int textX = brushX + 4, textTop = top + 4, textWidth = brushWidth - 8;
        for (int y = -1; y <= 1; y++) {
            for (int x = -1; x <= 1; x++) {
                if (x != 0 || y != 0) caption(value, textX + x, textTop + y, textWidth, 5, OUTLINE, true);
            }
        }
        caption(value, textX, textTop, textWidth, 5, PAPER, true);
    }

    private int keyShakeOffset(Action action) {
        if (action != game.selectedBinding || game.bindingChangedAt < 0) return 0;
        return shakeOffset(game.bindingChangedAt);
    }

    private int shakeOffset(double startedAt) {
        int frame = (int) ((game.elapsed - startedAt) / .045);
        return frame < SHAKE_OFFSETS.length ? SHAKE_OFFSETS[frame] : 0;
    }

    private void keybindRail() {
        var rail = MenuButton.KEYBIND_SCROLL;
        int x = rail.x + rail.width / 2;
        int top = MenuButton.keybindThumbTop(game.keybindScroll);
        int height = MenuButton.KEYBIND_THUMB_HEIGHT;
        rect(x, rail.top, 1, rail.height, GRAY);
        rect(x - 1, rail.top + 1, 3, rail.height - 2, GRAY);
        rect(x, top, 1, height, SHADOW);
        rect(x - 1, top + 1, 3, height - 2, SHADOW);
        rect(x, top + 1, 1, height - 2, sliderInk(state(rail)));
    }

    private void keybindDefault() {
        var button = MenuButton.DEFAULT;
        var state = state(button);
        int sourceX = switch (state) { case IDLE -> button.x; case HOVER -> 256; case PRESSED -> 257; };
        int sourceTop = state == State.HOVER ? 121 : button.top;
        rect(button.x, button.top, button.width, button.height, PAPER);
        batch.setColor(Color.WHITE);
        drawRegion(batch, keybinds, sourceX, sourceTop, button.width - state.shift, button.height - state.shift,
            button.x + state.shift, button.top + state.shift);
    }

    private void glossary() {
        sheet(glossary);
        rect(80, 52, 159, 12, PAPER);
        glossaryTab(MenuButton.KANJI, GlossaryTab.KANJI, 84, 54, 30);
        glossaryTab(MenuButton.HIRAGANA, GlossaryTab.HIRAGANA, 130, 56, 44);
        glossaryTab(MenuButton.KATAKANA, GlossaryTab.KATAKANA, 191, 54, 47);
        glossaryArrow(MenuButton.PREVIOUS, 80, 261);
        glossaryArrow(MenuButton.NEXT, 236, 269);
        var practice = MenuButton.PRACTICE;
        var state = state(practice);
        rect(practice.x, practice.top, practice.width, practice.height, PAPER);
        if (game.run.phase == com.yogiiefe.inkslayer.world.CampaignRun.Phase.WALK) {
            int sourceX = switch (state) { case IDLE -> 127; case HOVER -> 253; case PRESSED -> 254; };
            int sourceTop = switch (state) { case IDLE -> 139; case HOVER -> 135; case PRESSED -> 150; };
            batch.setColor(Color.WHITE);
            drawRegion(batch, glossary, sourceX, sourceTop, practice.width - state.shift, practice.height - state.shift,
                practice.x + state.shift + glossaryShakeOffset(practice), practice.top + state.shift);
        }
        back(glossary);
        int left = (ART_WIDTH - GLOSSARY_CONTENT_WIDTH) / 2;
        int writingTop = GLOSSARY_CENTER_Y - GLOSSARY_WRITING_HEIGHT / 2;
        // The tallest kanji leave about three pixels of clear space inside the writing frame.
        int pageTop = writingTop - GLOSSARY_LABEL_HEIGHT;
        int romajiTop = writingTop + GLOSSARY_WRITING_HEIGHT;
        caption(game.glossaryTab.name() + "  " + (game.journalPage + 1) + "/" + game.glossarySize(),
            left, pageTop, GLOSSARY_CONTENT_WIDTH, GLOSSARY_LABEL_HEIGHT, OUTLINE, true);
        writing.drawWord(batch, game.glossaryText(), game.elapsed - game.pageStartedAt,
            left, writingTop, GLOSSARY_CONTENT_WIDTH, GLOSSARY_WRITING_HEIGHT);
        caption(game.glossaryRomaji(), left, romajiTop, GLOSSARY_CONTENT_WIDTH, GLOSSARY_LABEL_HEIGHT, OUTLINE, true);
    }

    private void glossaryTab(MenuButton tab, GlossaryTab category, int x, int sourceTop, int width) {
        var state = state(tab);
        boolean highlighted = state != State.IDLE || game.glossaryTab == category;
        int drawX = x + glossaryShakeOffset(tab);
        batch.setColor(Color.WHITE);
        drawRegion(batch, glossary, x, sourceTop, width, 5, drawX, (highlighted ? 54 : 56) + state.shift);
        if (highlighted) {
            int sampleX = tab == MenuButton.KANJI ? 84 : 191;
            int sampleWidth = tab == MenuButton.KANJI ? 30 : 47;
            drawRegion(batch, glossary, sampleX, 60, sampleWidth, 3,
                drawX + (width - sampleWidth) / 2, 60 + state.shift);
        }
    }

    private int glossaryShakeOffset(MenuButton button) {
        if (game.glossaryPressedButton != button || game.menuPress == button && game.menuHover != button) return 0;
        return shakeOffset(game.glossaryPressedAt);
    }

    private void glossaryArrow(MenuButton button, int x, int sampleX) {
        var state = state(button);
        int sourceTop = switch (state) { case IDLE -> 92; case HOVER -> 71; case PRESSED -> 85; };
        batch.setColor(Color.WHITE);
        drawRegion(batch, glossary, state == State.IDLE ? x : sampleX, sourceTop, 5, 9, x, 92);
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
        back(settings);
    }

    private void back(Texture artwork) {
        var button = MenuButton.BACK;
        var state = state(button);
        int sourceX = switch (state) { case IDLE -> button.x; case HOVER -> 256; case PRESSED -> 280; };
        rect(button.x, button.top, button.width, button.height, PAPER);
        batch.setColor(Color.WHITE);
        drawRegion(batch, artwork, sourceX, button.top + state.shift, button.width - state.shift, button.height - state.shift,
            button.x + state.shift, button.top + state.shift);
    }

    private State state(MenuButton control) {
        if (game.keyboardPress == control) return State.PRESSED;
        if (game.menuPress == control && (game.menuHover == control || control.draggable())) return State.PRESSED;
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
        lettering.dispose();
        saves.dispose();
        pause.dispose();
        glossary.dispose();
    }

    private static final class MenuPixmap implements AutoCloseable {
        final Pixmap pixmap;
        MenuPixmap(String path) { pixmap = new Pixmap(Gdx.files.internal(path)); }
        MenuPixmap() { this(ART_WIDTH, ART_HEIGHT); }
        MenuPixmap(int width, int height) {
            pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
            pixmap.setBlending(Pixmap.Blending.None);
        }
        @Override public void close() { pixmap.dispose(); }
    }
}
