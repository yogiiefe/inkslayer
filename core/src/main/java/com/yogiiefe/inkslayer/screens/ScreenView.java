package com.yogiiefe.inkslayer.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.yogiiefe.inkslayer.audio.GameAudio.Channel;
import com.yogiiefe.inkslayer.recognition.InkPoint;
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
    private static final DateTimeFormatter SAVE_DATE = DateTimeFormatter.ofPattern("dd MMM, HH:mm").withZone(ZoneId.systemDefault());

    private final GameScreen g;
    private final SpriteBatch batch = new SpriteBatch();
    private final ActorArt art = new ActorArt();
    private final Texture pixel;
    private final BitmapFont small, body, large, quizFont;
    private final TitleMenu titleMenu;
    private final GlyphLayout layout = new GlyphLayout();

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
            chars.append("カユソ");
            g.content.katakana.forEach(word -> chars.append(word.text()));
            String characters = chars.toString().codePoints().distinct()
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
            small = font(generator, 16, characters);
            body = font(generator, 20, characters);
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
            case TITLE -> titleMenu.draw(large, small, g.titleSelection, g.titleHover, g.elapsed);
            case SLOTS -> slots();
            case SETTINGS -> settings();
            case CINEMATIC -> cinematic();
            case GAME -> arena();
            case STORY -> { arena(); story(); }
            case PAUSE -> pause();
            case JOURNAL -> journal();
            case PRACTICE -> practice();
        }
        batch.end();
    }

    private void arena() {
        if (g.tutorial) {
            tutorial();
            return;
        }
        var run = g.run;
        boolean walk = run.phase == Phase.WALK;
        box(0, 0, 1280, 720, STAGE[run.stage]);
        box(0, 0, 1280, CANVAS_TOP, Color.WHITE);
        box(0, CANVAS_TOP - 2, 1280, 2, LINE);
        arenaControls();
        if (walk) {
            float scroll = (float) Math.max(0, run.x - 215) * 2;
            districtProp(655 - scroll * 0.55f);
            for (int i = 0; i < 24; i++) {
                float x = i * 64 - scroll % 64;
                box(x, CANVAS_TOP - 10, 25, 2, LINE);
            }
            art.player(batch, (float) run.x * 2 - scroll - 36, CANVAS_TOP, 3, run.facing, walkingPose());
            float enemyX = 2 * ((float) CampaignRun.ENCOUNTER_X + 145) - scroll;
            drawEnemy(enemyX);
            center(quizFont, g.content.stages.get(run.stage).name(), 640, 649, PAPER);
            center(small, "Encounter " + (run.encounter + 1) + " / 10", 640, 598, MUTED);
            center(small, "A / D or arrow keys to walk", 640, 137, INK);
            if (!g.saveMessage.isEmpty()) center(small, g.saveMessage, 640, 50, PINK);
            return;
        }

        art.player(batch, 155, CANVAS_TOP, 3, 1, 0);
        if (run.phase != Phase.WON) drawEnemy(846);
        if (g.effectTime > 0) spellEffect();
        if (g.mode == Mode.STORY && g.story == StoryBeat.FIRST_MASK) {
            art.unmasked(batch, 846, CANVAS_TOP);
            center(small, "PUNISHMENT RECORD  #013", 956, 576, PAPER);
        } else if (run.phase == Phase.WON || run.phase == Phase.LOST) outcome();
        else drawArea();
    }

    private void arenaControls() {
        button("Glossarium", 24, 646, 172, 46, false);
        button("Settings", 208, 646, 132, 46, false);
    }

    private void districtProp(float x) {
        int left = Math.round(x);
        box(left, 260, 10, 144, LINE);
        box(left - 52, 355, 230, 84, LINE);
        box(left - 48, 359, 222, 76, PANEL);
        String label = switch (g.run.stage) {
            case 0 -> "INSPECTION OPEN";
            case 1 -> "RESIDENCE SEALED";
            case 2 -> "SHELTER THIS WAY >";
            default -> "RATION QUEUE";
        };
        center(small, label, left + 62, 410, PAPER);
        if (g.run.stage == 0 && g.run.encounter == 0)
            center(small, "Record #013", left + 62, 382, MUTED);
    }

    private void tutorial() {
        box(0, 0, 1280, 720, STAGE[0]);
        box(0, 0, 1280, CANVAS_TOP, Color.WHITE);
        box(0, CANVAS_TOP - 2, 1280, 2, LINE);
        door(86, false);
        door(478, true);
        float x = (float) g.run.x * 1.25f + 104;
        art.player(batch, x, CANVAS_TOP, 3, g.run.facing, g.run.phase == Phase.WALK ? walkingPose() : 0);
        box(1045, 260, 112, 218, LINE);
        box(1056, 260, 90, 206, PANEL);
        for (int i = 0; i < 5; i++) box(1058, 276 + 38 * i, 86, 4, MUTED);
        if (g.revealed()) box(1063, 261, 76, 116, INK);
        arenaControls();
        if (g.run.phase == Phase.WALK) {
            center(quizFont, "The Unchecked Door", 640, 626, PAPER);
            center(small, "Your door is still unchecked. Walk right to the water gate.", 640, 578, PAPER);
            center(small, "D / Right Arrow", 640, 105, INK);
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

    private void story() {
        panel(280, 61, 720, 173);
        center(body, g.story.title, 640, 214, PAPER);
        write(body, g.story.lines.get(g.storyLine), 326, 179, 628, Align.center, PAPER);
        button("Skip (S)", 360, 75, 180, 44, false);
        button("Next (Enter)", 740, 75, 180, 44, true);
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
            center(small, g.feedback.isEmpty() ? "Click tiles or use arrows + Space   /   Enter submit   /   Backspace undo" : g.feedback, 640, 44, INK);
        } else {
            String guide = g.guideGlyph();
            if (guide != null) template(guide, GUIDE_X, GUIDE_Y, GUIDE_SIZE);
            for (var path : g.drawing.snapshot()) brush(path);
            brush(g.drawing.active());
            boolean empty = g.drawing.count() == 0 && !g.drawing.isActive();
            String prompt = g.revealed() && !practice ? "Correct!" : g.feedback;
            if (prompt.isEmpty() && empty)
                prompt = (guide == null ? "Draw Kanji" : "Trace the strokes")
                    + "   /   Enter submit   /   Ctrl+Z undo   /   R clear   /   H guide";
            if (!prompt.isEmpty()) center(small, prompt, 640, 35, INK);
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

    private void slots() {
        panel(292, 90, 696, 530);
        center(large, "Save Files", 640, 574, PAPER);
        for (int i = 0; i < g.slots.size(); i++) {
            var slot = g.slots.get(i);
            int y = 425 - i * 90;
            box(320, y, 640, 68, g.slotSelection == i ? LINE : PANEL);
            box(322, y + 2, 636, 64, g.slotSelection == i ? PANEL : INK);
            if (g.slotSelection == i) box(320, y, 4, 68, ACCENT);
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
            if (slot.loadable()) right(small, "Load", 816, y + 43, ACCENT);
            if (slot.occupied()) button("Delete", 840, y + 13, 100, 42, false);
        }
        center(small, g.saveMessage, 640, 201, PINK);
        button("Back", 490, 116, 300, 44, false);
        if (g.confirmDelete) {
            box(0, 0, 1280, 720, new Color(0, 0, 0, 0.8f));
            panel(356, 250, 568, 218);
            center(quizFont, "Delete Slot " + (g.slotSelection + 1) + "?", 640, 430, PAPER);
            center(body, "This removes the checkpoint and its backup.", 640, 375, MUTED);
            button("Cancel (Esc)", 414, 283, 200, 44, false);
            button("Delete", 666, 283, 200, 44, true);
        }
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
        center(small, g.settingsMessage.isEmpty() ? "Use the mouse or arrow keys to change settings" : g.settingsMessage,
            640, 126, g.settingsMessage.isEmpty() ? MUTED : PAPER);
        button("Back", 490, 136, 300, 42, true);
    }

    private void pause() {
        panel(350, 157, 580, 453);
        center(large, "Paused", 640, 567, PAPER);
        center(body, g.tutorial ? "The Unchecked Door" : g.content.stages.get(g.run.stage).name()
            + "  /  Encounter " + (g.run.encounter + 1) + " of 10", 640, 510, PAPER);
        center(small, "Esc resume   /   Enter submit   /   H guide", 640, 471, MUTED);
        button("Resume", 460, 366, 360, 46, true);
        button("Glossarium", 460, 307, 360, 46, false);
        button("Settings", 460, 248, 360, 46, false);
        button(g.slot < 0 || g.tutorial ? "Return to Menu" : "Save & Return to Menu", 460, 189, 360, 46, false);
        center(small, g.tutorial ? "First checkpoint: Stage 1." : g.slot < 0 ? "All slots are full. This run is unsaved."
            : g.saveMessage.isEmpty() ? "Resumes at the encounter checkpoint." : g.saveMessage, 640, 179, MUTED);
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
        box(0, 0, 1280, 720, STAGE[g.ending ? 3 : 0]);
        if (!g.ending) {
            center(small, "Prologue   " + (g.panel + 1) + " / " + g.panels().size(), 640, 489, MUTED);
            write(large, g.panels().get(g.panel), 172, 416, 936, Align.center, PAPER);
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
        art.player(batch, 156 + 32 * g.panel + drift, 221, 4, 1, 0);
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
        art.dispose();
        pixel.dispose();
        batch.dispose();
    }
}
