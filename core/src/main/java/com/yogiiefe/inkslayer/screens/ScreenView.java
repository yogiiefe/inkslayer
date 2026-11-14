package com.yogiiefe.inkslayer.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.yogiiefe.inkslayer.audio.GameAudio.Channel;
import com.yogiiefe.inkslayer.recognition.InkPoint;
import com.yogiiefe.inkslayer.world.CampaignContent;
import com.yogiiefe.inkslayer.world.CampaignRun;
import com.yogiiefe.inkslayer.world.CampaignRun.Phase;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.yogiiefe.inkslayer.screens.GameScreen.*;

/** Draws the original pixel-art arena and the campaign screens. */
final class ScreenView implements Disposable {
    private static final Color INK = Color.valueOf("121212"), PAPER = Color.valueOf("ececec");
    private static final Color MUTED = Color.valueOf("b1b1b1"), ACCENT = Color.valueOf("cccccc");
    private static final Color PINK = Color.valueOf("909090"), PANEL = Color.valueOf("1b1b1b"), LINE = Color.valueOf("525252");
    private static final Color[] STAGE = colors("131313", "1d1d1d", "191919", "171717");
    private static final Color[] ELEMENT = colors("adadad", "8e8e8e", "adadad", "a8a8a8", "afafaf");
    private static final Color[] SHADOW = colors("5a5a5a", "525252", "5f5f5f", "5c5c5c", "676767");
    private static final DateTimeFormatter SAVE_DATE = DateTimeFormatter.ofPattern("dd MMM, HH:mm").withZone(ZoneId.systemDefault());

    private final GameScreen g;
    private final SpriteBatch batch = new SpriteBatch();
    private final Matrix4 transformed = new Matrix4(), identity = new Matrix4();
    private final Texture pixel;
    private final BitmapFont small, body, large, quizFont;
    private final TitleMenu titleMenu;

    ScreenView(GameScreen screen) {
        g = screen;
        var pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        pixel = new Texture(pixmap);
        pixmap.dispose();
        titleMenu = new TitleMenu(batch, pixel);
        var generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/DotGothic16-Regular.ttf"));
        try {
            var chars = new StringBuilder(FreeTypeFontGenerator.DEFAULT_CHARS).append("←→×");
            g.quizzes.forEach(quiz -> chars.append(quiz.japanese()).append(quiz.kanji())
                .append(quiz.reading()).append(quiz.katakana()));
            chars.append("カユソ");            g.content.katakana.forEach(word -> chars.append(word.text()));
            String characters = chars.toString().codePoints().distinct()
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
            small = font(generator, 16, characters);
            body = font(generator, 16, characters);
            large = font(generator, 48, characters);
            quizFont = font(generator, 32, characters);
        } finally {
            generator.dispose();
        }
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

    void render() {
        batch.setProjectionMatrix(g.viewport.getCamera().combined);
        batch.begin();
        switch (g.mode) {
            case TITLE -> titleMenu.draw(body, small, g.titleSelection, g.titleHover);
            case SLOTS -> slots();
            case SETTINGS -> settings();
            case CINEMATIC -> cinematic();
            case GAME -> arena();
            case PAUSE -> pause();
            case JOURNAL -> journal();
            case PRACTICE -> practice();
        }
        batch.end();
    }

    private void arena() {
        var run = g.run;
        boolean walk = run.phase == Phase.WALK;
        int index = CampaignContent.ELEMENTS.indexOf(run.enemy().element());
        box(0, 0, 1280, 720, STAGE[run.stage]);
        box(0, 0, 1280, CANVAS_TOP, Color.WHITE);
        box(0, CANVAS_TOP - 2, 1280, 2, LINE);
        if (walk) {
            float scroll = (float) Math.max(0, run.x - 215) * 2;
            for (int i = 0; i < 24; i++) {
                float x = i * 64 - scroll % 64;
                box(x, CANVAS_TOP - 10, 25, 2, LINE);
            }
            actor((float) run.x * 2 - scroll, CANVAS_TOP, 2.6f, () -> player(run.facing));
            float enemyX = 2 * ((float) CampaignRun.ENCOUNTER_X + 145) - scroll;
            actor(enemyX, CANVAS_TOP, 4f, () -> monster(index));
            center(body, g.content.stages.get(run.stage).name(), 640, 650, PAPER);
            center(small, "Encounter " + (run.encounter + 1) + " / 10     A / D to walk     Tab for journal", 640, 615, MUTED);
            if (!g.saveMessage.isEmpty()) center(small, g.saveMessage, 640, 50, PINK);
            return;
        }

        actor(216, CANVAS_TOP, 2.6f, () -> player(1));
        if (run.phase != Phase.WON) actor(826, CANVAS_TOP, 4f, () -> monster(index));
        center(body, "PLAYER", 250, 454, PAPER);
        button("Glossarium", 24, 646, 172, 46, false);
        button("Settings", 208, 646, 132, 46, false);
        if (g.effectTime > 0) spellEffect();
        if (run.phase == Phase.WON || run.phase == Phase.LOST) outcome();
        else drawArea();
    }

    private void actor(float x, float y, float scale, Runnable paint) {
        batch.setTransformMatrix(transformed.setToTranslation(Math.round(x), y, 0).scale(scale, scale, 1));
        paint.run();
        batch.setTransformMatrix(identity);
    }

    private void player(int facing) {
        box(8, 0, 10, 16, LINE);
        box(23, 0, 10, 16, LINE);
        box(4, 13, 31, 27, Color.valueOf("888888"));
        box(4, 13, 8, 27, Color.valueOf("545454"));
        box(3, 13, 35, 5, ELEMENT[4]);
        box(10, 39, 22, 21, PAPER);
        box(4, 56, 34, 6, LINE);
        box(9, 62, 24, 5, LINE);
        box(facing > 0 ? 26 : 13, 47, 4, 3, INK);
        box(-13, 31, 18, 6, PINK);
        box(-20, 25, 14, 6, PINK);
        box(31, 15, 4, 27, ELEMENT[4]);
    }

    private void monster(int palette) {
        Color color = ELEMENT[palette], shadow = SHADOW[palette];
        int variant = g.run.encounter % 5;
        int ridge = variant * 2;
        switch (g.run.stage) {
            case 0 -> {
                box(0, 0, 61, 29, color);
                box(6, 29, 49, 18, color);
                box(13, 47, 10, 15 + ridge, color);
                box(39, 47, 9, 21 - ridge, color);
            }
            case 1 -> {
                box(3, 0, 15, 13, shadow);
                box(43, 0, 15, 13, shadow);
                box(1, 12, 59, 27, color);
                box(9, 28, 45, 25, color);
                box(9, 51, 12 + ridge, 14 - ridge, color);
                box(42 - ridge, 51, 12 + ridge, 14 - ridge, color);
                box(12, 53, 5, 4, shadow);
                box(46, 53, 5, 4, shadow);
                box(52, 22, 13, 7, color);
            }
            case 2 -> {
                box(11, 10, 41, 48, color);
                box(3, 21, 57, 28, color);
                box(20 - ridge, 58, 25 + ridge, 7 + variant, color);
                box(15, 0, 4, 7, shadow);
                box(30, 2, 5, 7, shadow);
                box(43, 0, 4, 7, shadow);
            }
            default -> {
                box(5, 0, 50, 25, shadow);
                box(12, 20, 36, 28, color);
                box(13, 44, 34, 18, color);
                box(6, 60, 47, 5, shadow);
                box(15, 65, 29, 5, shadow);
                box(50, 10, 7, 33, PINK);
            }
        }
        int eyeY = g.run.stage == 3 ? 49 : 33;
        box(15, eyeY - 2, 9, 8, INK);
        box(38, eyeY - 2, 9, 8, INK);
        box(18, eyeY + 2, 4, 3, PAPER);
        box(39, eyeY + 2, 4, 3, PAPER);
        box(25, eyeY - 11, 12, 4, shadow);
        if (g.run.isBoss()) box(24, 22, 15, 11, PINK);
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
            text(small, g.run.enemy().name() + (g.run.isBoss() ? "  " + g.bossQuestionNumber() + "/" + g.bossQuestionCount() : ""),
                616, 678, MUTED);
            write(body, quiz.english(), 616, 650, 562, Align.left, PAPER);
            String sentence = g.revealed() ? quiz.completed()
                : g.run.isBoss() ? quiz.japanese() + "  (" + quiz.kanji() + ")" : quiz.japanese();
            write(quizFont, sentence, 616, 623, 562, Align.left, PAPER);
        }

        if (!practice && g.run.isBoss()) {
            center(large, g.kanaInput.isEmpty() ? "[     ]" : g.kanaInput, 640, 223, INK);
            var tiles = g.tiles();
            for (int i = 0; i < tiles.size(); i++)
                button(tiles.get(i), 332 + i * 88, 80, 76, 58, i == g.tileSelection);
            center(small, g.feedback.isEmpty() ? "Click tiles or use arrows + Space   /   Enter submit   /   R clear" : g.feedback, 640, 44, INK);
        } else {
            String guide = g.guideGlyph();
            if (guide != null) template(guide, GUIDE_X, GUIDE_Y, GUIDE_SIZE);
            for (var path : g.drawing.snapshot()) brush(path);
            brush(g.drawing.active());
            if (g.drawing.count() == 0 && !g.drawing.isActive() && guide == null) {
                String prompt = g.revealed() ? "Correct!" : g.feedback.isEmpty()
                    ? "DRAW KANJI   /   ENTER SUBMIT   /   H GUIDE   /   R CLEAR" : g.feedback;
                center(small, prompt, 640, 37, INK);
            } else if (!g.feedback.isEmpty()) {
                box(830, 12, 438, 24, Color.WHITE);
                write(small, g.feedback, 832, 32, 426, Align.right, INK);
            }
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
        center(body, result, 640, 412, PAPER);
        button(won ? "Continue" : "Retry", 475, 316, 330, 54, true);
        if (!g.saveMessage.isEmpty()) center(small, g.saveMessage, 640, 303, PINK);
    }

    private void slots() {
        panel(292, 90, 696, 530);
        center(large, "Save Files", 640, 574, PAPER);
        for (int i = 0; i < g.slots.size(); i++) {
            var slot = g.slots.get(i);
            int y = 425 - i * 90;
            box(320, y, 640, 68, g.slotSelection == i ? LINE : PANEL);
            box(322, y + 2, 636, 64, INK);
            String detail = slot.loadable()
                ? slot.progress().completed() ? "Completed  /  Replay ending"
                : g.content.stages.get(slot.progress().stage()).name() + "  /  " + (slot.progress().encounter() + 1) + " of 10"
                : slot.occupied() ? "Unreadable save" : "Empty";
            text(body, "Slot " + (i + 1) + "     " + detail, 340, y + 57, PAPER);
            if (slot.loadable()) {
                var progress = slot.progress();
                long minutes = progress.playSeconds() / 60;
                text(small, progress.battlesWon() + "/40 battles    " + minutes + " min    "
                    + SAVE_DATE.format(Instant.ofEpochMilli(progress.savedAt())) + (slot.recovered() ? "    Backup" : ""),
                    340, y + 26, MUTED);
            }
            if (slot.loadable()) right(small, "Load", 938, y + 51, ACCENT);
        }
        center(small, g.saveMessage, 640, 201, PINK);
        button("Back", 490, 116, 300, 44, false);
    }

    private void settings() {
        panel(338, 104, 604, 513);
        center(large, "Settings", 640, 562, PAPER);
        button("Display: " + g.displayMode.label, 490, 473, 300, 44, g.setting == 3);
        String[] names = {"Master", "SFX", "BGM"};
        for (int i = 0; i < names.length; i++) {
            int y = 406 - i * 76;
            int value = g.audio.volume(Channel.values()[i]);
            text(body, names[i], 372, y + 18, g.setting == i ? ACCENT : PAPER);
            box(496, y, 320, 8, LINE);
            box(496, y, value * 3.2f, 8, ACCENT);
            box(491 + value * 3.2f, y - 5, 10, 18, PAPER);
            right(body, value + "%", 904, y + 18, PAPER);
        }
        button(g.strict ? "Stroke order: Strict" : "Stroke order: Coaching", 490, 204, 300, 40, false);
        center(small, "Use the mouse or arrow keys to change settings", 640, 183, MUTED);
        button("Back", 490, 136, 300, 42, true);
        center(small, g.settingsMessage, 640, 125, PINK);
    }

    private void pause() {
        panel(350, 157, 580, 453);
        center(large, "Paused", 640, 567, PAPER);
        center(body, "HP " + g.run.hp + "/" + CampaignRun.MAX_HP + "     " + g.run.enemy().name() + "  " + g.run.monsterHp + " HP", 640, 510, PAPER);
        center(small, "A/D walk     Tab journal     Enter submit     H guide", 640, 471, MUTED);
        button("Resume", 460, 366, 360, 46, true);
        button("Journal", 460, 307, 360, 46, false);
        button("Settings", 460, 248, 360, 46, false);
        button(g.slot < 0 ? "Return to Menu" : "Save & Return to Menu", 460, 189, 360, 46, false);
        center(small, g.slot < 0 ? "All slots are full. This run is unsaved."
            : g.saveMessage.isEmpty() ? "Resumes at the encounter checkpoint." : g.saveMessage, 640, 179, MUTED);
    }

    private void journal() {
        panel(275, 90, 730, 555);
        center(large, "Glossarium", 640, 596, PAPER);
        var entries = g.journalEntries();
        if (!entries.isEmpty()) {
            var lesson = entries.get(g.journalPage);
            center(body, lesson.kanji() + "  " + lesson.reading() + "  /  " + lesson.english(), 640, 523, PAPER);
            template(lesson.kanji(), 552, 316, 176);
            var learned = g.learning.get(lesson.kanji());
            if (learned != null) center(small, learned.independent() + " independent  /  " + learned.assisted() + " assisted", 640, 233, MUTED);
            if (g.run.phase == Phase.WALK) button("Practice (Q)", 530, 270, 220, 42, true);
            button("Previous", 312, 115, 180, 42, false);
            button("Next", 788, 115, 180, 42, false);
        }
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
        box(0, 0, 1280, 720, STAGE[g.ending ? 3 : 0]);
        if (!g.ending) {
            center(large, g.panels().get(g.panel), 640, 411, PAPER);
            button("Next", 300, 50, 190, 44, false);
            button("Skip", 790, 50, 190, 44, false);
            if (g.slot < 0) center(small, "All slots are full. This run is unsaved.", 640, 34, PAPER);
            else if (!g.saveMessage.isEmpty()) center(small, g.saveMessage, 640, 34, PAPER);
            return;
        }
        box(0, 220, 1280, 2, LINE);
        float drift = (float) Math.min(90, g.panelTime * 10);
        for (int i = 0; i < 7; i++) {
            int height = 70 + (i * 41 + g.panel * 31) % 210;
            float x = i * 212 - drift;
            box(x, 221, 164, height, PANEL);
            box(x + 18, 221 + height - 24, 128, 6, LINE);
        }
        actor(196 + 32 * g.panel + drift, 221, 3.7f, () -> player(1));
        if (g.panel >= 2 && (!g.ending || g.panel < 4)) {
            box(837, 221, 175, 242, LINE);
            box(860, 452, 134, 43, LINE);
            box(832, 491, 188, 16, PINK);
            box(873, 474, 90, 6, PAPER);
        }
        box(0, 0, 1280, 220, INK);
        center(small, (g.ending ? "Epilogue" : "Prologue") + "   " + (g.panel + 1) + "/" + g.panels().size(), 640, 197, ACCENT);
        write(body, g.panels().get(g.panel), 270, 164, 740, Align.center, PAPER);
        button("Next", 300, 50, 190, 44, false);
        button("Skip", 790, 50, 190, 44, false);
        if (g.slot < 0) center(small, "All slots are full. This run is unsaved.", 640, 34, PINK);
        else if (!g.saveMessage.isEmpty()) center(small, g.saveMessage, 640, 34, PINK);
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
        boolean hover = hit(g.mouseX, g.mouseY, x, y, w, h);
        box(x, y, w, h, primary ? ACCENT : hover ? LINE : PANEL);
        box(x, y, w, 1, LINE);
        write(body, label, x, y + h / 2 + body.getCapHeight() / 2, w, Align.center, primary ? INK : PAPER);
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
        write(font, value, x - 580, y, 1160, Align.center, color);
    }

    private void right(BitmapFont font, String value, float x, float y, Color color) {
        write(font, value, x - 260, y, 260, Align.right, color);
    }

    @Override public void dispose() {

        large.dispose();
        quizFont.dispose();
        body.dispose();
        small.dispose();
        pixel.dispose();
        batch.dispose();
    }
}
