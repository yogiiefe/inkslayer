package com.yogiiefe.inkslayer;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.yogiiefe.inkslayer.recognition.InkPoint;
import com.yogiiefe.inkslayer.screens.GameScreen;
import com.yogiiefe.inkslayer.world.CampaignContent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public final class Main extends Game {
    @Override public void create() {
        var content = new CampaignContent(Gdx.files.internal("data/campaign.json").readString("UTF-8"));
        var source = new JsonReader().parse(Gdx.files.internal("data/kanji/templates.json"));
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
        if (!templates.keySet().equals(content.lessons.keySet()))
            throw new IllegalArgumentException("Campaign lessons must each have a drawing template.");
        setScreen(new GameScreen(content, templates));
    }
    @Override public void dispose() { if(getScreen()!=null) getScreen().dispose(); }
}
