package com.yogiiefe.inkslayer.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.math.Vector2;
import com.yogiiefe.inkslayer.audio.GameAudio.Channel;
import com.yogiiefe.inkslayer.world.CampaignContent;
import com.yogiiefe.inkslayer.world.CampaignRun;
import com.yogiiefe.inkslayer.world.CampaignRun.Phase;

/** Opt-in packaged desktop probe. Generates mouse input from the reference stroke paths. */
final class DesktopSmoke {
    private int frames, battles, counters, questions;
    private boolean invalidChecked, reloaded, practiceChecked;
    private boolean resizing;

    void frame(GameScreen g) {
        if (resizing) return;
        frames++;
        require(frames < 2400, "campaign timed out");
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
            require(g.mode == GameScreen.Mode.GAME && g.saveStore.inspect(0).loadable(), "opening checkpoint");
            return;
        }

        switch (g.mode) {
            case GAME -> play(g);
            case CINEMATIC -> {
                require(g.ending && battles == 40, "ending before all 40 battles");
                require(g.saveStore.inspect(0).progress().endingPending(), "missing ending checkpoint");
                capture("ending");
                key(Input.Keys.S);
                require(g.mode == GameScreen.Mode.TITLE && g.saveStore.inspect(0).progress().completed(), "ending did not save");
                Gdx.app.log("SMOKE", "PASS: New, opening, settings, all 40 battles, " + counters
                    + " defensive counters, " + questions + " lesson answers, checkpoint reload, ending and resize");
                Gdx.app.exit();
            }
            default -> throw new IllegalStateException("SMOKE: unexpected " + g.mode);
        }
    }

    private void play(GameScreen g) {
        switch (g.run.phase) {
            case WALK -> {
                if (battles == 0 && !practiceChecked) {
                    key(Input.Keys.Q);
                    key(Input.Keys.Q);
                    require(g.mode == GameScreen.Mode.PRACTICE, "safe practice route");
                    draw(g, "水");
                    key(Input.Keys.ENTER);
                    require(g.learning.containsKey("水") && g.run.phase == Phase.WALK, "practice changed the campaign state");
                    key(Input.Keys.ESCAPE);
                    key(Input.Keys.ESCAPE);
                    practiceChecked = true;
                }
                if (battles == 2 && !reloaded) {
                    key(Input.Keys.ESCAPE);
                    click(g, 640, 209);
                    require(g.mode == GameScreen.Mode.TITLE, "save and return");
                    key(Input.Keys.DOWN);
                    key(Input.Keys.ENTER);
                    require(g.mode == GameScreen.Mode.SLOTS, "Save menu");
                    capture("saves");
                    key(Input.Keys.ENTER);
                    require(g.mode == GameScreen.Mode.GAME && g.run.encounter == 2 && g.learning.containsKey("水"), "checkpoint load");
                    reloaded = true;
                    resizing = true;
                    try { Gdx.graphics.setWindowedMode(960, 540); }
                    finally { resizing = false; }
                    require(Gdx.graphics.getWidth() == 960 && Gdx.graphics.getHeight() == 540, "resize");
                }
                g.run.x = CampaignRun.ENCOUNTER_X;
            }
            case LESSON -> {
                if (g.exercise == GameScreen.Exercise.INTRO || g.exercise == GameScreen.Exercise.KANA_INTRO) {
                    key(Input.Keys.ENTER);
                } else if (g.canDraw()) {
                    draw(g, g.glyph);
                    key(Input.Keys.ENTER);
                    require(g.drawing.count() == 0, "lesson glyph rejected: " + g.glyph + " / " + g.feedback);
                    questions++;
                } else {
                    String answer = switch (g.exercise) {
                        case READING -> g.lesson().reading();
                        case MEANING -> g.lesson().meaning();
                        case KATAKANA -> g.kanaWord;
                        default -> throw new IllegalStateException("Missing exercise");
                    };
                    int index = g.answers.indexOf(answer);
                    require(index >= 0, "answer missing");
                    click(g, 435 + index * 194, 105);
                    questions++;
                }
            }
            case ATTACK -> {
                if (!invalidChecked) {
                    int hp = g.run.monsterHp;
                    key(Input.Keys.ENTER);
                    require(g.run.phase == Phase.ATTACK && g.run.monsterHp == hp, "empty drawing spent an attack");
                    invalidChecked = true;
                }
                draw(g, counter(g.run.enemy().element()));
                if (battles == 0 && !reloaded && g.run.monsterHp == g.run.enemy().health()) capture("battle");
                key(Input.Keys.ENTER);
                require(g.drawing.count() == 0, "attack glyph rejected: " + g.feedback);
            }
            case DEFEND -> {
                draw(g, counter(g.run.incoming()));
                if (counters == 0) capture("counter");
                int health = g.run.hp;
                key(Input.Keys.ENTER);
                require(g.run.hp == health && g.drawing.count() == 0, "counter failed: " + g.feedback);
                counters++;
            }
            case WON -> {
                battles++;
                if (battles == 10 || battles == 20 || battles == 30 || battles == 40) capture("stage-" + g.run.stage);
                key(Input.Keys.ENTER);
            }
            case LOST -> throw new IllegalStateException("SMOKE: player defeated");
        }
    }

    private String counter(String target) {
        return CampaignContent.GLYPHS.get(CampaignContent.COUNTERS.get(target));
    }

    private void draw(GameScreen g, String glyph) {
        var input = Gdx.input.getInputProcessor();
        for (var stroke : g.templates.get(glyph)) {
            for (int index = 0; index < stroke.size(); index++) {
                var point = stroke.get(index);
                var p = screen(g, GameScreen.GUIDE_X + (float) point.x() * GameScreen.GUIDE_SIZE / 109,
                    GameScreen.GUIDE_Y + GameScreen.GUIDE_SIZE - (float) point.y() * GameScreen.GUIDE_SIZE / 109);
                int x = Math.round(p.x), y = Math.round(p.y);
                if (index == 0) input.touchDown(x, y, 0, Input.Buttons.LEFT);
                else if (index + 1 == stroke.size()) input.touchUp(x, y, 0, Input.Buttons.LEFT);
                else input.touchDragged(x, y, 0);
            }
        }
        require(g.drawing.count() == g.templates.get(glyph).size(), "mouse strokes lost for " + glyph);
    }

    private void key(int key) { Gdx.input.getInputProcessor().keyDown(key); }

    private void click(GameScreen g, float x, float y) {
        Vector2 p = screen(g, x, y);
        var input = Gdx.input.getInputProcessor();
        input.touchDown(Math.round(p.x), Math.round(p.y), 0, Input.Buttons.LEFT);
        input.touchUp(Math.round(p.x), Math.round(p.y), 0, Input.Buttons.LEFT);
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
