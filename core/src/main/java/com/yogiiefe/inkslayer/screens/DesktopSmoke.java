package com.yogiiefe.inkslayer.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.math.Vector2;
import com.yogiiefe.inkslayer.audio.GameAudio.Channel;
import com.yogiiefe.inkslayer.screens.KeyBindings.Action;

import com.yogiiefe.inkslayer.world.CampaignRun;
import com.yogiiefe.inkslayer.world.CampaignRun.Phase;

import static com.yogiiefe.inkslayer.screens.GameScreen.*;

/** Opt-in packaged desktop probe. Generates mouse input from the reference stroke paths. */
final class DesktopSmoke {
    private int frames, battles, questions;
    private boolean invalidChecked, reloaded, practiceChecked, firstBrushShown, glossaryShown, practiceShown, pauseShown;
    private boolean resizing;
    private boolean initialGuideShown;
    private boolean skipSecondFrameShown;

    void frame(GameScreen g) {
        if (resizing) return;
        frames++;
        require(frames < 6500, "campaign timed out");
        if (frames < 5) return;
        if (frames >= 5 && frames <= 7) {
            titleMenu(g);
            return;
        }
        if (frames == 8) {
            capture("settings");
            resizing = true;
            try {
                for (int i = 0; i < 3; i++) key(Input.Keys.DOWN);
                key(Input.Keys.RIGHT);
                require(g.displayMode == DisplayMode.BORDERLESS && !Gdx.graphics.isFullscreen(), "borderless mode");
                key(Input.Keys.RIGHT);
                require(g.displayMode == DisplayMode.FULLSCREEN && Gdx.graphics.isFullscreen(), "fullscreen mode");
                key(Input.Keys.RIGHT);
                require(g.displayMode == DisplayMode.WINDOWED && !Gdx.graphics.isFullscreen(), "windowed mode");
                require(Gdx.app.getPreferences("inkslayer-display").getString("mode").equals("WINDOWED"), "display preference");
                for (int i = 0; i < 3; i++) key(Input.Keys.UP);
            } finally {
                resizing = false;
            }
            int value = g.audio.volume(Channel.MASTER);
            key(Input.Keys.LEFT);
            require(g.audio.volume(Channel.MASTER) == Math.max(0, value - 5), "master slider");
            var slider = MenuButton.MASTER;
            var grab = screen(g, slider.centerX(), slider.centerY());
            var end = screen(g, 1100, slider.centerY());
            var input = Gdx.input.getInputProcessor();
            input.touchDown(Math.round(grab.x), Math.round(grab.y), 0, Input.Buttons.LEFT);
            input.touchDragged(Math.round(end.x), Math.round(end.y), 0);
            require(g.menuPress == slider && g.audio.volume(Channel.MASTER) == 100, "slider drag clamped to endpoint");
            input.touchUp(Math.round(end.x), Math.round(end.y), 0, Input.Buttons.LEFT);
            require(g.menuPress == null, "slider released");
            g.audio.volume(Channel.MASTER, value);
            click(g, MenuButton.STROKE.centerX(), MenuButton.STROKE.centerY());
            require(g.strict, "stroke mode toggle");
            click(g, MenuButton.STROKE.centerX(), MenuButton.STROKE.centerY());
            click(g, MenuButton.KEYBINDS.centerX(), MenuButton.KEYBINDS.centerY());
            require(g.mode == GameScreen.Mode.KEYBINDS, "open Keybinds from Settings");
            return;
        }
        if (frames == 9) {
            capture("keybinds");
            for (int i = 0; i < Action.HELP.ordinal(); i++) key(Input.Keys.DOWN);
            key(Input.Keys.ENTER);
            key(Input.Keys.ENTER);
            require(g.bindingKey && !g.bindingMessage.isEmpty(), "duplicate gameplay keys must be rejected");
            key(Input.Keys.G);
            require(!g.bindingKey && new KeyBindings().matches(Action.HELP, Input.Keys.G), "help key persists");
            click(g, MenuButton.DEFAULT.centerX(), MenuButton.DEFAULT.centerY());
            require(new KeyBindings().matches(Action.HELP, Input.Keys.H), "default keybind persists");
            click(g, MenuButton.HELP.centerX(), MenuButton.HELP.centerY());
            key(Input.Keys.G);
            click(g, MenuButton.BACK.centerX(), MenuButton.BACK.centerY());
            require(g.mode == Mode.SETTINGS, "keybind back arrow");
            click(g, MenuButton.BACK.centerX(), MenuButton.BACK.centerY());
            key(Input.Keys.UP);
            key(Input.Keys.ENTER);
            require(g.mode == Mode.SLOTS, "open saves");
            return;
        }
        if (frames == 10) {
            capture("saves");
            click(g, MenuButton.BACK.centerX(), MenuButton.BACK.centerY());
            require(g.mode == Mode.TITLE, "saves back arrow");
            key(Input.Keys.UP);
            key(Input.Keys.ENTER);
            require(g.mode == Mode.CHAPTER_SELECT && g.menuFocus == MenuButton.ELEMENTS, "New must open chapter selection");
            require(!g.saveStore.inspect(0).occupied(), "use an empty -Dinkslayer.saves directory for smoke");
            return;
        }
        if (frames == 11) {
            capture("chapter-select");
            var card = MenuButton.PETS;
            var start = screen(g, card.centerX(), card.centerY());
            var outside = screen(g, 72, 540);
            var input = Gdx.input.getInputProcessor();
            input.mouseMoved(Math.round(start.x), Math.round(start.y));
            require(g.menuHover == card, "chapter card hover");
            input.touchDown(Math.round(start.x), Math.round(start.y), 0, Input.Buttons.LEFT);
            require(g.mode == Mode.CHAPTER_SELECT && g.menuPress == card, "chapter card press waits for release");
            input.touchUp(Math.round(outside.x), Math.round(outside.y), 0, Input.Buttons.LEFT);
            require(g.mode == Mode.CHAPTER_SELECT && g.menuPress == null, "releasing outside cancels chapter selection");
            key(Input.Keys.LEFT);
            key(Input.Keys.ENTER);
            require(g.mode == Mode.CINEMATIC && g.tutorial && g.panel == 0, "Elements starts its prologue");
            require(g.panels().getFirst().equals("In this world"), "Elements opening card");
            key(Input.Keys.ENTER);
            key(Input.Keys.SPACE);
            require(g.mode == Mode.CINEMATIC && g.panel == 0, "old Next keys must not advance a cinematic");
            return;
        }
        if (frames == 12) {
            capture("opening");
            hoverSkip(g);
            return;
        }

        switch (g.mode) {
            case GAME -> play(g);
            case PAUSE -> {
                if (g.tutorial || g.run.phase != Phase.WALK) {
                    key(Input.Keys.ESCAPE);
                    return;
                }
                if (!pauseShown) {
                    capture("pause");
                    pauseShown = true;
                    click(g, MenuButton.PAUSE_SETTINGS.centerX(), MenuButton.PAUSE_SETTINGS.centerY());
                    require(g.mode == Mode.SETTINGS, "pause opens Settings");
                    click(g, MenuButton.BACK.centerX(), MenuButton.BACK.centerY());
                    require(g.mode == Mode.PAUSE, "settings back returns to Pause");
                }
                key(practiceChecked ? Input.Keys.ESCAPE : Input.Keys.TAB);
            }
            case JOURNAL -> {
                if (!glossaryShown && g.glossaryTab == GameScreen.GlossaryTab.KANJI) {
                    capture("glossarium");
                    glossaryShown = true;
                }
                if (practiceChecked) key(Input.Keys.ESCAPE);
                else checkPractice(g);
            }
            case PRACTICE -> {
                if (!practiceShown) { capture("practice"); practiceShown = true; }
                key(Input.Keys.ESCAPE);
            }
            case CINEMATIC -> {
                if (!g.ending) {
                    checkChapterOpening(g);
                    return;
                }
                require(g.ending && battles == 40, "ending before all 40 battles");
                require(g.saveStore.inspect(0).progress().endingPending(), "missing ending checkpoint");
                int panel = g.panel;
                if (!g.cinematicTextComplete) {
                    click(g, 80, 580);
                    require(g.mode == Mode.CINEMATIC && g.panel == panel && g.cinematicTextComplete,
                        "first click reveals the epilogue sentence");
                    return;
                }
                capture("ending-slide-" + (panel + 1));
                if (panel + 1 < g.panels().size()) {
                    click(g, 80, 580);
                    require(g.mode == Mode.CINEMATIC && g.panel == panel + 1, "clicking the epilogue advances one card");
                    return;
                }
                skipCinematic(g);
                require(g.mode == GameScreen.Mode.TITLE && g.saveStore.inspect(0).progress().completed(), "ending did not save");
                checkDirectChapterStarts(g);
                checkDeleteDialog(g);
                Gdx.app.log("SMOKE", "PASS: chapter selection, cinematic-only story, click-only cinematic advance, Skip, chapter checkpoints, keybinds, guides, "
                    + battles + " encounters, "
                    + questions + " accepted quiz answers, checkpoint reload and ending");
                Gdx.app.exit();
            }
            default -> throw new IllegalStateException("SMOKE: unexpected " + g.mode);
        }
    }

    private void checkChapterOpening(GameScreen g) {
        int chapter = g.run.stage;
        require(g.run.encounter == 0 && chapter == battles / 10, "unexpected chapter opening");
        if (chapter == 0 && g.panel == 0) {
            if (!skipSecondFrameShown && g.skipHoverAt < 0) {
                hoverSkip(g);
            }
            if (frames == 20) capture("opening-typewriter");
            if (!skipSecondFrameShown && g.skipHoverAt >= 0
                && TitleMenu.hoverArrowOffset(g.elapsed - g.skipHoverAt) == 2) {
                capture("skip-hover-frame-2");
                skipSecondFrameShown = true;
            }
            if (g.elapsed - g.panelStartedAt < 5) return;
            require(skipSecondFrameShown, "Skip must reach its second animation frame on hover");
            if (g.skipHoverAt >= 0) capture("opening-skip-hover");
        } else {
            if (chapter > 0) {
                var saved = g.saveStore.inspect(0).progress();
                require(saved.stage() == chapter && saved.encounter() == 0, "chapter transition checkpoint");
            }
            if (!g.cinematicTextComplete) {
                int panel = g.panel;
                click(g, 395, 72);
                require(g.mode == Mode.CINEMATIC && g.panel == panel && g.cinematicTextComplete,
                    "first click reveals the chapter sentence");
                return;
            }
        }
        capture("chapter-" + (chapter + 1) + "-slide-" + (g.panel + 1));
        int panel = g.panel;
        click(g, 395, 72);
        if (g.mode == Mode.CINEMATIC) {
            require(g.panel == panel + 1, "click advances exactly one chapter slide");
            return;
        }
        require(g.mode == Mode.GAME && g.tutorial == (chapter == 0), "opening must enter the selected stage");
        if (chapter == 0) require(!g.saveStore.inspect(0).occupied(), "tutorial should precede first checkpoint");
    }

    private void hoverSkip(GameScreen g) {
        Vector2 skip = screen(g, SKIP_X + SKIP_WIDTH / 2f, SKIP_Y + SKIP_HEIGHT / 2f);
        int x = Math.round(skip.x), y = Math.round(skip.y);
        Gdx.input.setCursorPosition(x, y);
        Gdx.input.getInputProcessor().mouseMoved(x, y);
    }

    private void checkDirectChapterStarts(GameScreen g) {
        int artPixel = PixelArtViewport.ART_PIXEL;
        for (int chapter = 1; chapter < g.content.stages.size(); chapter++) {
            click(g, 290 * artPixel, (PixelArtViewport.ART_HEIGHT - 132) * artPixel);
            require(g.mode == Mode.CHAPTER_SELECT, "return to chapter selection");
            var card = MenuButton.CHAPTERS[chapter];
            click(g, card.centerX(), card.centerY());
            require(g.mode == Mode.CINEMATIC && !g.tutorial && g.run.stage == chapter && g.run.encounter == 0,
                "direct chapter start");
            require(g.learning.isEmpty(), "direct chapter must start a fresh run");
            int expectedSlot = chapter < 3 ? chapter : -1;
            require(g.slot == expectedSlot, "chapter should use an empty slot or play without saving");
            if (expectedSlot >= 0) {
                var saved = g.saveStore.inspect(expectedSlot).progress();
                require(saved.stage() == chapter && saved.encounter() == 0, "direct chapter checkpoint");
            }
            skipCinematic(g);
            require(g.mode == Mode.GAME && g.run.phase == Phase.WALK && g.run.stage == chapter, "direct chapter is playable");
            key(Input.Keys.ESCAPE);
            key(Input.Keys.S);
            require(g.mode == Mode.TITLE, "exit directly selected chapter");
        }
        require(g.saveStore.inspect(0).progress().completed(), "direct starts must preserve the completed slot");
        require(g.saveStore.inspect(1).progress().stage() == 1 && g.saveStore.inspect(2).progress().stage() == 2,
            "starting a chapter with full slots must not overwrite a save");
        key(Input.Keys.DOWN);
        key(Input.Keys.ENTER);
        key(Input.Keys.DOWN);
        key(Input.Keys.ENTER);
        require(g.mode == Mode.CINEMATIC && g.run.stage == 1 && g.run.encounter == 0, "chapter-start save reopens the cinematic");
        skipCinematic(g);
        require(g.mode == Mode.GAME && g.slot == 1, "chapter-start save restores its run");
    }

    private void checkDeleteDialog(GameScreen g) {
        var input = Gdx.input.getInputProcessor();
        key(Input.Keys.ESCAPE);
        key(Input.Keys.S);
        click(g, 282 * PixelArtViewport.ART_PIXEL, (PixelArtViewport.ART_HEIGHT - 143) * PixelArtViewport.ART_PIXEL);
        require(g.mode == Mode.SLOTS && g.slots.getFirst().occupied(), "occupied save row");
        var trash = MenuButton.TRASH_1;
        var p = screen(g, trash.centerX(), trash.centerY());
        input.mouseMoved(Math.round(p.x), Math.round(p.y));
        require(g.menuHover == trash, "trash button hover");
        click(g, trash.centerX(), trash.centerY());
        require(g.confirmDelete, "trash asks for confirmation");
        click(g, MenuButton.CANCEL_DELETE.centerX(), MenuButton.CANCEL_DELETE.centerY());
        require(!g.confirmDelete && g.saveStore.inspect(0).occupied(), "cancel preserves save");
    }

    private void titleMenu(GameScreen g) {
        var input = Gdx.input.getInputProcessor();
        int artPixel = PixelArtViewport.ART_PIXEL;
        var settings = screen(g, 282 * artPixel, (PixelArtViewport.ART_HEIGHT - 155) * artPixel);
        int x = Math.round(settings.x), y = Math.round(settings.y);
        if (frames == 5) {
            capture("title");
            key(Input.Keys.DOWN);
            require(g.titleSelection == TitleMenu.Option.SAVE, "title arrow's Save target");
            return;
        }
        if (frames == 6) {
            capture("title-save-selected");
            key(Input.Keys.DOWN);
            require(g.titleSelection == TitleMenu.Option.SETTINGS, "title keyboard navigation");
            var blank = screen(g, 160 * artPixel, 90 * artPixel);
            input.mouseMoved(Math.round(blank.x), Math.round(blank.y));
            require(g.titleHover == null, "blank title background must not select a menu option");
            input.mouseMoved(x, y);
            require(g.titleHover == TitleMenu.Option.SETTINGS, "title Settings hover");
            double hoverAt = g.titleHoverAt;
            input.mouseMoved(x + 1, y);
            require(g.titleHover == TitleMenu.Option.SETTINGS && g.titleHoverAt == hoverAt,
                "mouse movement within Settings must not restart hover");
            return;
        }
        if (g.titleHover != TitleMenu.Option.SETTINGS) input.mouseMoved(x, y);
        if (g.elapsed - g.titleHoverAt < 0.25) {
            frames--;
            return;
        }
        capture("title-hover");
        input.touchDown(x, y, 0, Input.Buttons.LEFT);
        input.touchUp(x, y, 0, Input.Buttons.LEFT);
        require(g.mode == GameScreen.Mode.SETTINGS, "title Settings click");
    }

    private void checkPractice(GameScreen g) {
        require(g.glossarySize() == 39, "Kanji glossary coverage");
        key(Input.Keys.NUM_2);
        key(Input.Keys.LEFT);
        require(g.glossarySize() == 46 && "ん".equals(g.glossaryText()) && "n".equals(g.glossaryRomaji()),
            "Hiragana glossary navigation");
        click(g, MenuButton.NEXT.centerX(), MenuButton.NEXT.centerY());
        require(g.journalPage == 0, "right arrow wraps to the first page");
        click(g, MenuButton.PRACTICE.centerX(), MenuButton.PRACTICE.centerY());
        require(g.mode == Mode.PRACTICE && "あ".equals(g.guideGlyph) && "a".equals(g.glossaryRomaji()),
            "practice the first hiragana");
        draw(g, "あ");
        key(Input.Keys.ENTER);
        require("Correct!".equals(g.feedback), "accept the displayed hiragana");
        key(Input.Keys.ESCAPE);
        click(g, MenuButton.KATAKANA.centerX(), MenuButton.KATAKANA.centerY());
        key(Input.Keys.LEFT);
        require(g.glossarySize() == 46 && "ン".equals(g.glossaryText())
            && g.journalPage == g.glossarySize() - 1, "Katakana glossary coverage");
        key(Input.Keys.Q);
        require(g.mode == Mode.PRACTICE && "ン".equals(g.guideGlyph), "practice the last katakana");
        key(Input.Keys.ENTER);
        require(g.feedback.contains("ン"), "prompt to draw the selected katakana on an empty canvas");
        draw(g, "ン");
        key(Input.Keys.ENTER);
        require("Correct!".equals(g.feedback), "accept the displayed katakana");
        key(Input.Keys.ESCAPE);
        key(Input.Keys.NUM_1);
        key(Input.Keys.Q);
        require(g.mode == GameScreen.Mode.PRACTICE && "水".equals(g.guideGlyph), "safe practice route and guide");
        draw(g, "水");
        key(Input.Keys.ENTER);
        require(g.learning.containsKey("水") && g.run.phase == Phase.WALK, "practice changed the campaign state");
        practiceChecked = true;
    }

    private void play(GameScreen g) {
        if (g.tutorial) {
            switch (g.run.phase) {
                case WALK -> g.run.x = CampaignRun.ENCOUNTER_X;
                case LESSON -> {
                    if (g.revealed()) return;
                    if (!initialGuideShown) {
                        require("水".equals(g.guideGlyph), "tutorial guide must appear automatically");
                        checkPixelScaling(g.viewport);
                        initialGuideShown = true;
                    }
                    if (g.guideGlyph != null) return;
                    capture("tutorial");
                    key(Input.Keys.H);
                    require(g.guideGlyph == null, "old help key must no longer show the guide");
                    key(Input.Keys.G);
                    require("水".equals(g.guideGlyph) && g.guideAge == 0, "remapped help must replay the guide from the start");
                    key(Input.Keys.ENTER);
                    require(!g.revealed() && g.run.hp == CampaignRun.MAX_HP, "failed tutorial attempt dealt damage");
                    draw(g, "水");
                    key(Input.Keys.ENTER);
                    require(g.revealed(), "tutorial rejected Water");
                }
                case WON -> {
                    key(Input.Keys.ENTER);
                    require(!g.tutorial && g.mode == Mode.GAME
                        && g.saveStore.inspect(0).progress().encounter() == 0, "Stage 1 checkpoint");
                }
                default -> throw new IllegalStateException("Unexpected tutorial phase");
            }
            return;
        }
        switch (g.run.phase) {
            case WALK -> {
                if (battles == 0 && !pauseShown) {
                    key(Input.Keys.TAB);
                    require(g.mode == GameScreen.Mode.GAME, "Glossarium must open only from Pause during gameplay");
                    key(Input.Keys.ESCAPE);
                    return;
                }
                if (battles == 2 && !reloaded) {
                    key(Input.Keys.ESCAPE);
                    key(Input.Keys.S);
                    require(g.mode == GameScreen.Mode.TITLE, "save and return");
                    key(Input.Keys.DOWN);
                    key(Input.Keys.ENTER);
                    require(g.mode == GameScreen.Mode.SLOTS, "Save menu");
                    capture("saves");
                    key(Input.Keys.ENTER);
                    require(g.mode == GameScreen.Mode.GAME && g.run.encounter == 2 && g.learning.containsKey("水"), "checkpoint load");
                    reloaded = true;
                }
                g.run.x = CampaignRun.ENCOUNTER_X;
            }
            case LESSON -> {
                if (g.revealed()) return;
                if (!invalidChecked) {
                    key(Input.Keys.ENTER);
                    require(!g.revealed(), "empty drawing accepted");
                    invalidChecked = true;
                }
                if (g.run.isBoss()) {
                    for (int cp : g.quiz().katakana().codePoints().toArray()) {
                        int index = g.tiles().indexOf(Character.toString(cp));
                        while (g.tileSelection != index) key(Input.Keys.RIGHT);
                        key(Input.Keys.SPACE);
                    }
                    if (g.bossQuestionNumber() == 1) capture("kotoba");
                } else {
                    if (battles == 0 && !firstBrushShown) {
                        require(g.quiz().kanji().equals(g.guideGlyph), "battle guide must appear automatically");
                        draw(g, g.quiz().kanji());
                        firstBrushShown = true;
                        return;
                    }
                    if (battles == 0) capture("battle");
                    else draw(g, g.quiz().kanji());
                }
                key(Input.Keys.ENTER);
                require(g.revealed(), "quiz answer rejected: " + g.quiz().kanji() + " / " + g.feedback);
                questions++;
            }
            case ATTACK, DEFEND -> throw new IllegalStateException("Legacy combat phase reached");
            case WON -> {
                battles++;
                require(g.saveStore.inspect(0).progress().battlesWon() == battles,
                    "every victory must save its checkpoint before Continue");
                if (battles == 10 || battles == 20 || battles == 30 || battles == 40) capture("stage-" + g.run.stage);
                key(Input.Keys.ENTER);
            }
            case LOST -> throw new IllegalStateException("SMOKE: player defeated");
        }
    }



    private void draw(GameScreen g, String glyph) {
        var input = Gdx.input.getInputProcessor();
        var strokes = g.mode == Mode.PRACTICE && glyph.equals(g.guideGlyph) ? g.guideStrokes() : g.templates.get(glyph);
        for (var stroke : strokes) {
            for (int index = 0; index < stroke.size(); index++) {
                var point = stroke.get(index);
                float uiX = GameScreen.GUIDE_X + (float) point.x() * GameScreen.GUIDE_SIZE / 109;
                float uiY = GameScreen.GUIDE_Y + GameScreen.GUIDE_SIZE - (float) point.y() * GameScreen.GUIDE_SIZE / 109;
                if (battles > 0 || g.mode == GameScreen.Mode.PRACTICE) {
                    if (index == 0) require(g.beginInk(uiX, uiY, 0), "quiz canvas was not ready");
                    else if (index + 1 == stroke.size()) g.endInk(uiX, uiY, 0);
                    else g.moveInk(uiX, uiY, 0);
                    continue;
                }
                var p = screen(g, uiX, uiY);
                int x = Math.round(p.x), y = Math.round(p.y);
                if (index == 0) input.touchDown(x, y, 0, Input.Buttons.LEFT);
                else if (index + 1 == stroke.size()) input.touchUp(x, y, 0, Input.Buttons.LEFT);
                else input.touchDragged(x, y, 0);
            }
        }
        require(g.drawing.count() == strokes.size(), "mouse strokes lost for U+"
            + Integer.toHexString(glyph.codePointAt(0)) + ": " + g.drawing.count() + "/"
            + strokes.size() + " in " + g.mode);
    }

    private void key(int key) {
        var input = Gdx.input.getInputProcessor();
        input.keyDown(key);
        input.keyUp(key);
    }

    private void skipCinematic(GameScreen g) {
        click(g, SKIP_X + SKIP_WIDTH / 2f, SKIP_Y + SKIP_HEIGHT / 2f);
    }

    private void click(GameScreen g, float worldX, float worldY) {
        Vector2 p = screen(g, worldX, worldY);
        var input = Gdx.input.getInputProcessor();
        int x = Math.round(p.x), y = Math.round(p.y);
        input.touchDown(x, y, 0, Input.Buttons.LEFT);
        input.touchUp(x, y, 0, Input.Buttons.LEFT);
    }

    private void checkPixelScaling(PixelArtViewport viewport) {
        int width = Gdx.graphics.getBackBufferWidth(), height = Gdx.graphics.getBackBufferHeight();
        int scale = Math.min(width / PixelArtViewport.ART_WIDTH, height / PixelArtViewport.ART_HEIGHT);
        int contentWidth = PixelArtViewport.ART_WIDTH * scale, contentHeight = PixelArtViewport.ART_HEIGHT * scale;
        require(scale > 0 && viewport.getScreenWidth() == contentWidth && viewport.getScreenHeight() == contentHeight,
            "art viewport must use the largest whole-pixel scale");
        require(viewport.getScreenX() == (width - contentWidth) / 2 && viewport.getScreenY() == (height - contentHeight) / 2,
            "letterbox must be centered in the backbuffer");

        // At the arena's left edge, the floor line is exactly one art pixel high.
        int floor = viewport.getScreenY() + GameScreen.CANVAS_TOP / PixelArtViewport.ART_PIXEL * scale;
        var pixels = Pixmap.createFromFrameBuffer(viewport.getScreenX() + scale, floor - scale - 1, 1, scale + 2);
        try {
            int line = pixels.getPixel(0, 1);
            require(pixels.getPixel(0, 0) == 0xffffffff && line != 0xffffffff && line != pixels.getPixel(0, scale + 1),
                "floor line must sit between the canvas and arena");
            for (int y = 1; y <= scale; y++)
                require(pixels.getPixel(0, y) == line, "art pixel must span exactly " + scale + " backbuffer rows");
        } finally {
            pixels.dispose();
        }
    }

    private Vector2 screen(GameScreen g, float x, float y) {
        Vector2 p = g.viewport.project(new Vector2(x, y));
        p.y = Gdx.graphics.getHeight() - p.y;
        return p;
    }

    private void capture(String name) {
        var file = Gdx.files.absolute(System.getProperty("inkslayer.screenshots", "build/screenshots") + "/" + name + ".png");
        file.parent().mkdirs();
        var pixmap = Pixmap.createFromFrameBuffer(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
        try { PixmapIO.writePNG(file, pixmap, -1, true); }
        finally { pixmap.dispose(); }
    }

    private void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException("SMOKE: " + message);
    }
}
