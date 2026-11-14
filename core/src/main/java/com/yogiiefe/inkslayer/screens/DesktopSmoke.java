package com.yogiiefe.inkslayer.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.math.Vector2;
import com.yogiiefe.inkslayer.audio.GameAudio.Channel;

import com.yogiiefe.inkslayer.world.CampaignRun;
import com.yogiiefe.inkslayer.world.CampaignRun.Phase;

/** Opt-in packaged desktop probe. Generates mouse input from the reference stroke paths. */
final class DesktopSmoke {
    private int frames, battles, questions;
    private boolean invalidChecked, reloaded, practiceChecked, firstBrushShown, glossaryShown, practiceShown, pauseShown;
    private boolean resizing;

    void frame(GameScreen g) {
        if (resizing) return;
        frames++;
        require(frames < 6500, "campaign timed out");
        if (frames < 5) return;
        if (frames == 5) {
            capture("title");
            key(Input.Keys.DOWN);
            key(Input.Keys.DOWN);
            key(Input.Keys.ENTER);
            require(g.mode == GameScreen.Mode.SETTINGS, "settings route");
            return;
        }
        if (frames == 6) {
            capture("settings");
            resizing = true;
            try {
                key(Input.Keys.UP);
                key(Input.Keys.RIGHT);
                require(g.displayMode == DisplayMode.BORDERLESS && !Gdx.graphics.isFullscreen(), "borderless mode");
                key(Input.Keys.RIGHT);
                require(g.displayMode == DisplayMode.FULLSCREEN && Gdx.graphics.isFullscreen(), "fullscreen mode");
                key(Input.Keys.RIGHT);
                require(g.displayMode == DisplayMode.WINDOWED && !Gdx.graphics.isFullscreen(), "windowed mode");
                require(Gdx.app.getPreferences("inkslayer-display").getString("mode").equals("WINDOWED"), "display preference");
                key(Input.Keys.DOWN);
            } finally {
                resizing = false;
            }
            int value = g.audio.volume(Channel.MASTER);
            key(Input.Keys.LEFT);
            require(g.audio.volume(Channel.MASTER) == Math.max(0, value - 5), "master slider");
            g.audio.volume(Channel.MASTER, value);
            key(Input.Keys.ESCAPE);
            key(Input.Keys.UP);
            key(Input.Keys.UP);
            key(Input.Keys.ENTER);
            require(g.mode == GameScreen.Mode.CINEMATIC && g.panel == 0, "New must open the prologue directly");
            require(g.slot == 0 && !g.saveStore.inspect(0).occupied(), "use an empty -Dinkslayer.saves directory for smoke");
            return;
        }
        if (frames == 7) {
            capture("opening");
            key(Input.Keys.S);
            require(g.mode == GameScreen.Mode.GAME && g.tutorial && !g.saveStore.inspect(0).occupied(), "opening must enter tutorial");
            return;
        }

        switch (g.mode) {
            case GAME -> play(g);
            case STORY -> {
                if (g.story == StoryBeat.FIRST_MASK && g.storyLine == 0) {
                    require(g.saveStore.inspect(0).progress().encounter() == 0, "checkpoint advanced before mask reveal");
                    capture("mask-reveal");
                    key(Input.Keys.ENTER);
                    require(g.storyLine == 1, "mask dialogue did not advance");
                } else key(Input.Keys.S);
            }
            case PAUSE -> {
                if (!pauseShown) { capture("pause"); pauseShown = true; }
                key(Input.Keys.ESCAPE);
            }
            case JOURNAL -> {
                if (!glossaryShown && g.glossaryTab == GameScreen.GlossaryTab.KANJI) {
                    capture("glossarium");
                    glossaryShown = true;
                }
                key(Input.Keys.ESCAPE);
            }
            case PRACTICE -> {
                if (!practiceShown) { capture("practice"); practiceShown = true; }
                key(Input.Keys.ESCAPE);
            }
            case CINEMATIC -> {
                require(g.ending && battles == 40, "ending before all 40 battles");
                require(g.saveStore.inspect(0).progress().endingPending(), "missing ending checkpoint");
                capture("ending");
                key(Input.Keys.S);
                require(g.mode == GameScreen.Mode.TITLE && g.saveStore.inspect(0).progress().completed(), "ending did not save");
                Gdx.app.log("SMOKE", "PASS: opening, tutorial, story, display modes, 40 encounters, " + questions + " accepted quiz answers, checkpoint reload and ending");
                Gdx.app.exit();
            }
            default -> throw new IllegalStateException("SMOKE: unexpected " + g.mode);
        }
    }

    private void play(GameScreen g) {
        if (g.tutorial) {
            switch (g.run.phase) {
                case WALK -> g.run.x = CampaignRun.ENCOUNTER_X;
                case LESSON -> {
                    if (g.revealed()) return;
                    require(g.hint && g.run.hp == CampaignRun.MAX_HP, "tutorial guide and safety");
                    capture("tutorial");
                    key(Input.Keys.ENTER);
                    require(!g.revealed() && g.run.hp == CampaignRun.MAX_HP, "failed tutorial attempt dealt damage");
                    draw(g, "水");
                    key(Input.Keys.ENTER);
                    require(g.revealed(), "tutorial rejected Water");
                }
                case WON -> {
                    key(Input.Keys.ENTER);
                    require(!g.tutorial && g.mode == GameScreen.Mode.STORY && g.story == StoryBeat.ELEMENTS_START
                        && g.saveStore.inspect(0).progress().encounter() == 0, "Stage 1 checkpoint");
                }
                default -> throw new IllegalStateException("Unexpected tutorial phase");
            }
            return;
        }
        switch (g.run.phase) {
            case WALK -> {
                if (battles == 0 && !pauseShown) {
                    key(Input.Keys.ESCAPE);
                    return;
                }
                if (battles == 0 && !glossaryShown) {
                    key(Input.Keys.Q);
                    return;
                }
                if (battles == 0 && !practiceChecked) {
                    key(Input.Keys.Q);
                    require(g.glossarySize() == 39, "Kanji glossary coverage");
                    key(Input.Keys.NUM_2);
                    key(Input.Keys.LEFT);
                    require(g.glossarySize() == 39 && g.journalPage == 38, "Hiragana glossary navigation");
                    key(Input.Keys.NUM_3);
                    key(Input.Keys.LEFT);
                    require(g.glossarySize() == g.bossQuestionCount() + g.content.katakana.size()
                        && g.journalPage == g.glossarySize() - 1, "Katakana glossary coverage");
                    key(Input.Keys.NUM_1);
                    key(Input.Keys.Q);
                    require(g.mode == GameScreen.Mode.PRACTICE, "safe practice route");
                    draw(g, "水");
                    key(Input.Keys.ENTER);
                    require(g.learning.containsKey("水") && g.run.phase == Phase.WALK, "practice changed the campaign state");
                    practiceChecked = true;
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
                if (battles == 10 || battles == 20 || battles == 30 || battles == 40) capture("stage-" + g.run.stage);
                key(Input.Keys.ENTER);
            }
            case LOST -> throw new IllegalStateException("SMOKE: player defeated");
        }
    }



    private void draw(GameScreen g, String glyph) {
        var input = Gdx.input.getInputProcessor();
        for (var stroke : g.templates.get(glyph)) {
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
        require(g.drawing.count() == g.templates.get(glyph).size(), "mouse strokes lost for U+"
            + Integer.toHexString(glyph.codePointAt(0)) + ": " + g.drawing.count() + "/"
            + g.templates.get(glyph).size() + " in " + g.mode);
    }

    private void key(int key) { Gdx.input.getInputProcessor().keyDown(key); }

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
