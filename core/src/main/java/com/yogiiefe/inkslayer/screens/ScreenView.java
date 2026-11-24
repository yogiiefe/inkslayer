package com.yogiiefe.inkslayer.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.yogiiefe.inkslayer.recognition.InkPoint;
import com.yogiiefe.inkslayer.screens.KeyBindings.Action;
import com.yogiiefe.inkslayer.world.CampaignRun;
import com.yogiiefe.inkslayer.world.CampaignRun.Phase;
import java.util.List;

import static com.yogiiefe.inkslayer.screens.GameScreen.*;
import static com.yogiiefe.inkslayer.screens.PixelArtViewport.*;

/** Draws the original pixel-art arena and the campaign screens. */
final class ScreenView implements Disposable {
    private static final Color INK = Color.valueOf("121212"), PAPER = Color.valueOf("ececec");
    private static final Color BACKDROP = new Color(0.035f, 0.035f, 0.035f, 1);
    private static final Color MUTED = Color.valueOf("b1b1b1"), ACCENT = Color.valueOf("cccccc");
    private static final Color PINK = Color.valueOf("909090"), PANEL = Color.valueOf("1b1b1b"), LINE = Color.valueOf("525252");
    private static final Color[] STAGE = colors("131313", "1d1d1d", "191919", "171717");

    private final GameScreen g;
    private final SpriteBatch batch = new SpriteBatch();
    private final FrameBuffer pixelArt = new FrameBuffer(Pixmap.Format.RGBA8888, ART_WIDTH, ART_HEIGHT, false);
    private final ActorArt art = new ActorArt();
    private final Texture pixel;
    private final Texture cinematicSkip = new Texture(Gdx.files.internal("SkipAnim.png"));
    private final BitmapFont small, body, large, quizFont;
    private final BitmapFont cinematicFont;
    private final TitleMenu titleMenu = new TitleMenu();
    private final MenuView menus;
    private final GlyphLayout layout = new GlyphLayout();

    ScreenView(GameScreen screen) {
        g = screen;
        pixelArt.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        cinematicSkip.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        var pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        pixel = new Texture(pixmap);
        pixmap.dispose();
        var generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/DotGothic16-Regular.ttf"));
        try {
            var chars = new StringBuilder(FreeTypeFontGenerator.DEFAULT_CHARS).append("←→×");
            g.quizzes.forEach(quiz -> chars.append(quiz.japanese()).append(quiz.kanji())
                .append(quiz.reading()).append(quiz.katakana()));
            chars.append("カユソ");
            g.content.katakana.forEach(word -> chars.append(word.text()));
            String characters = chars.toString().codePoints().distinct()
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
            small = font(generator, 16, characters);
            body = font(generator, 20, characters);
            large = font(generator, 48, characters);
            quizFont = font(generator, 32, characters);
            cinematicFont = cinematicFont(generator, characters);
        } finally {
            generator.dispose();
        }
        menus = new MenuView(g, batch, pixel);
    }

    private static Color[] colors(String... hex) {
        return java.util.Arrays.stream(hex).map(Color::valueOf).toArray(Color[]::new);
    }

    private BitmapFont font(FreeTypeFontGenerator generator, int size, String chars) {
        var p = new FreeTypeFontGenerator.FreeTypeFontParameter();
        p.size = size;
        p.mono = size >= 32;
        p.characters = chars;
        p.minFilter = Texture.TextureFilter.Nearest;
        p.magFilter = Texture.TextureFilter.Nearest;
        return generator.generateFont(p);
    }

    private BitmapFont cinematicFont(FreeTypeFontGenerator generator, String characters) {
        var font = new BitmapFont(Gdx.files.internal("fonts/cinematic.fnt"));
        // Letters come directly from the supplied alphabet; supplement punctuation and Japanese glyphs.
        StringBuilder supplement = new StringBuilder();
        characters.chars().filter(c -> !font.getData().hasGlyph((char) c))
            .forEach(c -> supplement.append((char) c));
        var style = new FreeTypeFontGenerator.FreeTypeFontParameter();
        style.size = 7;
        style.mono = true;
        style.characters = supplement.toString();
        style.color = Color.valueOf("c9d1d1");
        var fallback = generator.generateFont(style);
        int pageOffset = font.getRegions().size;
        font.getRegions().addAll(fallback.getRegions());
        for (char character : supplement.toString().toCharArray()) {
            var glyph = fallback.getData().getGlyph(character);
            if (glyph == null) continue;
            glyph.page += pageOffset;
            glyph.yoffset += Math.round(fallback.getData().ascent);
            font.getData().setGlyph(character, glyph);
        }
        font.getData().missingGlyph = font.getData().getGlyph('?');
        // The cinematic font owns both texture pages after the transfer.
        fallback.setOwnsTexture(false);
        fallback.dispose();
        font.getRegions().forEach(region -> region.getTexture().setFilter(
            Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest));
        font.getData().setScale(ART_PIXEL);
        return font;
    }

    void render() {
        batch.setProjectionMatrix(g.viewport.getCamera().combined);
        boolean hasArt = g.mode == Mode.TITLE || g.mode == Mode.GAME
            || (g.mode == Mode.CINEMATIC && g.ending);
        if (hasArt) renderPixelArt();

        g.viewport.apply();
        batch.begin();
        if (hasArt) {
            batch.setColor(Color.WHITE);
            batch.disableBlending();
            // OpenGL framebuffer textures have their origin at the bottom.
            batch.draw(pixelArt.getColorBufferTexture(), 0, 0, UI_WIDTH, UI_HEIGHT,
                0, 0, ART_WIDTH, ART_HEIGHT, false, true);
            batch.enableBlending();
        } else {
            box(0, 0, UI_WIDTH, UI_HEIGHT, BACKDROP);
        }
        switch (g.mode) {
            case TITLE -> {}
            case CHAPTER_SELECT, SLOTS, SETTINGS, KEYBINDS, PAUSE -> menus.draw();
            case CINEMATIC -> cinematic();
            case GAME -> arenaUi();
            case JOURNAL -> journal();
            case PRACTICE -> practice();
        }
        batch.end();
    }

    private void renderPixelArt() {
        pixelArt.begin();
        if (g.mode == Mode.TITLE) ScreenUtils.clear(0, 0, 0, 1);
        batch.begin();
        if (g.mode == Mode.TITLE) titleMenu.draw(batch, g.titleSelection, g.titleHover, g.elapsed - g.titleHoverAt, g.elapsed);
        else if (g.mode == Mode.CINEMATIC) endingArt();
        else arenaArt();
        batch.end();
        pixelArt.end();
    }

    private void arenaArt() {
        var run = g.run;
        box(0, 0, UI_WIDTH, UI_HEIGHT, STAGE[g.tutorial ? 0 : run.stage]);
        box(0, 0, UI_WIDTH, CANVAS_TOP, Color.WHITE);
        box(0, CANVAS_TOP - ART_PIXEL, UI_WIDTH, ART_PIXEL, LINE);
        if (g.tutorial) {
            tutorialArt();
            return;
        }
        if (run.phase == Phase.WALK) {
            float scroll = walkScroll();
            districtProp(districtLeft(scroll));
            for (int i = 0; i < 24; i++) {
                float x = i * 64 - scroll % 64;
                box(x, CANVAS_TOP - 12, 25, ART_PIXEL, LINE);
            }
            art.player(batch, (float) run.x * 2 - scroll + 60, CANVAS_TOP, run.facing, walkingPose());
            drawEnemy(2 * ((float) CampaignRun.ENCOUNTER_X + 145) - scroll);
            return;
        }

        art.player(batch, 251, CANVAS_TOP, 1, 0);
        if (run.phase != Phase.WON) drawEnemy(846);
        if (g.effectTime > 0) spellEffect();
    }

    private void arenaUi() {
        if (g.tutorial) {
            tutorialUi();
            return;
        }
        var run = g.run;
        if (run.phase == Phase.WALK) {
            districtLabel(districtLeft(walkScroll()));
            center(quizFont, g.content.stages.get(run.stage).name(), 640, 649, PAPER);
            center(small, "Encounter " + (run.encounter + 1) + " / 10", 640, 598, MUTED);
            center(small, movementKeys() + " to walk", 640, 565, PAPER);
            if (!g.saveMessage.isEmpty()) center(small, g.saveMessage, 640, 540, PAPER);
            return;
        }

        if (run.phase == Phase.WON || run.phase == Phase.LOST) outcome();
        else drawArea();
    }

    private float walkScroll() {
        return (float) Math.max(0, g.run.x - 215) * 2;
    }

    private int districtLeft(float scroll) {
        return Math.round((655 - scroll * 0.55f) / ART_PIXEL) * ART_PIXEL;
    }

    private String movementKeys() {
        return g.bindings.label(Action.LEFT) + "   /   " + g.bindings.label(Action.RIGHT);
    }

    private void districtProp(int left) {
        box(left, 260, 10, 144, LINE);
        box(left - 52, 355, 230, 84, LINE);
        box(left - 48, 359, 222, 76, PANEL);
    }

    private void districtLabel(int left) {
        String label = switch (g.run.stage) {
            case 0 -> "INSPECTION OPEN";
            case 1 -> "RESIDENCE SEALED";
            case 2 -> "SHELTER THIS WAY >";
            default -> "RATION QUEUE";
        };
        center(small, label, left + 62, 410, PAPER);
    }

    private void tutorialArt() {
        door(86, false);
        door(478, true);
        float centerX = (float) g.run.x * 1.25f + 200;
        art.player(batch, centerX, CANVAS_TOP, g.run.facing, g.run.phase == Phase.WALK ? walkingPose() : 0);
        box(1045, 260, 112, 218, LINE);
        box(1056, 260, 90, 206, PANEL);
        for (int i = 0; i < 5; i++) box(1058, 276 + 38 * i, 86, 4, MUTED);
        if (g.revealed()) box(1063, 261, 76, 116, INK);
    }

    private void tutorialUi() {
        if (g.run.phase == Phase.WALK) {
            center(quizFont, "The Unchecked Door", 640, 626, PAPER);
            center(small, "Your door is still unchecked. Walk right to the water gate.", 640, 578, PAPER);
            center(small, g.bindings.label(Action.RIGHT), 640, 551, PAPER);
        } else if (g.run.phase == Phase.WON) {
            panel(380, 278, 520, 238);
            center(large, "Gate open", 640, 470, PAPER);
            center(body, "Your ink moved the water. The road is clear.", 640, 412, PAPER);
            button("Continue to Stage 1", 475, 316, 330, 54, true);
            if (!g.saveMessage.isEmpty()) center(small, g.saveMessage, 640, 303, PAPER);
        } else {
            drawArea();
            center(small, "The note by the gate reads 水 (みず) / water.", 640, 541, PAPER);
        }
    }

    private void door(int x, boolean checked) {
        box(x, 260, 134, 216, LINE);
        box(x + 8, 260, 118, 208, PANEL);
        box(x + 38, 388, 56, 64, MUTED);
        box(x + 45, 397, 42, 45, INK);
        if (checked) {
            box(x + 53, 411, 26, 18, PAPER);
            box(x + 58, 416, 16, 8, LINE);
        }
        box(x + 104, 329, 8, 8, MUTED);
    }

    private double lastWalkX = 110, walkDistance, lastWalkAt = -1;

    private int walkingPose() {
        if (g.run.x != lastWalkX) {
            walkDistance += Math.abs(g.run.x - lastWalkX);
            lastWalkX = g.run.x;
            lastWalkAt = g.elapsed;
        }
        return g.elapsed - lastWalkAt < 0.09 ? 1 + ((int) (walkDistance / 17) % 2) : 0;
    }

    private void drawEnemy(float x) {
        var run = g.run;
        boolean floats = run.stage == 2;
        int lift = floats ? 4 * (int) Math.round(1 + Math.sin(g.elapsed * 3)) : 0;
        if (floats) box(Math.round(x) + 52, CANVAS_TOP, 132, 4, LINE);
        art.enemy(batch, x, CANVAS_TOP - 4, run.stage, run.encounter, lift);
    }

    private void spellEffect() {
        float travel = (float) (1 - g.effectTime / 0.3);
        for (int i = 0; i < 6; i++) {
            float x = 288 + travel * 640 - i * 15;
            float y = 350 + 36 * (float) Math.sin(travel * Math.PI) + (i % 3 - 1) * 10;
            box(x, y, 22 - i * 2, 10, PAPER);
        }
    }

    private void drawArea() {
        boolean practice = g.mode == Mode.PRACTICE;
        if (!practice) {
            var quiz = g.quiz();
            box(590, 573, 614, 119, LINE);
            box(594, 577, 606, 111, PANEL);
            box(973, 561, 32, 12, LINE);
            box(981, 550, 24, 11, LINE);
            box(989, 543, 16, 7, LINE);
            String speaker = g.tutorial ? "Note at the water gate" : g.run.enemy().name();
            text(small, speaker + (g.run.isBoss() ? "  " + g.bossQuestionNumber() + "/" + g.bossQuestionCount() : ""),
                616, 678, MUTED);
            write(body, quiz.english(), 616, 650, 562, Align.left, PAPER);
            String sentence = g.revealed() ? quiz.completed()
                : g.run.isBoss() ? quiz.japanese() + "  (" + quiz.kanji() + ")" : quiz.japanese();
            write(quizFont, sentence, 616, 623, 562, Align.left, PAPER);
        }

        if (!practice && g.run.isBoss()) {
            panel(472, 156, 336, 76);
            center(large, g.kanaInput.isEmpty() ? "[     ]" : g.kanaInput, 640, 218, PAPER);
            var tiles = g.tiles();
            for (int i = 0; i < tiles.size(); i++)
                button(tiles.get(i), tileX(i, tiles.size()), 80, 76, 58, i == g.tileSelection, quizFont);
            center(small, g.feedback.isEmpty() ? "Click tiles or use " + movementKeys() + " + Space   /   "
                + g.bindings.label(Action.SUBMIT) + " submit   /   Backspace undo" : g.feedback, 640, 44, INK);
        } else {
            if (g.guideGlyph != null) previewGuide();
            for (var path : g.drawing.snapshot()) brush(path);
            brush(g.drawing.active());
            String prompt = g.revealed() && !practice ? "Correct!" : g.feedback;
            if (!prompt.isEmpty()) {
                box(260, 272, 760, 38, INK);
                center(small, prompt, 640, 298, PAPER);
            }
        }
    }

    private void previewGuide() {
        var strokes = g.templates.get(g.guideGlyph);
        int active = (int) g.guideProgress;
        for (int index = 0; index <= active; index++) {
            var path = strokes.get(index);
            int count = index == active ? 1 + (int) ((g.guideProgress - active) * path.size()) : path.size();
            stroke(path.subList(0, count), GUIDE_X, GUIDE_Y + GUIDE_SIZE, GUIDE_SIZE / 109f, LINE, 3);
        }
    }

    private void brush(List<InkPoint> path) {
        if (path.isEmpty()) return;
        var first = path.getFirst();
        stamp(first.x(), first.y(), 18);
        for (int index = 1; index < path.size(); index++) {
            var a = path.get(index - 1);
            var b = path.get(index);
            double distance = a.distance(b);
            int steps = Math.max(1, (int) Math.ceil(distance / 4));
            int width = Math.clamp((int) (23 - distance * 0.36), 12, 23);
            for (int step = 1; step <= steps; step++) {
                double t = (double) step / steps;
                stamp(a.x() + (b.x() - a.x()) * t, a.y() + (b.y() - a.y()) * t, width);
            }
        }
    }

    private void stamp(double x, double y, int width) {
        int left = (int) Math.round((x - width / 2.0) / 3) * 3;
        int bottom = (int) Math.round((CANVAS_TOP - y - width / 2.0) / 3) * 3;
        int right = Math.min(CANVAS_WIDTH, left + width);
        int top = Math.min(CANVAS_TOP, bottom + width);
        left = Math.max(0, left);
        bottom = Math.max(0, bottom);
        if (right > left && top > bottom) box(left, bottom, right - left, top - bottom, INK);
    }

    private void outcome() {
        boolean won = g.run.phase == Phase.WON;
        panel(380, 278, 520, 238);
        center(large, won ? "Victory" : "Defeat", 640, 470, won ? ACCENT : PINK);
        String result = won ? g.run.enemy().name() + " defeated" : "Retry from this encounter's checkpoint";
        if (won && g.run.stage == 0 && g.run.encounter == 0)
            result = "Under the mask is a face from Kotoba's list of victims.";
        write(body, result, 406, 412, 468, Align.center, PAPER);
        button(won ? "Continue" : "Retry", 475, 316, 330, 54, true);
        if (!g.saveMessage.isEmpty()) center(small, g.saveMessage, 640, 303, PINK);
    }

    private void journal() {
        panel(275, 76, 730, 569);
        center(large, "Glossarium", 640, 612, PAPER);
        String[] labels = {"1  Kanji", "2  Hiragana", "3  Katakana"};
        for (int i = 0; i < labels.length; i++)
            button(labels[i], 324 + i * 214, 505, 204, 42, g.glossaryTab.ordinal() == i);
        center(small, (g.journalPage + 1) + " / " + g.glossarySize(), 640, 484, MUTED);

        var entries = g.journalEntries();
        if (g.journalPage >= entries.size()) {
            var word = g.content.katakana.get(g.journalPage - entries.size());
            center(large, word.text(), 640, 425, PAPER);
            center(body, word.meaning() + "  /  " + word.romaji(), 640, 352, PAPER);
            write(body, word.note(), 325, 295, 630, Align.center, MUTED);
        } else {
            var quiz = entries.get(g.journalPage);
            if (g.glossaryTab == GameScreen.GlossaryTab.KANJI) {
                template(quiz.kanji(), 580, 353, 120);
                center(body, quiz.kanji() + "  /  " + quiz.reading() + "  /  "
                    + g.templates.get(quiz.kanji()).size() + " strokes", 640, 342, PAPER);
                if (g.run.phase == Phase.WALK) button("Practice (Q)", 530, 270, 220, 42, true);
            } else {
                boolean bossReading = g.glossaryTab == GameScreen.GlossaryTab.KATAKANA;
                center(small, bossReading ? "MR. KOTOBA / READING IN KATAKANA" : "READING IN THIS SENTENCE", 640, 443, MUTED);
                center(large, bossReading ? quiz.katakana() : quiz.reading(), 640, 408, PAPER);
                center(body, "Kanji: " + quiz.kanji() + (bossReading ? "  /  Hiragana: " + quiz.reading() : ""), 640, 336, PAPER);
            }
            write(quizFont, quiz.completed(), 325, 250, 630, Align.center, PAPER);
            write(body, quiz.english(), 325, 198, 630, Align.center, MUTED);
        }
        button("Previous", 312, 115, 180, 42, false);
        button("Next", 788, 115, 180, 42, false);
        button("Back", 530, 115, 220, 42, true);
    }

    private void practice() {
        var lesson = g.quizzes.stream().filter(quiz -> quiz.kanji().equals(g.practiceGlyph)).findFirst().orElseThrow();
        box(0, 0, 1280, 720, STAGE[g.run.stage]);
        center(large, lesson.kanji(), 620, 590, ACCENT);
        center(body, lesson.reading() + "  /  " + lesson.english(), 620, 486, PAPER);
        box(0, 0, 1280, CANVAS_TOP, Color.WHITE);
        drawArea();
    }

    private void cinematic() {
        if (!g.ending) {
            box(0, 0, UI_WIDTH, UI_HEIGHT, Color.BLACK);
        } else {
            box(0, 0, UI_WIDTH, 220, INK);
        }
        String text = g.cinematicText();
        layout.setText(cinematicFont, text, Color.WHITE, 260 * ART_PIXEL, Align.center, true);
        for (var run : layout.runs) run.x = Math.round(run.x / ART_PIXEL) * ART_PIXEL;
        float textTop = (ART_HEIGHT - 77) * ART_PIXEL;
        if (g.ending) box(24 * ART_PIXEL, textTop - layout.height - 3 * ART_PIXEL,
            272 * ART_PIXEL, layout.height + 6 * ART_PIXEL, Color.BLACK);
        var cache = cinematicFont.getCache();
        // The sample line starts at row 77 of the supplied 320x180 layout.
        cache.setText(layout, 30 * ART_PIXEL, textTop);
        cache.setColors(Color.CLEAR);
        int visible = Math.min(layout.glyphCount, g.cinematicVisibleCharacters());
        if (visible > 0) cache.setColors(Color.WHITE, 0, visible);
        cache.draw(batch);
        boolean skipHovered = g.skipHoverAt >= 0;
        double hoverAge = g.elapsed - g.skipHoverAt;
        float shade = skipHovered ? TitleMenu.hoverShade(hoverAge) : 1f;
        int arrowOffset = skipHovered ? TitleMenu.hoverArrowOffset(hoverAge) : 0;
        batch.setColor(shade, shade, shade, 1f);
        TitleMenu.drawRegion(batch, cinematicSkip, 298, 5, 18, 5, 298, 5);
        batch.setColor(Color.WHITE);
        // The sheet supplies the two endpoints; the shared menu motion adds the middle position.
        int frame = arrowOffset == 2 ? 1 : 0;
        TitleMenu.drawRegion(batch, cinematicSkip, 287 + 2 * frame, 4 + 10 * frame, 7, 7,
            287 + arrowOffset, 4);
        if (!g.saveMessage.isEmpty()) center(small, g.saveMessage, 640, 34, PINK);
    }

    private void endingArt() {
        box(0, 0, UI_WIDTH, UI_HEIGHT, STAGE[3]);
        box(0, 220, UI_WIDTH, ART_PIXEL, LINE);
        float drift = (float) Math.min(90, (g.elapsed - g.panelStartedAt) * 10);
        for (int i = 0; i < 7; i++) {
            int height = 70 + (i * 41 + g.panel * 31) % 210;
            float x = i * 212 - drift;
            box(x, 221, 164, height, PANEL);
            box(x + 18, 221 + height - 24, 128, 6, LINE);
        }
        art.player(batch, 284 + 32 * g.panel + drift, 221, 1, 0);
        if (g.panel >= 2 && g.panel < 4) {
            box(837, 221, 175, 242, LINE);
            box(860, 452, 134, 43, LINE);
            box(832, 491, 188, 16, PINK);
            box(873, 474, 90, 6, PAPER);
        }
    }

    private void template(String id, float x, float y, float size) {
        var strokes = g.templates.get(id);
        float scale = size / 109;
        int active = (int) (g.elapsed * 1.1) % strokes.size();
        for (int index = 0; index < strokes.size(); index++) {
            var path = strokes.get(index);
            stroke(path, x, y + size, scale, LINE, 3);
            var start = path.getFirst();
            text(small, Integer.toString(index + 1), x + (float) start.x() * scale - 9,
                y + size - (float) start.y() * scale + 9, MUTED);
            if (index == active) {
                int point = Math.min(path.size() - 1, (int) ((g.elapsed * 1.1 % 1) * path.size()));
                var p = path.get(point);
                box(x + (float) p.x() * scale - 2, y + size - (float) p.y() * scale - 2, 5, 5, ACCENT);
            }
        }
    }

    private void stroke(List<InkPoint> path, float x, float top, float scale, Color color, float width) {
        for (int i = 1; i < path.size(); i++) {
            var start = path.get(i - 1);
            var end = path.get(i);
            float dx = (float) (end.x() - start.x()) * scale, dy = (float) (start.y() - end.y()) * scale;
            batch.setColor(color);
            batch.draw(pixel, x + (float) start.x() * scale, top - (float) start.y() * scale - width / 2, 0, width / 2,
                (float) Math.hypot(dx, dy), width, 1, 1, (float) Math.toDegrees(Math.atan2(dy, dx)), 0, 0, 1, 1, false, false);
        }
    }

    private void panel(float x, float y, float w, float h) {
        box(x, y, w, h, LINE);
        box(x + 2, y + 2, w - 4, h - 4, INK);
    }

    private void button(String label, float x, float y, float w, float h, boolean primary) {
        button(label, x, y, w, h, primary, body);
    }

    private void button(String label, float x, float y, float w, float h, boolean primary, BitmapFont font) {
        boolean hover = hit(g.mouseX, g.mouseY, x, y, w, h);
        box(x, y, w, h, primary ? ACCENT : hover ? LINE : PANEL);
        box(x, y, w, 1, LINE);
        write(font, label, x + 8, y + h / 2 + font.getCapHeight() / 2, w - 16, Align.center, primary ? INK : PAPER);
    }

    private void box(float x, float y, float w, float h, Color color) {
        batch.setColor(color);
        batch.draw(pixel, x, y, w, h);
    }

    private void write(BitmapFont font, String value, float x, float y, float w, int align, Color color) {
        font.setColor(color);
        font.draw(batch, value, x, y, w, align, w > 0);
    }

    private void text(BitmapFont font, String value, float x, float y, Color color) {
        write(font, value, x, y, 0, Align.left, color);
    }

    private void center(BitmapFont font, String value, float x, float y, Color color) {
        layout.setText(font, value);
        text(font, value, Math.round(x - layout.width / 2), y, color);
    }

    private void right(BitmapFont font, String value, float x, float y, Color color) {
        write(font, value, x - 260, y, 260, Align.right, color);
    }

    @Override public void dispose() {

        large.dispose();
        quizFont.dispose();
        body.dispose();
        small.dispose();
        cinematicFont.dispose();
        art.dispose();
        titleMenu.dispose();
        menus.dispose();
        pixelArt.dispose();
        pixel.dispose();
        cinematicSkip.dispose();
        batch.dispose();
    }
}
