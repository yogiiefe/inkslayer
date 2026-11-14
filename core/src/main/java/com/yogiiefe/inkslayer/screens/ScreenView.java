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
    private static final Color INK = Color.valueOf("10111f"), PAPER = Color.valueOf("f5e9ec");
    private static final Color MUTED = Color.valueOf("b8acc7"), ACCENT = Color.valueOf("72e6d6");
    private static final Color PINK = Color.valueOf("ef739e"), PANEL = Color.valueOf("1c192f"), LINE = Color.valueOf("594d6f");
    private static final Color[] STAGE = colors("131126", "10211f", "101a30", "231229");
    private static final Color[] ELEMENT = colors("64bde4", "e57962", "acabc7", "74b998", "ceac76");
    private static final Color[] SHADOW = colors("345f93", "9c3c56", "625b82", "366952", "86604d");
    private static final DateTimeFormatter SAVE_DATE = DateTimeFormatter.ofPattern("dd MMM, HH:mm").withZone(ZoneId.systemDefault());

    private final GameScreen g;
    private final SpriteBatch batch = new SpriteBatch();
    private final Matrix4 transformed = new Matrix4(), identity = new Matrix4();
    private final Texture pixel;
    private final BitmapFont small, body, large, symbol;
    private final TitleMenu titleMenu;

    ScreenView(GameScreen screen) {
        g = screen;
        var pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        pixel = new Texture(pixmap);
        pixmap.dispose();
        titleMenu = new TitleMenu(batch, pixel);
        var generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/NotoSansJP.ttf"));
        try {
            var chars = new StringBuilder(FreeTypeFontGenerator.DEFAULT_CHARS).append("←→×");
            g.content.lessons.values().forEach(lesson -> chars.append(lesson.glyph()).append(lesson.reading()));
            g.content.katakana.forEach(word -> chars.append(word.text()));
            String characters = chars.toString().codePoints().distinct()
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
            small = font(generator, 15, characters);
            body = font(generator, 22, characters);
            large = font(generator, 38, characters);
            symbol = font(generator, 66, String.join("", g.content.lessons.keySet()));
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
        p.characters = chars;
        p.minFilter = Texture.TextureFilter.Linear;
        p.magFilter = Texture.TextureFilter.Linear;
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
        box(0, 0, 1280, CANVAS_TOP, INK);
        box(0, CANVAS_TOP - 2, 1280, 2, LINE);
        if (walk) {
            float scroll = (float) Math.max(0, run.x - 215) * 2;
            for (int i = 0; i < 24; i++) {
                float x = i * 64 - scroll % 64;
                box(x, CANVAS_TOP - 10, 25, 2, LINE);
            }
            actor((float) run.x * 2 - scroll, CANVAS_TOP, 2.6f, () -> player(run.facing));
            float enemyX = 2 * ((float) CampaignRun.ENCOUNTER_X + 145) - scroll;
            actor(enemyX, CANVAS_TOP, 5.2f, () -> monster(index));
            center(body, g.content.stages.get(run.stage).name(), 640, 650, PAPER);
            center(small, "Encounter " + (run.encounter + 1) + " / 10     A / D to walk     Tab for journal", 640, 615, MUTED);
            if (!g.saveMessage.isEmpty()) center(small, g.saveMessage, 640, 50, PINK);
            return;
        }

        actor(125, CANVAS_TOP, 2.6f, () -> player(1));
        if (run.phase != Phase.WON) actor(862, CANVAS_TOP, 5.2f, () -> monster(index));
        center(body, "Player", 155, 465, PAPER);
        center(body, run.enemy().name() + (run.isBoss() ? "  /  Round " + (run.round + 1) : ""), 1020, 671, PAPER);
        center(small, CampaignContent.GLYPHS.get(run.enemy().element()) + "  " + elementName(run.enemy().element()), 1020, 636, ELEMENT[index]);
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
        box(4, 13, 31, 27, Color.valueOf("718d96"));
        box(4, 13, 8, 27, Color.valueOf("3e596a"));
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
        int index = CampaignContent.ELEMENTS.indexOf(g.effectElement);
        float travel = (float) (1 - g.effectTime / 0.3);
        float x = g.effectHitsPlayer ? 930 - travel * 770 : 216 + travel * 720;
        float y = 357 + 42 * (float) Math.sin(travel * Math.PI);
        for (int i = 0; i < 5; i++) {
            float tail = (g.effectHitsPlayer ? 1 : -1) * i * 13;
            box(x + tail, y + (i % 3 - 1) * 10, 20 - i * 3, 9 - i, i == 0 ? PAPER : ELEMENT[index]);
        }
    }

    private void drawArea() {
        box(CANVAS_X, 0, 2, CANVAS_TOP, LINE);
        box(CANVAS_X + CANVAS_WIDTH - 2, 0, 2, CANVAS_TOP, LINE);
        if (g.run.phase == Phase.LESSON && !g.canDraw()) {
            showQuestion();
            return;
        }
        String guide = g.guideGlyph();
        if (guide != null) template(guide, GUIDE_X, GUIDE_Y, GUIDE_SIZE);
        for (var stroke : g.drawing.snapshot()) stroke(stroke, CANVAS_X, CANVAS_TOP, 1, PAPER, 4);
        stroke(g.drawing.active(), CANVAS_X, CANVAS_TOP, 1, PAPER, 4);
        String instruction = g.mode == Mode.PRACTICE ? "Practice " + g.practiceGlyph + "    Esc to return" : switch (g.run.phase) {
            case LESSON -> g.exercise == Exercise.TRACE
                ? "Trace " + g.glyph + "  " + g.lesson().reading()
                : "Draw the kanji for " + g.lesson().meaning() + "  (" + g.lesson().reading() + ")";
            case ATTACK -> "Your turn  /  Draw a spell";
            case DEFEND -> {
                String element = g.run.incoming();
                yield "Incoming " + CampaignContent.GLYPHS.get(element) + "  " + elementName(element) + "  /  Draw counter or G Guard";
            }
            default -> "";
        };
        center(small, instruction, 620, 45, g.run.phase == Phase.DEFEND ? PINK : PAPER);
        center(small, g.feedback.isEmpty() ? "Enter submit     H guide     R clear     Ctrl+Z undo" : g.feedback,
            620, 22, MUTED);
    }

    private void showQuestion() {
        switch (g.exercise) {
            case INTRO -> {
                center(symbol, g.glyph, 620, 226, ACCENT);
                center(body, g.lesson().reading() + "  /  " + g.lesson().meaning(), 620, 151, PAPER);
                center(small, g.lesson().romaji() + "     " + g.lesson().strokeCount() + " strokes", 620, 120, MUTED);
                button("Continue", 540, 66, 200, 46, true);
            }
            case KANA_INTRO -> {
                center(large, g.kanaWord, 620, 220, ACCENT);
                center(body, g.kana().romaji() + "  /  " + g.kana().meaning(), 620, 163, PAPER);
                write(small, g.kana().note(), 363, 127, 514, Align.center, MUTED);
                button("Continue", 540, 66, 200, 46, true);
            }
            default -> {
                String prompt = switch (g.exercise) {
                    case READING -> "Choose the hiragana reading for " + g.glyph;
                    case MEANING -> "Choose the meaning of " + g.glyph;
                    default -> "Choose the katakana for " + g.kana().meaning();
                };
                center(body, prompt, 620, 204, PAPER);
                for (int i = 0; i < g.answers.size(); i++)
                    button(g.answers.get(i), 346 + i * 194, 78, 178, 58, g.choice == i);
                center(small, g.feedback.isEmpty() ? "Click or use arrows and Enter     H hint" : g.feedback, 620, 49, MUTED);
            }
        }
    }

    private void outcome() {
        boolean won = g.run.phase == Phase.WON;
        panel(380, 278, 520, 238);
        center(large, won ? "Victory" : "Defeat", 640, 470, won ? ACCENT : PINK);
        center(body, won ? g.run.enemy().name() + " defeated" : "Retry from this encounter's checkpoint", 640, 412, PAPER);
        button(won ? "Continue" : "Retry", 475, 316, 330, 54, true);
        if (!g.saveMessage.isEmpty()) center(small, g.saveMessage, 640, 303, PINK);
    }

    private void slots() {
        panel(292, 90, 696, 530);
        center(large, g.newGame ? "New Game" : "Save Files", 640, 574, PAPER);
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
            if (g.newGame || slot.loadable())
                right(small, g.newGame ? g.overwrite == i ? "Confirm overwrite" : "Start" : "Load", 938, y + 51, ACCENT);
        }
        String message = g.overwrite >= 0 ? "Choose this slot again to replace its campaign." : g.saveMessage;
        center(small, message, 640, 201, PINK);
        button("Back", 490, 116, 300, 44, false);
    }

    private void settings() {
        panel(338, 104, 604, 513);
        center(large, "Settings", 640, 562, PAPER);
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
        center(small, "Arrow keys or mouse to adjust volume", 640, 183, MUTED);
        button("Back", 490, 136, 300, 42, true);
        center(small, g.settingsMessage, 640, 125, PINK);
    }

    private void pause() {
        panel(350, 157, 580, 453);
        center(large, "Paused", 640, 567, PAPER);
        center(body, "HP " + g.run.hp + "/" + CampaignRun.MAX_HP + "     " + g.run.enemy().name() + "  " + g.run.monsterHp + " HP", 640, 510, PAPER);
        center(small, "A/D walk     Tab journal     Enter submit     G guard     H guide", 640, 471, MUTED);
        button("Resume", 460, 366, 360, 46, true);
        button("Journal", 460, 307, 360, 46, false);
        button("Settings", 460, 248, 360, 46, false);
        button("Save & Return to Menu", 460, 189, 360, 46, false);
        center(small, g.saveMessage.isEmpty() ? "Resumes at the encounter checkpoint." : g.saveMessage, 640, 179, MUTED);
    }

    private void journal() {
        panel(275, 90, 730, 555);
        center(large, "Kanji Journal", 640, 596, PAPER);
        var entries = g.journalEntries();
        if (!entries.isEmpty()) {
            var lesson = entries.get(g.journalPage);
            center(body, lesson.glyph() + "  " + lesson.reading() + "  /  " + lesson.meaning(), 640, 523, PAPER);
            template(lesson.glyph(), 552, 316, 176);
            var learned = g.learning.get(lesson.id());
            if (learned != null) center(small, learned.independent() + " independent  /  " + learned.assisted() + " assisted", 640, 233, MUTED);
            if (g.run.phase == Phase.WALK) button("Practice (Q)", 530, 270, 220, 42, true);
            String cycle = "水 Water > 火 Fire > 金 Metal > 木 Wood > 土 Earth > 水 Water";
            center(small, cycle, 640, 195, ACCENT);
            button("Previous", 312, 115, 180, 42, false);
            button("Next", 788, 115, 180, 42, false);
        }
        button("Back", 530, 115, 220, 42, true);
    }

    private void practice() {
        var lesson = g.content.lessons.get(g.practiceGlyph);
        box(0, 0, 1280, 720, STAGE[g.run.stage]);
        center(symbol, lesson.glyph(), 620, 590, ACCENT);
        center(body, lesson.reading() + "  /  " + lesson.meaning(), 620, 486, PAPER);
        box(0, CANVAS_TOP - 2, 1280, 2, LINE);
        drawArea();
    }

    private void cinematic() {
        box(0, 0, 1280, 720, STAGE[g.ending ? 3 : 0]);
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
        if (!g.saveMessage.isEmpty()) center(small, g.saveMessage, 640, 34, PINK);
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
        symbol.dispose();
        large.dispose();
        body.dispose();
        small.dispose();
        pixel.dispose();
        batch.dispose();
    }
}
