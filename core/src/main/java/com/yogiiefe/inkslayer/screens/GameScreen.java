package com.yogiiefe.inkslayer.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.yogiiefe.inkslayer.audio.GameAudio;
import com.yogiiefe.inkslayer.audio.GameAudio.Channel;
import com.yogiiefe.inkslayer.audio.GameAudio.Cue;
import com.yogiiefe.inkslayer.audio.GameAudio.Track;
import com.yogiiefe.inkslayer.recognition.Drawing;
import com.yogiiefe.inkslayer.recognition.InkPoint;
import com.yogiiefe.inkslayer.recognition.KanjiRecognizer;
import com.yogiiefe.inkslayer.save.SaveStore;
import com.yogiiefe.inkslayer.save.SaveStore.Learning;
import com.yogiiefe.inkslayer.save.SaveStore.Progress;
import com.yogiiefe.inkslayer.world.CampaignContent;
import com.yogiiefe.inkslayer.world.CampaignRun;
import com.yogiiefe.inkslayer.world.CampaignRun.Phase;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public final class GameScreen extends ScreenAdapter {
    enum Mode { TITLE, SLOTS, SETTINGS, CINEMATIC, GAME, PAUSE, JOURNAL, PRACTICE }
    enum Exercise { INTRO, READING, MEANING, TRACE, RECALL, KANA_INTRO, KATAKANA }
    static final int CANVAS_X = 328, CANVAS_WIDTH = 584, CANVAS_TOP = 250;
    static final int GUIDE_SIZE = 176, GUIDE_X = 552, GUIDE_Y = 54;
    private static final Track[] STAGE_TRACKS = {Track.STAGE1, Track.STAGE2, Track.STAGE3, Track.STAGE4};

    final CampaignContent content;
    final CampaignRun run;
    final Map<String, List<List<InkPoint>>> templates;
    final Drawing drawing = new Drawing();
    final FitViewport viewport = new FitViewport(1280, 720, new OrthographicCamera());
    final GameAudio audio = new GameAudio();
    final Map<String, Learning> learning = new HashMap<>();
    final SaveStore saveStore = new SaveStore(SaveStore.defaultDirectory());
    private final KanjiRecognizer recognizer;
    private final ScreenView view;
    private final Predicate<DisplayMode> applyDisplay;
    private final com.badlogic.gdx.Preferences displayPreferences = Gdx.app.getPreferences("inkslayer-display");
    DisplayMode displayMode = DisplayMode.WINDOWED;

    Mode mode = Mode.TITLE;
    Mode settingsReturn = Mode.TITLE, journalReturn = Mode.GAME;
    TitleMenu.Option titleSelection = TitleMenu.Option.NEW, titleHover;
    List<SaveStore.Slot> slots = List.of();
    int slot = -1, slotSelection;
    boolean ending, endingPending, completed, hint, assisted, strict;
    int panel, choice, setting, journalPage;
    double effectTime;
    double elapsed, playTime, panelTime;
    float mouseX = -1, mouseY = -1;
    String feedback = "", saveMessage = "", settingsMessage = "";
    String glyph, kanaWord, practiceGlyph;
    String effectElement;
    boolean effectHitsPlayer;
    Exercise exercise;
    List<String> answers = List.of();
    private String answer;
    private int examIndex, pointer = -1;
    private double accumulator;
    private DesktopSmoke smoke;

    public GameScreen(CampaignContent content, Map<String, List<List<InkPoint>>> templates,
                      Predicate<DisplayMode> applyDisplay) {
        this.content = content;
        this.templates = templates;
        this.applyDisplay = applyDisplay;
        run = new CampaignRun(content);
        recognizer = KanjiRecognizer.fromTemplates(templates, 0.145);
        view = new ScreenView(this);
    }

    @Override public void show() {
        Gdx.input.setInputProcessor(new Controls());
        audio.music(Track.MENU);
        String saved = displayPreferences.getString("mode");
        for (DisplayMode mode : DisplayMode.values()) {
            if (mode.name().equals(saved) && mode != DisplayMode.WINDOWED) {
                Gdx.app.postRunnable(() -> changeDisplay(mode));
                break;
            }
        }
    }

    @Override public void render(float delta) {
        double frame = Math.min(delta, 0.1);
        elapsed += frame;
        effectTime = Math.max(0, effectTime - frame);
        if (mode == Mode.GAME) {
            playTime += frame;
            if (run.phase == Phase.WALK) walk(frame);
        } else if (mode == Mode.CINEMATIC && saveMessage.isEmpty()) {
            panelTime += frame;
            if (panelTime >= 9) nextPanel();
        }
        viewport.apply();
        ScreenUtils.clear(0.035f, 0.039f, 0.068f, 1);
        view.render();
        if (Boolean.getBoolean("inkslayer.smoke")) {
            if (smoke == null) smoke = new DesktopSmoke();
            smoke.frame(this);
        }
    }

    private void walk(double frame) {
        accumulator = Math.min(accumulator + frame, CampaignRun.STEP * 6);
        int move = (held(Input.Keys.D, Input.Keys.RIGHT) ? 1 : 0) - (held(Input.Keys.A, Input.Keys.LEFT) ? 1 : 0);
        while (accumulator >= CampaignRun.STEP) {
            run.tick(move);
            accumulator -= CampaignRun.STEP;
        }
        if (run.phase == Phase.LESSON) beginLesson();
    }

    private boolean held(int first, int second) {
        return Gdx.input.isKeyPressed(first) || Gdx.input.isKeyPressed(second);
    }

    private void menu() {
        mode = Mode.TITLE;
        titleHover = null;
        audio.music(Track.MENU);
    }

    private void startNewGame() {
        slot = -1;
        for (int index = 0; index < SaveStore.SLOT_COUNT; index++) {
            if (!saveStore.inspect(index).occupied()) {
                slot = index;
                break;
            }
        }
        learning.clear();
        playTime = 0;
        endingPending = completed = false;
        run.restore(0, 0);
        cinematic(false);
    }

    private void openSlots() {
        slots = java.util.stream.IntStream.range(0, SaveStore.SLOT_COUNT).mapToObj(saveStore::inspect).toList();
        slotSelection = 0;
        saveMessage = "";
        mode = Mode.SLOTS;
    }

    private void selectSlot(int selected) {
        slotSelection = selected;
        var entry = slots.get(selected);
        if (entry.loadable()) {
            slot = selected;
            var data = entry.progress();
            learning.clear();
            learning.putAll(data.learning());
            playTime = data.playSeconds();
            endingPending = data.endingPending();
            completed = data.completed();
            run.restore(data.stage(), data.encounter());
            if (completed || endingPending) cinematic(true);
            else enterStage();
        } else {
            saveMessage = entry.message().isEmpty() ? "This slot has no saved game." : entry.message();
        }
    }

    private boolean save() {
        if (slot < 0) return true;
        int stage = run.stage;
        int encounter = run.encounter;
        if (run.phase == Phase.WON && !run.isBoss()
            && ++encounter == content.stages.get(stage).encounters().size()) {
            stage++;
            encounter = 0;
        }
        try {
            saveStore.write(slot, new Progress(stage, encounter, 0, endingPending, completed,
                (long) playTime, 0, learning));
            saveMessage = "";
            return true;
        } catch (IOException | IllegalArgumentException ex) {
            Gdx.app.error("Save", "Checkpoint write failed", ex);
            saveMessage = "Could not save. The last saved checkpoint is still available.";
            return false;
        }
    }

    List<String> panels() { return ending ? content.ending : content.opening; }

    private void cinematic(boolean finalScene) {
        ending = finalScene;
        mode = Mode.CINEMATIC;
        panel = 0;
        panelTime = 0;
        saveMessage = "";
        audio.music(ending ? Track.ENDING : Track.MENU);
    }

    private void nextPanel() {
        panelTime = 0;
        if (panel + 1 < panels().size()) panel++;
        else finishCinematic();
    }

    private void finishCinematic() {
        if (ending) {
            endingPending = false;
            completed = true;
            if (save()) menu();
        } else {
            if (save()) enterStage();
        }
    }

    private void enterStage() {
        clearDrawing();
        exercise = null;
        mode = Mode.GAME;
        accumulator = 0;
        effectTime = 0;
        audio.music(STAGE_TRACKS[run.stage]);
    }

    private void beginLesson() {
        examIndex = 0;
        if (run.isBoss()) {
            audio.music(Track.BOSS);
            beginExam();
        } else {
            glyph = run.enemy().lessonId();
            if (glyph == null || glyph.isEmpty()) finishLesson();
            else if ((run.stage == 3 || run.encounter >= 5) && learning.containsKey(glyph)) {
                glyph = reviewGlyph(glyph);
                startExercise(Exercise.RECALL);
            } else startExercise(Exercise.INTRO);
        }
    }

    private String reviewGlyph(String preferred) {
        boolean mixedReview = run.stage == 3 ? run.encounter == 8 : run.encounter % 2 != 0;
        if (!mixedReview) return preferred;
        return content.lessons.keySet().stream().filter(learning::containsKey)
            .min(java.util.Comparator.<String>comparingLong(id -> learning.get(id).lastReviewed())
                .thenComparing(java.util.Comparator.<String>comparingInt(id -> learning.get(id).errors()).reversed())
                .thenComparing(id -> id))
            .orElse(preferred);
    }

    private void beginExam() {
        glyph = content.bossRounds.get(run.round).get(examIndex);
        startExercise(switch (examIndex) {
            case 0, 3 -> Exercise.MEANING;
            case 1 -> Exercise.READING;
            default -> Exercise.RECALL;
        });
    }

    CampaignContent.Lesson lesson() { return content.lessons.get(glyph); }

    private void startExercise(Exercise next) {
        boolean justIntroduced = exercise == Exercise.INTRO || exercise == Exercise.KANA_INTRO;
        clearDrawing();
        exercise = next;
        hint = next == Exercise.TRACE;
        assisted = hint || justIntroduced;
        choice = 0;
        answers = switch (next) {
            case READING -> choices(lesson().reading(), content.lessons.values().stream().map(CampaignContent.Lesson::reading).toList());
            case MEANING -> choices(lesson().meaning(), content.lessons.values().stream().map(CampaignContent.Lesson::meaning).toList());
            case KATAKANA -> choices(kanaWord, content.katakana.stream().map(CampaignContent.KanaWord::text).toList());
            default -> List.of();
        };
    }

    private List<String> choices(String correct, List<String> pool) {
        answer = correct;
        List<String> list = new ArrayList<>();
        list.add(correct);
        pool.stream().filter(option -> !option.equals(correct)).distinct().sorted().limit(2).forEach(list::add);
        java.util.Collections.rotate(list, Math.floorMod(run.encounter + examIndex + exercise.ordinal(), list.size()));
        return list;
    }

    CampaignContent.KanaWord kana() {
        return content.katakana.stream().filter(word -> word.text().equals(kanaWord))
            .findFirst().orElseThrow();
    }

    private void choose() {
        if (!answers.get(choice).equals(answer)) {
            record(exercise == Exercise.KATAKANA ? kanaWord : glyph, false, false);
            feedback = switch (exercise) {
                case READING -> "Reading: " + lesson().reading() + ". Try again.";
                case MEANING -> "Meaning: " + lesson().meaning() + ". Try again.";
                default -> "Look for " + answer + ". Try again.";
            };
            assisted = true;
            audio.play(Cue.RETRY);
            return;
        }
        audio.play(Cue.ACCEPT);
        record(exercise == Exercise.KATAKANA ? answer : glyph, true, !assisted);
        if (run.isBoss() && exercise != Exercise.KATAKANA) advanceExam();
        else if (exercise == Exercise.READING) startExercise(Exercise.TRACE);
        else finishLesson();
    }

    private void advanceExam() {
        examIndex++;
        if (examIndex == content.bossRounds.get(run.round).size()) {
            kanaWord = content.katakana.get(run.round * 3).text();
            startExercise(Exercise.KATAKANA);
        }
        else beginExam();
    }

    private void finishLesson() {
        exercise = null;
        answers = List.of();
        clearDrawing();
        run.finishLesson();
        if (run.phase == Phase.DEFEND) audio.play(Cue.valueOf(run.incoming()));
    }

    private void record(String id, boolean correct, boolean independent) {
        var previous = learning.getOrDefault(id, new Learning(0, 0, 0, 0));
        learning.put(id, new Learning(
            previous.assisted() + (correct && !independent ? 1 : 0),
            previous.independent() + (correct && independent ? 1 : 0),
            previous.errors() + (correct ? 0 : 1), (long) playTime));
    }

    Set<String> spells() {
        Set<String> available = new HashSet<>();
        for (int index = 0; index < 5; index++) {
            String learned = content.stages.getFirst().encounters().get(index).lessonId();
            if (run.stage > 0 || run.encounter >= index) available.add(learned);
        }
        return available;
    }

    boolean canDraw() {
        return mode == Mode.PRACTICE || run.phase == Phase.ATTACK || run.phase == Phase.DEFEND
            || run.phase == Phase.LESSON && (exercise == Exercise.TRACE || exercise == Exercise.RECALL);
    }

    String guideGlyph() {
        if (!hint) return null;
        if (mode == Mode.PRACTICE) return practiceGlyph;
        return switch (run.phase) {
            case LESSON -> glyph;
            case ATTACK -> CampaignContent.GLYPHS.get(CampaignContent.COUNTERS.get(run.enemy().element()));
            case DEFEND -> CampaignContent.GLYPHS.get(CampaignContent.COUNTERS.get(run.incoming()));
            default -> null;
        };
    }

    private void submit() {
        if (drawing.isActive() || !canDraw()) return;
        if (mode == Mode.PRACTICE) {
            var match = recognizer.match(drawing.snapshot(), strict, Set.of(practiceGlyph));
            feedback = match.result().message();
            audio.play(match.result().accepted() ? Cue.ACCEPT : Cue.RETRY);
            record(practiceGlyph, match.result().accepted(), !assisted);
            return;
        }
        boolean lesson = run.phase == Phase.LESSON;
        var match = recognizer.match(drawing.snapshot(), lesson && strict, lesson ? Set.of(glyph) : spells());
        if (match.id() == null || !match.result().accepted()) {
            feedback = match.result().message();
            if (lesson) record(glyph, false, false);
            audio.play(Cue.RETRY);
            return;
        }
        boolean usedHelp = assisted;
        clearDrawing();
        if (lesson) {
            record(glyph, true, !usedHelp);
            audio.play(Cue.ACCEPT);
            if (run.isBoss()) advanceExam();
            else if (!lesson().katakana().isEmpty()) {
                kanaWord = lesson().katakana();
                startExercise(learning.containsKey(kanaWord) ? Exercise.KATAKANA : Exercise.KANA_INTRO);
            }
            else finishLesson();
            return;
        }
        String element = CampaignContent.ELEMENTS.stream()
            .filter(id -> CampaignContent.GLYPHS.get(id).equals(match.id())).findFirst().orElseThrow();
        record(match.id(), true, !usedHelp);
        if (run.phase == Phase.ATTACK) {
            int hit = run.attack(element);
            audio.play(Cue.valueOf(element));
            effect(element, false);
            feedback = hit + " damage";
            resolvePhase();
        } else defend(element);
    }

    private void defend(String element) {
        clearDrawing();
        String incoming = run.incoming();
        int hit = run.defend(element);
        audio.play(hit == 0 ? Cue.COUNTER : Cue.HIT);
        effect(hit == 0 ? element : incoming, hit > 0);
        feedback = hit == 0 ? "Counter! No damage." : "You took " + hit + " damage.";
        resolvePhase();
    }

    private void effect(String element, boolean hitsPlayer) {
        effectElement = element;
        effectHitsPlayer = hitsPlayer;
        effectTime = 0.3;
    }

    private void resolvePhase() {
        if (run.phase == Phase.LESSON) beginLesson();
        else if (run.phase == Phase.LOST) audio.play(Cue.DEFEAT);
        else if (run.phase == Phase.WON) {
            if (run.isBoss()) endingPending = true;
            save();
            audio.play(Cue.VICTORY);
        }
    }

    private void advance() {
        if (run.phase == Phase.LOST) {
            run.restore(run.stage, run.encounter);
            enterStage();
        } else if (run.isFinalEncounter()) {
            endingPending = true;
            if (save()) cinematic(true);
        } else if (run.phase == Phase.WON) {
            if (save()) { run.advance(); enterStage(); }
        }
    }

    private void clearDrawing() {
        drawing.clear();
        pointer = -1;
        hint = false;
        assisted = false;
        feedback = "";
    }

    private void cancelStroke() {
        drawing.cancelActive();
        pointer = -1;
    }

    @Override public void resize(int width, int height) {
        viewport.update(width, height, true);
        titleHover = null;
        cancelStroke();
    }

    @Override public void pause() {
        cancelStroke();
        if (mode == Mode.GAME) mode = Mode.PAUSE;
        audio.pause();
    }

    @Override public void resume() { audio.resume(); }
    @Override public void hide() { Gdx.input.setInputProcessor(null); }
    @Override public void dispose() { audio.dispose(); view.dispose(); }

    static boolean hit(float x, float y, float left, float bottom, float width, float height) {
        return x >= left && x <= left + width && y >= bottom && y <= bottom + height;
    }

    static String elementName(String id) {
        return id.substring(0, 1) + id.substring(1).toLowerCase(Locale.ROOT);
    }

    private void settings(Mode from) {
        settingsReturn = from;
        setting = 0;
        settingsMessage = "";
        mode = Mode.SETTINGS;
    }

    private void closeSettings() {
        try {
            audio.saveSettings();
            mode = settingsReturn;
        } catch (com.badlogic.gdx.utils.GdxRuntimeException ex) {
            if (!settingsMessage.isEmpty()) {
                mode = settingsReturn;
                return;
            }
            Gdx.app.error("Settings", "Could not save volume settings", ex);
            settingsMessage = "Could not save volume. Choose Back again to keep changes for this session.";
        }
    }

    private void cycleDisplay(int direction) {
        var modes = DisplayMode.values();
        changeDisplay(modes[Math.floorMod(displayMode.ordinal() + direction, modes.length)]);
    }

    boolean changeDisplay(DisplayMode target) {
        if (!applyDisplay.test(target)) {
            settingsMessage = "This display mode is unavailable.";
            return false;
        }
        displayMode = target;
        settingsMessage = "";
        try {
            displayPreferences.putString("mode", target.name()).flush();
        } catch (com.badlogic.gdx.utils.GdxRuntimeException ex) {
            Gdx.app.error("Settings", "Could not save display mode", ex);
            settingsMessage = "Display mode could not be saved.";
        }
        return true;
    }

    private void back() {
        switch (mode) {
            case TITLE -> {}
            case SLOTS -> menu();
            case SETTINGS -> closeSettings();
            case CINEMATIC -> finishCinematic();
            case GAME -> { cancelStroke(); mode = Mode.PAUSE; }
            case PAUSE -> mode = Mode.GAME;
            case JOURNAL -> mode = journalReturn;
            case PRACTICE -> { clearDrawing(); mode = Mode.JOURNAL; }
        }
    }

    private void title(TitleMenu.Option option) {
        if (option == null) return;
        audio.play(Cue.UI);
        titleSelection = option;
        switch (option) {
            case NEW -> startNewGame();
            case SAVE -> openSlots();
            case SETTINGS -> settings(Mode.TITLE);
            case EXIT -> Gdx.app.exit();
        }
    }

    private void journal(Mode from) {
        journalReturn = from;
        journalPage = 0;
        mode = Mode.JOURNAL;
    }

    List<CampaignContent.Lesson> journalEntries() {
        return content.bossRounds.stream().flatMap(List::stream)
            .filter(id -> spells().contains(id) || learning.containsKey(id)).map(content.lessons::get).toList();
    }

    private void practice() {
        if (run.phase != Phase.WALK) return;
        var entries = journalEntries();
        if (entries.isEmpty()) return;
        practiceGlyph = entries.get(journalPage).glyph();
        clearDrawing();
        hint = assisted = true;
        mode = Mode.PRACTICE;
    }

    private void changeVolume(int index, int value) {
        setting = index;
        Channel channel = Channel.values()[index];
        audio.volume(channel, value);
        if (channel != Channel.BGM) audio.play(Cue.UI);
    }

    private boolean adjustSlider(float x, float y, boolean grab) {
        for (int index = 0; index < 3; index++) {
            if (hit(x, y, 496, 390 - index * 76, 320, 48)) {
                changeVolume(index, Math.round((x - 496) / 3.2f));
                return grab;
            }
        }
        return false;
    }

    private void click(float x, float y) {
        switch (mode) {
            case TITLE -> title(TitleMenu.Option.at(x, y));
            case SLOTS -> {
                if (hit(x, y, 490, 116, 300, 44)) back();
                else for (int i = 0; i < 3; i++) if (hit(x, y, 320, 425 - i * 90, 640, 68)) selectSlot(i);
            }
            case SETTINGS -> {
                if (hit(x, y, 490, 136, 300, 42)) back();
                else if (hit(x, y, 490, 204, 300, 40)) strict = !strict;
                else if (hit(x, y, 490, 473, 300, 44)) { setting = 3; cycleDisplay(1); }
            }
            case CINEMATIC -> {
                if (hit(x, y, 790, 50, 190, 44)) finishCinematic();
                else if (hit(x, y, 300, 50, 190, 44)) nextPanel();
            }
            case PAUSE -> {
                if (hit(x, y, 460, 366, 360, 46)) back();
                else if (hit(x, y, 460, 307, 360, 46)) journal(mode);
                else if (hit(x, y, 460, 248, 360, 46)) settings(mode);
                else if (hit(x, y, 460, 189, 360, 46) && save()) menu();
            }
            case JOURNAL -> {
                int count = journalEntries().size();
                if (hit(x, y, 530, 115, 220, 42)) back();
                else if (hit(x, y, 530, 270, 220, 42)) practice();
                else if (count > 0 && hit(x, y, 312, 115, 180, 42)) journalPage = Math.floorMod(journalPage - 1, count);
                else if (count > 0 && hit(x, y, 788, 115, 180, 42)) journalPage = (journalPage + 1) % count;
            }
            case GAME -> {
                if (run.phase == Phase.LESSON && !answers.isEmpty()) {
                    for (int i = 0; i < answers.size(); i++) {
                        if (hit(x, y, 346 + i * 194, 78, 178, 58)) { choice = i; choose(); break; }
                    }
                } else if (run.phase == Phase.LESSON && hit(x, y, 540, 66, 200, 46)) {
                    if (exercise == Exercise.INTRO) startExercise(Exercise.READING);
                    else if (exercise == Exercise.KANA_INTRO) startExercise(Exercise.KATAKANA);
                } else if ((run.phase == Phase.WON || run.phase == Phase.LOST) && hit(x, y, 475, 316, 330, 54)) advance();
            }
        }
    }

    private final class Controls extends InputAdapter {
        private boolean slider;

        @Override public boolean keyDown(int key) {
            if (key == Input.Keys.ESCAPE) { back(); return true; }
            switch (mode) {
                case TITLE -> {
                    titleHover = null;
                    if (key == Input.Keys.UP) titleSelection = titleSelection.step(-1);
                    else if (key == Input.Keys.DOWN || key == Input.Keys.TAB) titleSelection = titleSelection.step(1);
                    else if (key == Input.Keys.ENTER || key == Input.Keys.SPACE) title(titleSelection);
                }
                case SLOTS -> {
                    if (key == Input.Keys.UP || key == Input.Keys.DOWN) {
                        slotSelection = Math.floorMod(slotSelection + (key == Input.Keys.UP ? -1 : 1), 3);
                    } else if (key == Input.Keys.ENTER) selectSlot(slotSelection);
                }
                case SETTINGS -> {
                    if (key == Input.Keys.UP || key == Input.Keys.DOWN) setting = Math.floorMod(setting + (key == Input.Keys.UP ? -1 : 1), 4);
                    else if (key == Input.Keys.LEFT || key == Input.Keys.RIGHT) {
                        int direction = key == Input.Keys.LEFT ? -1 : 1;
                        if (setting == 3) cycleDisplay(direction);
                        else changeVolume(setting, Math.clamp(audio.volume(Channel.values()[setting]) + direction * 5, 0, 100));
                    } else if (key == Input.Keys.ENTER) back();
                }
                case CINEMATIC -> {
                    if (key == Input.Keys.ENTER || key == Input.Keys.SPACE) nextPanel();
                    else if (key == Input.Keys.S) finishCinematic();
                }
                case PAUSE -> { if (key == Input.Keys.ENTER) back(); }
                case JOURNAL -> {
                    int size = journalEntries().size();
                    if (key == Input.Keys.TAB || key == Input.Keys.ENTER) back();
                    else if (size > 0 && (key == Input.Keys.LEFT || key == Input.Keys.RIGHT))
                        journalPage = Math.floorMod(journalPage + (key == Input.Keys.LEFT ? -1 : 1), size);
                    else if (key == Input.Keys.Q) practice();
                }
                case PRACTICE -> drawingKey(key);
                case GAME -> gameKey(key);
            }
            return true;
        }

        private void gameKey(int key) {
            if (key == Input.Keys.TAB || key == Input.Keys.Q && run.phase == Phase.WALK) {
                cancelStroke();
                journal(mode);
            } else if (run.phase == Phase.WON || run.phase == Phase.LOST) {
                if (key == Input.Keys.ENTER) advance();
            } else if (!answers.isEmpty() && run.phase == Phase.LESSON) {
                if (key == Input.Keys.LEFT || key == Input.Keys.RIGHT)
                    choice = Math.floorMod(choice + (key == Input.Keys.LEFT ? -1 : 1), answers.size());
                else if (key == Input.Keys.ENTER) choose();
                else if (key == Input.Keys.H) { assisted = true; feedback = "Hint: " + answer; }
            } else if (run.phase == Phase.LESSON && key == Input.Keys.ENTER && exercise == Exercise.INTRO) {
                startExercise(Exercise.READING);
            } else if (run.phase == Phase.LESSON && key == Input.Keys.ENTER && exercise == Exercise.KANA_INTRO) {
                startExercise(Exercise.KATAKANA);
            } else if (canDraw()) drawingKey(key);
        }

        private void drawingKey(int key) {
            if (key == Input.Keys.ENTER) submit();
            else if (key == Input.Keys.G && run.phase == Phase.DEFEND && !drawing.isActive()) defend(null);
            else if (key == Input.Keys.H) { hint = !hint; assisted |= hint; }
            else if (key == Input.Keys.R) {
                drawing.clear();
                pointer = -1;
                assisted = hint;
                feedback = "";
            } else if (key == Input.Keys.Z && held(Input.Keys.CONTROL_LEFT, Input.Keys.CONTROL_RIGHT)) {
                drawing.undo();
                pointer = -1;
                feedback = "";
            }
        }

        private Vector2 point(int x, int y) { return viewport.unproject(new Vector2(x, y)); }

        @Override public boolean mouseMoved(int x, int y) {
            var p = point(x, y);
            mouseX = p.x;
            mouseY = p.y;
            titleHover = TitleMenu.Option.at(p.x, p.y);
            return true;
        }

        @Override public boolean touchDown(int x, int y, int index, int button) {
            if (button != Input.Buttons.LEFT) return false;
            var p = point(x, y);
            mouseX = p.x;
            mouseY = p.y;
            if (mode == Mode.SETTINGS && adjustSlider(p.x, p.y, true)) slider = true;
            else if ((mode == Mode.GAME || mode == Mode.PRACTICE) && canDraw() && pointer == -1
                && hit(p.x, p.y, CANVAS_X, 42, CANVAS_WIDTH, CANVAS_TOP - 42)) {
                pointer = index;
                drawing.begin(p.x - CANVAS_X, CANVAS_TOP - p.y);
                feedback = "";
                audio.play(Cue.STROKE);
            } else if (pointer == -1) click(p.x, p.y);
            return true;
        }

        @Override public boolean touchDragged(int x, int y, int index) {
            var p = point(x, y);
            if (slider && mode == Mode.SETTINGS) changeVolume(setting, Math.clamp(Math.round((p.x - 496) / 3.2f), 0, 100));
            if ((mode == Mode.GAME || mode == Mode.PRACTICE) && index == pointer)
                drawing.add(Math.clamp(p.x - CANVAS_X, 0, CANVAS_WIDTH), Math.clamp(CANVAS_TOP - p.y, 0, CANVAS_TOP - 42));
            return true;
        }

        @Override public boolean touchUp(int x, int y, int index, int button) {
            if (button != Input.Buttons.LEFT) return false;
            slider = false;
            if (index == pointer) {
                touchDragged(x, y, index);
                drawing.end();
                pointer = -1;
            }
            return true;
        }
    }
}
