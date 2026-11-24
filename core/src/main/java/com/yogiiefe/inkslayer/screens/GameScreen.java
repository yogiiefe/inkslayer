package com.yogiiefe.inkslayer.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
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
import com.yogiiefe.inkslayer.screens.KeyBindings.Action;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import static com.yogiiefe.inkslayer.screens.PixelArtViewport.UI_HEIGHT;
import static com.yogiiefe.inkslayer.screens.PixelArtViewport.UI_WIDTH;
import static com.yogiiefe.inkslayer.screens.PixelArtViewport.ART_PIXEL;
import static com.yogiiefe.inkslayer.screens.PixelArtViewport.ART_HEIGHT;

public final class GameScreen extends ScreenAdapter {
    enum Mode { TITLE, CHAPTER_SELECT, SLOTS, SETTINGS, KEYBINDS, CINEMATIC, GAME, PAUSE, JOURNAL, PRACTICE }
    enum GlossaryTab { KANJI, HIRAGANA, KATAKANA }
    static final int CANVAS_X = 0, CANVAS_WIDTH = 1280, CANVAS_TOP = 260;
    static final int GUIDE_SIZE = 176, GUIDE_X = 552, GUIDE_Y = 54;
    // Padded clickable area around the artwork in the 320x180 SkipAnim.png sheet.
    static final int SKIP_WIDTH = 132, SKIP_HEIGHT = 44;
    static final int SKIP_X = UI_WIDTH - SKIP_WIDTH - 8, SKIP_Y = UI_HEIGHT - SKIP_HEIGHT - 8;
    private static final double GUIDE_STROKES_PER_SECOND = 1.1;
    private static final Track[] STAGE_TRACKS = {Track.STAGE1, Track.STAGE2, Track.STAGE3, Track.STAGE4};

    final CampaignContent content;
    final List<FillQuiz> quizzes;
    final CampaignRun run;
    final Map<String, List<List<InkPoint>>> templates;
    final Drawing drawing = new Drawing();
    final PixelArtViewport viewport = new PixelArtViewport();
    final GameAudio audio = new GameAudio();
    final KeyBindings bindings = new KeyBindings();
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
    MenuButton menuFocus, menuHover, menuPress, keyboardPress;
    private int activationKey = -1;
    boolean showMenuFocus;
    List<SaveStore.Slot> slots = List.of();
    int slot = -1, slotSelection;
    boolean confirmDelete;
    boolean ending, endingPending, completed, assisted, strict, bindingKey;
    int panel, journalPage;
    int keybindScroll;
    Action selectedBinding = Action.LEFT;
    GlossaryTab glossaryTab = GlossaryTab.KANJI;
    double effectTime;
    boolean tutorial;
    double elapsed, playTime, panelStartedAt, titleHoverAt;
    double skipHoverAt = -1;
    boolean cinematicTextComplete;
    private int previousCinematicReveal;
    private boolean hussPanel;
    float mouseX = -1, mouseY = -1;
    String feedback = "", saveMessage = "", settingsMessage = "";
    String bindingMessage = "";
    String guideGlyph;
    double guideProgress;
    String practiceGlyph;
    String kanaInput = "";
    int tileSelection;
    private int examIndex, pointer = -1;
    private int menuPointer = -1;
    private float sliderGrabOffset;
    private double nextSliderCue;
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
        if (Gdx.graphics.getWidth() <= 0 || Gdx.graphics.getHeight() <= 0) return;

        double frame = Math.min(delta, 0.1);
        elapsed += frame;
        audio.update((float) frame);
        effectTime = Math.max(0, effectTime - frame);
        if (mode == Mode.GAME) {
            playTime += frame;
            if (run.phase == Phase.WALK) walk(frame);
            else if (correctTime >= 0) {
                correctTime -= frame;
                if (correctTime <= 0) nextQuiz();
            }
        } else if (mode == Mode.CINEMATIC) {
            playCinematicTyping();
            if (!hit(mouseX, mouseY, SKIP_X, SKIP_Y, SKIP_WIDTH, SKIP_HEIGHT)) skipHoverAt = -1;
            else if (skipHoverAt < 0) skipHoverAt = elapsed;
        }
        if ((mode == Mode.GAME || mode == Mode.PRACTICE) && guideGlyph != null) {
            guideProgress += frame * GUIDE_STROKES_PER_SECOND;
            if (guideProgress >= templates.get(guideGlyph).size()) guideGlyph = null;
        }
        ScreenUtils.clear(0, 0, 0, 1);
        view.render();
        if (Boolean.getBoolean("inkslayer.smoke")) {
            if (smoke == null) smoke = new DesktopSmoke();
            smoke.frame(this);
        }
    }

    private void walk(double frame) {
        accumulator = Math.min(accumulator + frame, CampaignRun.STEP * 6);
        int move = (bindings.held(Action.RIGHT) ? 1 : 0) - (bindings.held(Action.LEFT) ? 1 : 0);
        while (accumulator >= CampaignRun.STEP) {
            run.tick(move);
            accumulator -= CampaignRun.STEP;
        }
        if (run.phase == Phase.LESSON) {
            beginLesson();
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

    static int chapterY(int chapter) { return 453 - 73 * chapter; }

    private void openChapterSelect() {
        showMenu(Mode.CHAPTER_SELECT);
    }

    private void startNewGame(int chapter) {
        slot = -1;
        for (int index = 0; index < SaveStore.SLOT_COUNT; index++) {
            if (!saveStore.inspect(index).occupied()) {
                slot = index;
                break;
            }
        }
        learning.clear();
        tutorial = chapter == 0;
        playTime = 0;
        endingPending = completed = false;
        run.restore(chapter, 0);
        cinematic(false);
        if (!tutorial && !save()) saveMessage = "Could not create the first checkpoint. You can still play.";
    }

    private void openSlots() {
        refreshSlots();
        confirmDelete = false;
        slotSelection = 0;
        saveMessage = "";
        showMenu(Mode.SLOTS);
    }

    private void refreshSlots() {
        slots = java.util.stream.IntStream.range(0, SaveStore.SLOT_COUNT).mapToObj(saveStore::inspect).toList();
    }

    private void requestDelete(int index) {
        if (!slots.get(index).occupied()) return;
        slotSelection = index;
        confirmDelete = true;
        showMenu(Mode.SLOTS);
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
        showMenu(Mode.SLOTS);
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
            else if (data.stage() > 0 && data.encounter() == 0) cinematic(false);
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
        return ending ? content.ending : content.stages.get(run.stage).opening();
    }

    private void cinematic(boolean finalScene) {
        ending = finalScene;
        mode = Mode.CINEMATIC;
        panel = 0;
        startCinematicPanel();
        skipHoverAt = -1;
        saveMessage = "";
        audio.music(Track.STORY);
    }

    private void nextPanel() {
        if (panel + 1 < panels().size()) {
            panel++;
            startCinematicPanel();
        } else finishCinematic();
    }

    private void startCinematicPanel() {
        audio.stop(Cue.HUSS);
        panelStartedAt = elapsed;
        cinematicTextComplete = false;
        previousCinematicReveal = 0;
        hussPanel = cinematicText().equals("HUSS HUSS HUSS");
        if (hussPanel) {
            audio.stop(Cue.TYPEWRITER);
            audio.play(Cue.HUSS);
        }
    }

    int cinematicVisibleCharacters() {
        if (!cinematicTextComplete) return (int) ((elapsed - panelStartedAt) * 28);
        String text = cinematicText();
        return text.codePointCount(0, text.length());
    }

    String cinematicText() {
        return panels().get(panel).toUpperCase(Locale.ROOT);
    }

    private void playCinematicTyping() {
        if (hussPanel || cinematicTextComplete) return;
        String text = cinematicText();
        int visible = Math.min(cinematicVisibleCharacters(), text.codePointCount(0, text.length()));
        if (visible <= previousCinematicReveal) return;

        int start = text.offsetByCodePoints(0, previousCinematicReveal);
        int end = text.offsetByCodePoints(start, visible - previousCinematicReveal);
        previousCinematicReveal = visible;
        for (int index = start; index < end;) {
            int character = text.codePointAt(index);
            if (!Character.isWhitespace(character)) {
                audio.play(Cue.TYPEWRITER);
                return;
            }
            index += Character.charCount(character);
        }
    }

    private void advanceCinematic() {
        String text = cinematicText();
        if (cinematicVisibleCharacters() < text.codePointCount(0, text.length())) cinematicTextComplete = true;
        else nextPanel();
    }

    private void finishCinematic() {
        audio.stop(Cue.HUSS);
        if (ending) {
            endingPending = false;
            completed = true;
            if (save()) menu();
        } else enterStage();
    }

    private void skipCinematic() {
        audio.play(Cue.SKIP);
        finishCinematic();
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
        else showGuide();
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
            save();
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

    private void showGuide() {
        guideGlyph = mode == Mode.PRACTICE ? practiceGlyph : quiz().kanji();
        guideProgress = 0;
        assisted = true;
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
        } else if (run.phase == Phase.LOST) {
            run.restore(run.stage, run.encounter);
            enterStage();
        } else if (run.isFinalEncounter()) {
            endingPending = true;
            if (save()) cinematic(true);
        } else if (run.phase == Phase.WON) advanceCheckpoint();
    }

    private void advanceCheckpoint() {
        mode = Mode.GAME;
        if (!save()) return;
        int previousStage = run.stage;
        run.advance();
        if (run.stage != previousStage) {
            cinematic(false);
            return;
        }
        enterStage();
    }

    private void clearDrawing() {
        drawing.clear();
        pointer = -1;
        guideGlyph = null;
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
        clearMenuPointer();
        cancelStroke();
    }

    @Override public void pause() {
        cancelStroke();
        clearMenuPointer();
        if (mode == Mode.GAME) showMenu(Mode.PAUSE);
        audio.pause();
    }

    @Override public void resume() {
        audio.resume();
        if (mode == Mode.CINEMATIC && hussPanel) audio.play(Cue.HUSS);
    }
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
        settingsMessage = "";
        showMenu(Mode.SETTINGS);
    }

    private void openKeybinds() {
        bindingKey = false;
        keybindScroll = 0;
        selectedBinding = Action.LEFT;
        bindingMessage = "";
        showMenu(Mode.KEYBINDS);
    }

    private void closeSettings() {
        try {
            audio.saveSettings();
            showMenu(settingsReturn);
        } catch (com.badlogic.gdx.utils.GdxRuntimeException ex) {
            if (!settingsMessage.isEmpty()) {
                showMenu(settingsReturn);
                return;
            }
            Gdx.app.error("Settings", "Could not save volume settings", ex);
            settingsMessage = "Volume could not save. Choose Back again.";
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
        clearMenuPointer();
        switch (mode) {
            case TITLE -> {}
            case CHAPTER_SELECT -> menu();
            case SLOTS -> {
                if (confirmDelete) {
                    confirmDelete = false;
                    showMenu(Mode.SLOTS);
                } else menu();
            }
            case SETTINGS -> closeSettings();
            case KEYBINDS -> {
                if (bindingKey) { bindingKey = false; bindingMessage = ""; }
                else {
                    showMenu(Mode.SETTINGS);
                    menuFocus = MenuButton.KEYBINDS;
                }
            }
            case CINEMATIC -> skipCinematic();
            case GAME -> { cancelStroke(); showMenu(Mode.PAUSE); }
            case PAUSE -> mode = Mode.GAME;
            case JOURNAL -> showMenu(journalReturn);
            case PRACTICE -> { clearDrawing(); mode = Mode.JOURNAL; }
        }
    }

    private void title(TitleMenu.Option option) {
        if (option == null) return;
        titleHover = null;
        audio.play(Cue.UI);
        titleSelection = option;
        switch (option) {
            case NEW -> openChapterSelect();
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
        mode = Mode.PRACTICE;
        showGuide();
    }

    private void showMenu(Mode next) {
        mode = next;
        clearMenuPointer();
        showMenuFocus = false;
        var controls = menuButtons();
        menuFocus = controls.length == 0 ? null : controls[0];
    }

    private MenuButton[] menuButtons() {
        return switch (mode) {
            case CHAPTER_SELECT -> MenuButton.CHAPTERS;
            case SLOTS -> confirmDelete ? MenuButton.DELETE_DIALOG : MenuButton.SLOTS;
            case SETTINGS -> MenuButton.SETTINGS;
            case KEYBINDS -> MenuButton.BINDINGS;
            case PAUSE -> MenuButton.PAUSE;
            default -> MenuButton.NONE;
        };
    }

    private boolean menuEnabled(MenuButton control) {
        return switch (control) {
            case TRASH_1, TRASH_2, TRASH_3 -> slots.get(control.ordinal() - MenuButton.TRASH_1.ordinal()).occupied();
            default -> true;
        };
    }

    private MenuButton menuAt(float x, float y) {
        boolean inKeybinds = mode == Mode.KEYBINDS;
        if (inKeybinds && MenuButton.KEYBIND_SCROLL.contains(x, y, 0)) return MenuButton.KEYBIND_SCROLL;
        if (inKeybinds && MenuButton.inKeybindList(x, y)) {
            float rowOffset = ART_HEIGHT - y / ART_PIXEL - MenuButton.LEFT.top + keybindScroll;
            return MenuButton.BINDINGS[(int) (rowOffset / MenuButton.LEFT.height)];
        }
        for (MenuButton control : menuButtons()) {
            if (inKeybinds && control.bindingIndex() >= 0) continue;
            int slotIndex = control.slotIndex();
            int extraWidth = slotIndex >= 0 && !slots.get(slotIndex).occupied() ? MenuButton.EMPTY_SLOT_EXTRA_WIDTH : 0;
            if (control.contains(x, y, extraWidth) && menuEnabled(control)) return control;
        }
        return null;
    }

    private void clearMenuPointer() {
        menuPointer = -1;
        activationKey = -1;
        keyboardPress = menuPress = menuHover = null;
    }

    private void moveMenuFocus(int direction, boolean includeTrash) {
        var controls = menuButtons();
        int index = java.util.Arrays.asList(controls).indexOf(menuFocus);
        if (index < 0 && direction < 0) index = 0;
        do {
            index = Math.floorMod(index + direction, controls.length);
        } while (!menuEnabled(controls[index]) || !includeTrash && switch (controls[index]) {
            case TRASH_1, TRASH_2, TRASH_3 -> true;
            default -> false;
        });
        menuFocus = controls[index];
        if (mode == Mode.KEYBINDS) revealBinding(menuFocus);
    }

    private void moveChapterFocus(int axis, int direction) {
        int index = java.util.Arrays.asList(MenuButton.CHAPTERS).indexOf(menuFocus);
        if (index >= 0 && index < 4) menuFocus = MenuButton.CHAPTERS[index ^ axis];
        else moveMenuFocus(direction, false);
    }

    private void changeVolume(int index, int value, boolean start) {
        Channel channel = Channel.values()[index];
        boolean changed = audio.volume(channel) != value;
        if (changed) audio.volume(channel, value);
        sliderCue(changed, start);
    }

    private void sliderCue(boolean changed, boolean start) {
        if (start || changed && elapsed >= nextSliderCue) {
            nextSliderCue = elapsed + .07;
            audio.play(Cue.SLIDER);
        }
    }

    private float sliderPosition(float pointer, float min, float max, float center, int handleSize, boolean start) {
        if (start) {
            int handleStart = Math.round(center) - handleSize / 2;
            sliderGrabOffset = pointer >= handleStart && pointer < handleStart + handleSize ? pointer - center : 0;
        }
        return Math.clamp((pointer - sliderGrabOffset - min) / (max - min), 0f, 1f);
    }

    private void dragMenuControl(float x, float y, boolean start) {
        if (menuPress == MenuButton.KEYBIND_SCROLL) {
            var rail = MenuButton.KEYBIND_SCROLL;
            int size = MenuButton.KEYBIND_THUMB_HEIGHT;
            float position = sliderPosition(ART_HEIGHT - y / ART_PIXEL,
                rail.top + size / 2f, rail.top + rail.height - size / 2f,
                MenuButton.keybindThumbTop(keybindScroll) + size / 2f, size, start);
            scrollKeybinds(Math.round(position * MenuButton.KEYBIND_SCROLL_MAX), start);
        } else if (menuPress.sliderIndex() >= 0) {
            int index = menuPress.sliderIndex();
            int min = MenuButton.TRACK_START, max = MenuButton.TRACK_END;
            float center = min + audio.volume(Channel.values()[index]) * (max - min) / 100f;
            float position = sliderPosition(x / ART_PIXEL, min, max, center, 5, start);
            changeVolume(index, Math.round(position * 100), start);
        }
    }

    private void scrollKeybinds(int position, boolean start) {
        int scroll = Math.clamp(position, 0, MenuButton.KEYBIND_SCROLL_MAX);
        sliderCue(scroll != keybindScroll, start);
        keybindScroll = scroll;
        if (!bindingKey && menuFocus != null && menuFocus.bindingIndex() >= 0) {
            int rowHeight = MenuButton.LEFT.height;
            int first = scroll / rowHeight;
            int last = (scroll + MenuButton.KEYBIND_LIST_HEIGHT - 1) / rowHeight;
            menuFocus = MenuButton.BINDINGS[Math.clamp(menuFocus.bindingIndex(), first, last)];
        }
    }

    private void revealBinding(MenuButton row) {
        if (row.bindingIndex() < 0) return;
        int top = row.top - keybindScroll;
        if (top < MenuButton.LEFT.top) keybindScroll = row.top - MenuButton.LEFT.top;
        else if (top + row.height > MenuButton.KEYBIND_LIST_END)
            keybindScroll = row.top + row.height - MenuButton.KEYBIND_LIST_END;
    }

    private void activateMenu(MenuButton control) {
        if (control == null || control.draggable()) return;
        audio.play(Cue.UI);
        switch (control) {
            case BACK, CANCEL_DELETE, RESUME -> back();
            case ELEMENTS, PETS, WEATHER, FOOD -> startNewGame(control.ordinal() - MenuButton.ELEMENTS.ordinal());
            case SLOT_1, SLOT_2, SLOT_3 -> selectSlot(control.ordinal() - MenuButton.SLOT_1.ordinal());
            case TRASH_1, TRASH_2, TRASH_3 -> requestDelete(control.ordinal() - MenuButton.TRASH_1.ordinal());
            case CONFIRM_DELETE -> deleteSelectedSlot();
            case DISPLAY -> cycleDisplay(1);
            case STROKE -> strict = !strict;
            case KEYBINDS -> openKeybinds();
            case LEFT, RIGHT, CONTINUE, HELP, CLEAR, UNDO -> {
                revealBinding(control);
                selectedBinding = Action.values()[control.bindingIndex()];
                bindingKey = true;
                bindingMessage = "";
            }
            case DEFAULT -> {
                bindingKey = false;
                bindingMessage = bindings.reset();
            }
            case GLOSSARIUM -> journal(mode);
            case PAUSE_SETTINGS -> settings(mode);
            case SAVE_MENU -> { if (tutorial || save()) menu(); }
            case MASTER, SFX, BGM, KEYBIND_SCROLL -> {}
        }
    }

    private void click(float x, float y) {
        switch (mode) {
            case TITLE -> title(TitleMenu.Option.at(x, y));
            case CINEMATIC -> {
                if (hit(x, y, SKIP_X, SKIP_Y, SKIP_WIDTH, SKIP_HEIGHT)) skipCinematic();
                else if (hit(x, y, 0, 0, UI_WIDTH, UI_HEIGHT)) advanceCinematic();
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
                if (run.phase == Phase.WON && hit(x, y, 475, 316, 330, 54)) advance();
                else if (run.phase == Phase.LESSON && run.isBoss() && !revealed()) {
                    var options = tiles();
                    for (int i = 0; i < options.size(); i++) {
                        if (hit(x, y, tileX(i, options.size()), 80, 76, 58)) { tileSelection = i; addTile(i); break; }
                    }
                }
            }
        }
    }

    private final class Controls extends InputAdapter {
        @Override public boolean keyDown(int key) {
            if (keyboardPress != null && key == activationKey) return true;
            if (key == Input.Keys.ESCAPE) {
                if (menuButtons().length > 0) audio.play(Cue.UI);
                back();
                return true;
            }
            switch (mode) {
                case TITLE -> {
                    titleHover = null;
                    if (key == Input.Keys.UP) titleSelection = titleSelection.step(-1);
                    else if (key == Input.Keys.DOWN || key == Input.Keys.TAB) titleSelection = titleSelection.step(1);
                    else if (key == Input.Keys.ENTER || key == Input.Keys.SPACE) title(titleSelection);
                }
                case CHAPTER_SELECT, SLOTS, SETTINGS, KEYBINDS, PAUSE -> menuKey(key);
                case CINEMATIC -> {
                    if (key == Input.Keys.S) skipCinematic();
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
            }
            return true;
        }

        @Override public boolean keyUp(int key) {
            if (keyboardPress == null || activationKey != key) return false;
            var pressed = keyboardPress;
            keyboardPress = null;
            activationKey = -1;
            if (menuFocus == pressed) activateMenu(pressed);
            return true;
        }

        private void menuKey(int key) {
            clearMenuPointer();
            showMenuFocus = true;
            if (mode == Mode.KEYBINDS && bindingKey) {
                bindingMessage = bindings.bind(selectedBinding, key);
                bindingKey = !bindings.matches(selectedBinding, key);
                if (!bindingKey) audio.play(Cue.UI);
                return;
            }
            switch (key) {
                case Input.Keys.UP, Input.Keys.DOWN, Input.Keys.TAB -> {
                    if (mode == Mode.PAUSE && key == Input.Keys.TAB) activateMenu(MenuButton.GLOSSARIUM);
                    else if (mode == Mode.CHAPTER_SELECT && key != Input.Keys.TAB)
                        moveChapterFocus(2, key == Input.Keys.UP ? -1 : 1);
                    else moveMenuFocus(key == Input.Keys.UP ? -1 : 1, key == Input.Keys.TAB);
                }
                case Input.Keys.LEFT, Input.Keys.RIGHT -> {
                    int direction = key == Input.Keys.LEFT ? -1 : 1;
                    if (mode == Mode.SETTINGS && menuFocus != null && menuFocus.sliderIndex() >= 0) {
                        int index = menuFocus.sliderIndex();
                        int value = audio.volume(Channel.values()[index]);
                        changeVolume(index, Math.clamp(value + direction * 5, 0, 100), false);
                    } else if (mode == Mode.SETTINGS && menuFocus == MenuButton.DISPLAY) {
                        audio.play(Cue.UI);
                        cycleDisplay(direction);
                    } else if (mode == Mode.CHAPTER_SELECT) moveChapterFocus(1, direction);
                    else if (mode != Mode.KEYBINDS) moveMenuFocus(direction, false);
                }
                case Input.Keys.ENTER, Input.Keys.SPACE -> {
                    if (menuFocus != null && !menuFocus.draggable()) {
                        keyboardPress = menuFocus;
                        activationKey = key;
                    }
                }
                case Input.Keys.FORWARD_DEL -> {
                    if (mode != Mode.SLOTS || confirmDelete || menuFocus == null) return;
                    int row = menuFocus.slotIndex();
                    if (row >= 0 && slots.get(row).occupied()) activateMenu(MenuButton.TRASH[row]);
                }
                case Input.Keys.S -> { if (mode == Mode.PAUSE) activateMenu(MenuButton.SAVE_MENU); }
                default -> {}
            }
        }

        private void gameKey(int key) {
            if (run.phase == Phase.WON || run.phase == Phase.LOST) {
                if (bindings.matches(Action.SUBMIT, key)) advance();
            } else if (run.phase == Phase.LESSON && run.isBoss() && !revealed()) {
                if (bindings.matches(Action.LEFT, key) || bindings.matches(Action.RIGHT, key))
                    tileSelection = Math.floorMod(tileSelection + (bindings.matches(Action.LEFT, key) ? -1 : 1), tiles().size());
                else if (key == Input.Keys.SPACE) addTile(tileSelection);
                else if (key == Input.Keys.BACKSPACE && !kanaInput.isEmpty())
                    kanaInput = kanaInput.substring(0, kanaInput.length() - 1);
                else if (bindings.matches(Action.CLEAR, key)) kanaInput = "";
                else if (bindings.matches(Action.SUBMIT, key)) {
                    if (kanaInput.equals(quiz().katakana())) acceptQuiz();
                    else { feedback = "Check the reading and try again."; audio.play(Cue.RETRY); }
                }
            } else if (canDraw()) drawingKey(key);
        }

        private void drawingKey(int key) {
            if (bindings.matches(Action.SUBMIT, key)) submit();
            else if (bindings.matches(Action.HELP, key)) showGuide();
            else if (bindings.matches(Action.CLEAR, key)) {
                drawing.clear();
                pointer = -1;
                assisted = guideGlyph != null;
                feedback = "";
            } else if (bindings.matches(Action.UNDO, key) && held(Input.Keys.CONTROL_LEFT, Input.Keys.CONTROL_RIGHT)) {
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
            if (mode == Mode.TITLE) {
                var hovered = TitleMenu.Option.at(p.x, p.y);
                if (hovered != titleHover) {
                    titleHover = hovered;
                    titleHoverAt = elapsed;
                    if (hovered != null) audio.play(Cue.UI);
                }
            } else updateMenuHover(p);
            return true;
        }

        private void updateMenuHover(Vector2 p) {
            var hovered = menuAt(p.x, p.y);
            if (hovered != null) showMenuFocus = false;
            if (hovered != menuHover && hovered != null && menuPress == null) {
                audio.play(hovered.draggable() ? Cue.SLIDER : Cue.UI);
            }
            menuHover = hovered;
        }

        @Override public boolean scrolled(float amountX, float amountY) {
            if (mode != Mode.KEYBINDS || amountY == 0f || !Float.isFinite(amountY)) return false;
            if (!MenuButton.inKeybindList(mouseX, mouseY) && !MenuButton.KEYBIND_SCROLL.contains(mouseX, mouseY, 0)) return false;
            if (menuPointer == -1) {
                scrollKeybinds(Math.round(keybindScroll + amountY * MenuButton.LEFT.height), false);
                menuHover = menuAt(mouseX, mouseY);
                if (menuHover != null) showMenuFocus = false;
            }
            return true;
        }

        @Override public boolean touchDown(int x, int y, int index, int button) {
            if (button != Input.Buttons.LEFT) return false;
            keyboardPress = null;
            activationKey = -1;
            var p = point(x, y);
            mouseX = p.x;
            mouseY = p.y;
            if (menuButtons().length > 0) {
                if (menuPointer != -1) return true;
                menuHover = menuAt(p.x, p.y);
                menuPress = menuHover;
                showMenuFocus = false;
                if (menuPress != null) {
                    menuPointer = index;
                    if (menuPress != MenuButton.KEYBIND_SCROLL) menuFocus = menuPress;
                    dragMenuControl(p.x, p.y, true);
                }
            } else if (!beginInk(p.x, p.y, index) && pointer == -1) click(p.x, p.y);
            return true;
        }

        @Override public boolean touchDragged(int x, int y, int index) {
            var p = point(x, y);
            mouseX = p.x;
            mouseY = p.y;
            if (menuPointer == index && menuPress != null) {
                dragMenuControl(p.x, p.y, false);
                menuHover = menuAt(p.x, p.y);
            }
            moveInk(p.x, p.y, index);
            return true;
        }

        @Override public boolean touchUp(int x, int y, int index, int button) {
            if (button != Input.Buttons.LEFT) return false;
            var p = point(x, y);
            if (menuPointer == index && menuPress != null) {
                var pressed = menuPress;
                menuHover = menuAt(p.x, p.y);
                menuPress = null;
                menuPointer = -1;
                if (pressed == menuHover) activateMenu(pressed);
            }
            endInk(p.x, p.y, index);
            return true;
        }

        @Override public boolean touchCancelled(int x, int y, int index, int button) {
            if (menuPointer == index) clearMenuPointer();
            if (pointer == index) cancelStroke();
            return true;
        }
    }
}
