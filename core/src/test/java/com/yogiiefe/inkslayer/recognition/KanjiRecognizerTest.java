package com.yogiiefe.inkslayer.recognition;

import com.badlogic.gdx.utils.JsonReader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KanjiRecognizerTest {
    private static final Map<String, List<List<InkPoint>>> templates = new LinkedHashMap<>();
    private static KanjiRecognizer recognizer;

    @BeforeAll
    static void loadTemplates() throws Exception {
        Path path = Path.of(System.getProperty("inkslayer.assets"), "data/kanji/templates.json");
        var data = new JsonReader().parse(Files.readString(path));
        for (var entry : data) {
            var strokes = new ArrayList<List<InkPoint>>();
            for (var stroke : entry) {
                var points = new ArrayList<InkPoint>();
                for (var point : stroke) {
                    points.add(new InkPoint(point.getDouble(0), point.getDouble(1)));
                }
                strokes.add(points);
            }
            templates.put(entry.name, strokes);
        }
        recognizer = KanjiRecognizer.fromTemplates(templates, 0.145);
    }

    @Test
    void identifiesEveryCampaignTemplateAfterMouseQuantization() {
        assertEquals(Set.of("水", "火", "金", "木", "土", "犬", "猫", "鳥", "魚", "馬",
            "雨", "風", "雪", "雲", "空"), templates.keySet());
        for (var entry : templates.entrySet()) {
            Drawing drawing = mouseTrace(entry.getValue(), 1.2);
            assertEquals(entry.getValue().size(), drawing.count());
            var match = recognizer.match(drawing.snapshot(), true, templates.keySet());
            assertEquals(entry.getKey(), match.id(), entry.getKey() + ": " + match.result());
        }
    }

    @Test
    void knownButUnavailableCharacterCannotBeSubstitutedForAvailableGlyph() {
        var match = recognizer.match(templates.get("木"), false, Set.of("水", "火"));
        assertNull(match.id());
        assertFalse(match.result().accepted());
        assertTrue(match.result().message().contains("木"));
    }

    @Test
    void identicalCandidatesProduceAnAmbiguousRejection() {
        var water = templates.get("水");
        var collision = KanjiRecognizer.fromTemplates(Map.of("水", water, "火", water), 0.145);
        var match = collision.match(water, false, Set.of("水", "火"));
        assertNull(match.id());
        assertFalse(match.result().accepted());
        assertTrue(match.result().message().contains("similar"));
    }

    @Test
    void coachingDoesNotConfuseOrderWithShapeOnLongerCharacters() {
        var gold = new ArrayList<>(templates.get("金"));
        Collections.swap(gold, 0, 4);
        var gentle = recognizer.match(gold, false, templates.keySet());
        assertEquals("金", gentle.id());
        assertFalse(gentle.result().orderCorrect());
        assertNull(recognizer.match(gold, true, templates.keySet()).id());
    }

    private static Drawing mouseTrace(List<List<InkPoint>> template, double scale) {
        var drawing = new Drawing();
        for (var stroke : template) {
            InkPoint start = stroke.getFirst();
            drawing.begin(Math.round(start.x() * scale) + 48, Math.round(start.y() * scale) + 16);
            for (var point : stroke) {
                drawing.add(Math.round(point.x() * scale) + 48, Math.round(point.y() * scale) + 16);
            }
            drawing.end();
        }
        return drawing;
    }
}
