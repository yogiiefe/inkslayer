package com.yogiiefe.inkslayer;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.yogiiefe.inkslayer.recognition.InkPoint;
import com.yogiiefe.inkslayer.screens.GameScreen;
import com.yogiiefe.inkslayer.screens.DisplayMode;
import com.yogiiefe.inkslayer.world.CampaignContent;
import com.yogiiefe.inkslayer.world.FillQuiz;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Predicate;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public final class Main extends Game {
    private final Predicate<DisplayMode> applyDisplay;

    public Main(Predicate<DisplayMode> applyDisplay) {
        this.applyDisplay = applyDisplay;
    }

    @Override public void create() {
        var content = new CampaignContent(Gdx.files.internal("data/campaign.json").readString("UTF-8"));
        var quizzes = FillQuiz.parse(Gdx.files.internal("data/fill-quizzes.tsv").readString("UTF-8"));
        var templates = readStrokes("fill-templates.json");
        if (quizzes.stream().anyMatch(quiz -> !templates.containsKey(quiz.kanji())))
            throw new IllegalArgumentException("Campaign quizzes must each have a drawing template.");
        setScreen(new GameScreen(content, quizzes, templates, readStrokes("glossary-kana.json"), applyDisplay));
    }

    private static LinkedHashMap<String, List<List<InkPoint>>> readStrokes(String filename) {
        var source = new JsonReader().parse(Gdx.files.internal("data/kanji/" + filename));
        var templates = new LinkedHashMap<String, List<List<InkPoint>>>();
        for (var glyph : source) {
            var strokes = new ArrayList<List<InkPoint>>();
            for (var stroke : glyph) {
                var points = new ArrayList<InkPoint>();
                for (var point : stroke) points.add(new InkPoint(point.getDouble(0), point.getDouble(1)));
                strokes.add(List.copyOf(points));
            }
            templates.put(glyph.name(), List.copyOf(strokes));
        }
        return templates;
    }
    @Override public void dispose() { if(getScreen()!=null) getScreen().dispose(); }
}
