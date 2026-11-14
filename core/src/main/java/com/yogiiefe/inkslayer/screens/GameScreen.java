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
import com.yogiiefe.inkslayer.world.FillQuiz;
import com.yogiiefe.inkslayer.world.CampaignRun.Phase;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public final class GameScreen extends ScreenAdapter {
    enum Mode { TITLE, SLOTS, SETTINGS, CINEMATIC, GAME, STORY, PAUSE, JOURNAL, PRACTICE }
    enum GlossaryTab { KANJI, HIRAGANA, KATAKANA }
    static final int CANVAS_X = 0, CANVAS_WIDTH = 1280, CANVAS_TOP = 260;
    static final int GUIDE_SIZE = 176, GUIDE_X = 552, GUIDE_Y = 54;
    private static final Track[] STAGE_TRACKS = {Track.STAGE1, Track.STAGE2, Track.STAGE3, Track.STAGE4};

    final CampaignContent content;
    final List<FillQuiz> quizzes;
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
    boolean confirmDelete;
    boolean ending, endingPending, completed, hint, assisted, strict;
    int panel, choice, setting, journalPage;
    GlossaryTab glossaryTab = GlossaryTab.KANJI;
    double effectTime;
    boolean tutorial;
    StoryBeat story;
    int storyLine;
    private Runnable afterStory;
    double elapsed, playTime, panelTime;
    float mouseX = -1, mouseY = -1;
    String feedback = "", saveMessage = "", settingsMessage = "";
    String practiceGlyph;
    String kanaInput = "";
    int tileSelection;
    private int examIndex, pointer = -1;
    private double correctTime = -1;
    private final List<FillQuiz> bossQuizzes;

    private double accumulator;
    private DesktopSmoke smoke;

    public GameScreen(CampaignContent content, List<FillQuiz> quizzes, Map<String, List<List<InkPoint>>> templates,
                      Predicate<DisplayMode> applyDisplay) {
        this.content = content;
        this.quizzes = quizzes;
        bossQuizzes = List.of("水", "猫", "雨", "卵").stream()
            .map(id -> quizzes.stream().filter(quiz -> quiz.kanji().equals(id)).findFirst().orElseThrow())
            .toList();
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
            else if (correctTime >= 0) {
                correctTime -= frame;
                if (correctTime <= 0) nextQuiz();
            }
        } else if (mode == Mode.CINEMATIC && saveMessage.isEmpty()) {
            panelTime += frame;
            if (panelTime >= 9) nextPanel();
        }
        viewport.apply();
        ScreenUtils.clear(0.035f, 0.035f, 0.035f, 1);
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
        if (run.phase == Phase.LESSON) {
            beginLesson();
            if (tutorial) hint = assisted = true;
            else if (run.isBoss()) showStory(StoryBeat.KOTOBA, () -> mode = Mode.GAME);
        }
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
        tutorial = false;
        playTime = 0;
        endingPending = completed = false;
        run.restore(0, 0);
        cinematic(false);
    }

    private void openSlots() {
        refreshSlots();
        confirmDelete = false;
        slotSelection = 0;
        saveMessage = "";
        mode = Mode.SLOTS;
    }

    private void refreshSlots() {
        slots = java.util.stream.IntStream.range(0, SaveStore.SLOT_COUNT).mapToObj(saveStore::inspect).toList();
    }

    private void requestDelete(int index) {
        if (!slots.get(index).occupied()) return;
        slotSelection = index;
        confirmDelete = true;
    }

    private void deleteSelectedSlot() {
        confirmDelete = false;
        try {
            saveStore.delete(slotSelection);
            saveMessage = "Slot " + (slotSelection + 1) + " deleted.";
        } catch (IOException | SecurityException ex) {
            Gdx.app.error("Save", "Could not delete slot", ex);
            saveMessage = "Could not delete slot " + (slotSelection + 1) + ".";
        }
        refreshSlots();
    }

    private void selectSlot(int selected) {
        slotSelection = selected;
        var entry = slots.get(selected);
        if (entry.loadable()) {
            tutorial = false;
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

    List<String> panels() {
        return ending ? content.ending : List.of("In this world", "Everyone speak Japanese",
            "Except me! I'm the only one that can't speak Japanese");
    }

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
            tutorial = true;
            enterStage();
        }
    }

    private void showStory(StoryBeat beat, Runnable continuation) {
        story = beat;
        storyLine = 0;
        afterStory = continuation;
        mode = Mode.STORY;
    }

    private void continueStory(boolean skip) {
        if (!skip && storyLine + 1 < story.lines.size()) {
            storyLine++;
            return;
        }
        var continuation = afterStory;
        afterStory = null;
        continuation.run();
    }

    private void enterStage() {
        clearDrawing();
        correctTime = -1;
        mode = Mode.GAME;
        accumulator = 0;
        effectTime = 0;
        audio.music(STAGE_TRACKS[run.stage]);
    }

    private void beginLesson() {
        examIndex = 0;
        tileSelection = 0;
        kanaInput = "";
        clearDrawing();
        if (run.isBoss()) audio.music(Track.BOSS);
    }

    FillQuiz quiz() {
        return run.isBoss() ? bossQuizzes.get(examIndex) : quizzes.get(run.stage * 10 + run.encounter);
    }

    boolean revealed() { return correctTime >= 0 || run.phase == Phase.WON; }
    int bossQuestionNumber() { return examIndex + 1; }
    int bossQuestionCount() { return bossQuizzes.size(); }

    List<String> tiles() {
        return (quiz().katakana() + "カユソ").codePoints().distinct().sorted()
            .mapToObj(Character::toString).toList();
    }

    static int tileX(int index, int count) {
        return (1280 - (count * 88 - 12)) / 2 + index * 88;
    }

    private void addTile(int index) {
        if (revealed() || kanaInput.length() >= quiz().katakana().length()) return;
        kanaInput += tiles().get(index);
    }

    private void acceptQuiz() {
        record(quiz().kanji(), true, !assisted);
        clearDrawing();
        correctTime = 0.75;
        effectTime = 0.3;
        audio.play(Cue.ACCEPT);
    }

    private void nextQuiz() {
        correctTime = -1;
        if (tutorial) {
            run.completeQuizEncounter();
            audio.play(Cue.VICTORY);
        } else if (run.isBoss() && examIndex + 1 < bossQuizzes.size()) {
            examIndex++;
            kanaInput = "";
            tileSelection = 0;
            clearDrawing();
        } else {
            run.completeQuizEncounter();
            if (run.isBoss()) endingPending = true;
            if (StoryBeat.victory(run.stage, run.encounter) == null) save();
            audio.play(Cue.VICTORY);
        }
    }

    private void record(String id, boolean correct, boolean independent) {
        var previous = learning.getOrDefault(id, new Learning(0, 0, 0, 0));
        learning.put(id, new Learning(
            previous.assisted() + (correct && !independent ? 1 : 0),
            previous.independent() + (correct && independent ? 1 : 0),
            previous.errors() + (correct ? 0 : 1), (long) playTime));
    }

    boolean canDraw() {
        return mode == Mode.PRACTICE || run.phase == Phase.LESSON && !revealed() && !run.isBoss();
    }

    String guideGlyph() {
        return hint ? mode == Mode.PRACTICE ? practiceGlyph : quiz().kanji() : null;
    }

    private void submit() {
        if (drawing.isActive() || !canDraw()) return;
        boolean practice = mode == Mode.PRACTICE;
        String target = practice ? practiceGlyph : quiz().kanji();
        var match = recognizer.match(drawing.snapshot(), practice && strict, templates.keySet());
        boolean correct = target.equals(match.id());
        if (correct && !practice) {
            acceptQuiz();
        } else {
            feedback = match.id() == null ? match.result().message()
                : correct ? "Correct!" : "Read the English clue and try again.";
            record(target, correct, !assisted);
            audio.play(correct ? Cue.ACCEPT : Cue.RETRY);
        }
    }

    private void advance() {
        if (tutorial && run.phase == Phase.WON) {
            run.restore(0, 0);
            if (!save()) {
                run.x = CampaignRun.ENCOUNTER_X;
                run.phase = Phase.LESSON;
                run.completeQuizEncounter();
                return;
            }
            tutorial = false;
            enterStage();
            showStory(StoryBeat.ELEMENTS_START, () -> mode = Mode.GAME);
        } else if (run.phase == Phase.LOST) {
            run.restore(run.stage, run.encounter);
            enterStage();
        } else if (run.isFinalEncounter()) {
            endingPending = true;
            if (save()) cinematic(true);
        } else if (run.phase == Phase.WON) {
            var beat = StoryBeat.victory(run.stage, run.encounter);
            if (beat != null) showStory(beat, this::advanceCheckpoint);
            else advanceCheckpoint();
        }
    }

    private void advanceCheckpoint() {
        mode = Mode.GAME;
        if (!save()) return;
        run.advance();
        enterStage();
        var beat = StoryBeat.arrival(run.stage, run.encounter);
        if (beat != null) showStory(beat, () -> mode = Mode.GAME);
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

    boolean beginInk(float x, float y, int index) {
        if ((mode != Mode.GAME && mode != Mode.PRACTICE) || !canDraw() || pointer != -1
            || !hit(x, y, CANVAS_X, 0, CANVAS_WIDTH, CANVAS_TOP)) return false;
        pointer = index;
        drawing.begin(x - CANVAS_X, CANVAS_TOP - y);
        feedback = "";
        audio.play(Cue.STROKE);
        return true;
    }

    void moveInk(float x, float y, int index) {
        if (index != pointer) return;
        drawing.add(Math.clamp(x - CANVAS_X, 0, CANVAS_WIDTH), Math.clamp(CANVAS_TOP - y, 0, CANVAS_TOP));
    }

    void endInk(float x, float y, int index) {
        if (index != pointer) return;
        moveInk(x, y, index);
        drawing.end();
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
            case SLOTS -> { if (confirmDelete) confirmDelete = false; else menu(); }
            case SETTINGS -> closeSettings();
            case CINEMATIC -> finishCinematic();
            case STORY -> continueStory(true);
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
        glossaryTab = GlossaryTab.KANJI;
        journalPage = 0;
        mode = Mode.JOURNAL;
    }

    List<FillQuiz> journalEntries() {
        return glossaryTab == GlossaryTab.KATAKANA ? bossQuizzes : quizzes;
    }

    private int journalSize() {
        return journalEntries().size() + (glossaryTab == GlossaryTab.KATAKANA ? content.katakana.size() : 0);
    }

    int glossarySize() { return journalSize(); }

    private void selectGlossaryTab(int index) {
        glossaryTab = GlossaryTab.values()[index];
        journalPage = 0;
    }

    private void practice() {
        if (run.phase != Phase.WALK || glossaryTab != GlossaryTab.KANJI) return;
        var entries = journalEntries();
        if (entries.isEmpty()) return;
        practiceGlyph = entries.get(journalPage).kanji();
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
                if (confirmDelete) {
                    if (hit(x, y, 414, 283, 200, 44)) confirmDelete = false;
                    else if (hit(x, y, 666, 283, 200, 44)) deleteSelectedSlot();
                    return;
                }
                if (hit(x, y, 490, 116, 300, 44)) back();
                else for (int i = 0; i < slots.size(); i++) {
                    int row = 425 - i * 90;
                    if (slots.get(i).occupied() && hit(x, y, 840, row + 13, 100, 42)) {
                        requestDelete(i);
                        break;
                    }
                    if (hit(x, y, 320, row, 640, 68)) {
                        selectSlot(i);
                        break;
                    }
                }
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
                else if (hit(x, y, 460, 189, 360, 46) && (tutorial || save())) menu();
            }
            case JOURNAL -> {
                int count = journalSize();
                if (hit(x, y, 530, 115, 220, 42)) back();
                else if (hit(x, y, 530, 270, 220, 42)) practice();
                else if (count > 0 && hit(x, y, 312, 115, 180, 42)) journalPage = Math.floorMod(journalPage - 1, count);
                else if (count > 0 && hit(x, y, 788, 115, 180, 42)) journalPage = (journalPage + 1) % count;
                else for (int i = 0; i < GlossaryTab.values().length; i++) {
                    if (hit(x, y, 324 + i * 214, 505, 204, 42)) {
                        selectGlossaryTab(i);
                        break;
                    }
                }
            }
            case GAME -> {
                if (hit(x, y, 24, 646, 172, 46)) { cancelStroke(); journal(mode); }
                else if (hit(x, y, 208, 646, 132, 46)) { cancelStroke(); settings(mode); }
                else if (run.phase == Phase.WON && hit(x, y, 475, 316, 330, 54)) advance();
                else if (run.phase == Phase.LESSON && run.isBoss() && !revealed()) {
                    var options = tiles();
                    for (int i = 0; i < options.size(); i++) {
                        if (hit(x, y, tileX(i, options.size()), 80, 76, 58)) { tileSelection = i; addTile(i); break; }
                    }
                }
            }
            case STORY -> {
                if (hit(x, y, 740, 75, 180, 44)) continueStory(false);
                else if (hit(x, y, 360, 75, 180, 44)) continueStory(true);
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
                    if (confirmDelete) {
                        if (key == Input.Keys.ENTER) deleteSelectedSlot();
                    } else if (key == Input.Keys.FORWARD_DEL) requestDelete(slotSelection);
                    else if (key == Input.Keys.UP || key == Input.Keys.DOWN) {
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
                case PAUSE -> {
                    if (key == Input.Keys.ENTER) back();
                    else if (key == Input.Keys.S && (tutorial || save())) menu();
                }
                case JOURNAL -> {
                    int size = journalSize();
                    if (key == Input.Keys.TAB || key == Input.Keys.ENTER) back();
                    else if (size > 0 && (key == Input.Keys.LEFT || key == Input.Keys.RIGHT))
                        journalPage = Math.floorMod(journalPage + (key == Input.Keys.LEFT ? -1 : 1), size);
                    else if (key == Input.Keys.NUM_1 || key == Input.Keys.NUM_2 || key == Input.Keys.NUM_3)
                        selectGlossaryTab(key - Input.Keys.NUM_1);
                    else if (key == Input.Keys.Q) practice();
                }
                case PRACTICE -> drawingKey(key);
                case GAME -> gameKey(key);
                case STORY -> {
                    if (key == Input.Keys.ENTER) continueStory(false);
                    else if (key == Input.Keys.S) continueStory(true);
                }
            }
            return true;
        }

        private void gameKey(int key) {
            if (key == Input.Keys.TAB || key == Input.Keys.Q && run.phase == Phase.WALK) {
                cancelStroke();
                journal(mode);
            } else if (run.phase == Phase.WON || run.phase == Phase.LOST) {
                if (key == Input.Keys.ENTER) advance();
            } else if (run.phase == Phase.LESSON && run.isBoss() && !revealed()) {
                if (key == Input.Keys.LEFT || key == Input.Keys.RIGHT)
                    tileSelection = Math.floorMod(tileSelection + (key == Input.Keys.LEFT ? -1 : 1), tiles().size());
                else if (key == Input.Keys.SPACE) addTile(tileSelection);
                else if (key == Input.Keys.BACKSPACE && !kanaInput.isEmpty())
                    kanaInput = kanaInput.substring(0, kanaInput.length() - 1);
                else if (key == Input.Keys.R) kanaInput = "";
                else if (key == Input.Keys.ENTER) {
                    if (kanaInput.equals(quiz().katakana())) acceptQuiz();
                    else { feedback = "Check the reading and try again."; audio.play(Cue.RETRY); }
                }
            } else if (canDraw()) drawingKey(key);
        }

        private void drawingKey(int key) {
            if (key == Input.Keys.ENTER) submit();
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
            else if (!beginInk(p.x, p.y, index) && pointer == -1) click(p.x, p.y);
            return true;
        }

        @Override public boolean touchDragged(int x, int y, int index) {
            var p = point(x, y);
            if (slider && mode == Mode.SETTINGS) changeVolume(setting, Math.clamp(Math.round((p.x - 496) / 3.2f), 0, 100));
            moveInk(p.x, p.y, index);
            return true;
        }

        @Override public boolean touchUp(int x, int y, int index, int button) {
            if (button != Input.Buttons.LEFT) return false;
            slider = false;
            var p = point(x, y);
            endInk(p.x, p.y, index);
            return true;
        }
    }
}
