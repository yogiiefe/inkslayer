package com.yogiiefe.inkslayer.world;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Loads authored campaign data and checks references before play starts. */
public final class CampaignContent {
    public static final String BOSS_ID = "s4-10";
    public static final List<String> ELEMENTS = List.of("WATER", "FIRE", "METAL", "WOOD", "EARTH");
    public static final Map<String, String> GLYPHS = Map.of(
        "WATER", "水", "FIRE", "火", "METAL", "金", "WOOD", "木", "EARTH", "土");
    public static final Map<String, String> COUNTERS = Map.of(
        "FIRE", "WATER", "METAL", "FIRE", "WOOD", "METAL", "EARTH", "WOOD", "WATER", "EARTH");

    public record Encounter(String id, String name, String element, List<String> attacks,
                            int health, int damage, String lessonId) {
        public Encounter {
            attacks = List.copyOf(attacks);
        }
    }

    public record Stage(String name, List<Encounter> encounters) {
        public Stage {
            encounters = List.copyOf(encounters);
        }
    }

    public record Lesson(String id, String glyph, String reading, String meaning, String katakana,
                         String romaji, int strokeCount) {}
    public record KanaWord(String text, String meaning, String romaji, String note) {}

    public final List<Stage> stages;
    public final Map<String, Lesson> lessons;
    public final List<KanaWord> katakana;
    public final List<List<String>> bossRounds;
    public final List<List<String>> bossAttacks;
    public final List<String> opening;
    public final List<String> ending;

    public CampaignContent(String json) {
        JsonValue root = new JsonReader().parse(json);
        require(root.getInt("contentVersion") == 1, "Unsupported campaign content version");
        katakana = readKana(array(root, "katakana", 8, 8));
        lessons = readLessons(array(root, "lessons", 15, 15), katakana);
        stages = readStages(array(root, "stages", 4, 4), lessons);
        opening = strings(array(root, "opening", 5, 7));
        ending = strings(array(root, "ending", 5, 7));

        var rounds = new ArrayList<List<String>>();
        var attackRounds = new ArrayList<List<String>>();
        var tested = new HashSet<String>();
        for (JsonValue entry : array(root, "bossRounds", 3, 3)) {
            List<String> characters = strings(array(entry, "characters", 5, 5));
            for (String glyph : characters) {
                require(lessons.containsKey(glyph) && tested.add(glyph), "Invalid boss question: " + glyph);
            }
            rounds.add(characters);
            attackRounds.add(strings(array(entry, "attacks", 1, 2)));
        }
        require(tested.equals(lessons.keySet()), "Boss must test every taught character");
        List<String> orderedAttacks = attackRounds.stream().flatMap(List::stream).toList();
        require(orderedAttacks.equals(stages.getLast().encounters().getLast().attacks())
            && new HashSet<>(orderedAttacks).equals(Set.copyOf(ELEMENTS)), "Invalid boss attack order");
        bossRounds = List.copyOf(rounds);
        bossAttacks = List.copyOf(attackRounds);
    }

    private static List<KanaWord> readKana(JsonValue entries) {
        var words = new ArrayList<KanaWord>();
        var used = new HashSet<String>();
        for (JsonValue entry : entries) {
            String word = field(entry, "text");
            require(word.matches("[ァ-ヺー]+") && used.add(word), "Invalid or repeated katakana: " + word);
            words.add(new KanaWord(word, field(entry, "meaning"), field(entry, "romaji"), entry.getString("note", "")));
        }
        return List.copyOf(words);
    }

    private static Map<String, Lesson> readLessons(JsonValue entries, List<KanaWord> katakana) {
        var result = new LinkedHashMap<String, Lesson>();
        for (JsonValue entry : entries) {
            String glyph = field(entry, "glyph");
            String reading = field(entry, "reading");
            String kana = entry.getString("katakana", "");
            require(glyph.matches("\\p{IsHan}") && !result.containsKey(glyph), "Invalid or repeated Kanji: " + glyph);
            require(reading.matches("\\p{IsHiragana}+"), "Invalid hiragana reading: " + reading);
            require(kana.isEmpty() || katakana.stream().anyMatch(word -> word.text().equals(kana)),
                "Unknown katakana lesson: " + kana);
            int count = entry.getInt("strokeCount");
            require(count >= 1 && count <= 12, "Invalid stroke count: " + glyph);
            require(Integer.parseInt(field(entry, "codePoint").replaceFirst("^U\\+", ""), 16) == glyph.codePointAt(0),
                "Code point does not match glyph: " + glyph);
            result.put(glyph, new Lesson(glyph, glyph, reading, field(entry, "meaning"), kana, field(entry, "romaji"), count));
        }
        require(result.keySet().containsAll(GLYPHS.values()), "Missing one or more elemental Kanji");
        return Collections.unmodifiableMap(result);
    }

    private static List<Stage> readStages(JsonValue entries, Map<String, Lesson> lessons) {
        var result = new ArrayList<Stage>();
        var usedIds = new HashSet<String>();
        var taught = new HashSet<String>();
        for (JsonValue stage : entries) {
            var encounters = new ArrayList<Encounter>();
            for (JsonValue entry : array(stage, "encounters", 10, 10)) {
                String id = field(entry, "id");
                String element = field(entry, "element");
                String lesson = entry.getString("lessonId", "");
                boolean finalEncounter = result.size() == 3 && encounters.size() == 9;
                require(usedIds.add(id) && BOSS_ID.equals(id) == finalEncounter, "Invalid encounter ID: " + id);
                if (!finalEncounter) {
                    require(lessons.containsKey(lesson), id + " references an unknown lesson: " + lesson);
                    boolean introduction = result.size() < 3 && encounters.size() < 5;
                    require(taught.add(lesson) == introduction, id + " introduces or reviews a lesson out of order");
                }
                require(counterAvailable(taught, element), id + " needs an untaught offensive counter");
                List<String> attacks = strings(array(entry, "attacks", 1, 5));
                for (String attack : attacks) {
                    require(counterAvailable(taught, attack), id + " needs an untaught defensive counter");
                }
                int health = entry.getInt("health");
                int damage = entry.getInt("damage");
                require(health > 0 && damage > 0, id + " has invalid combat parameters");
                encounters.add(new Encounter(id, field(entry, "name"), element, attacks, health, damage, lesson));
            }
            result.add(new Stage(field(stage, "name"), encounters));
        }
        require(taught.equals(lessons.keySet()), "Every lesson must be taught before the final examination");
        return List.copyOf(result);
    }

    private static boolean counterAvailable(Set<String> taught, String targetElement) {
        String counter = COUNTERS.get(targetElement);
        return counter != null && taught.contains(GLYPHS.get(counter));
    }

    private static JsonValue array(JsonValue parent, String name, int min, int max) {
        JsonValue value = parent.get(name);
        require(value != null && value.isArray() && value.size >= min && value.size <= max,
            "Invalid array: " + name);
        return value;
    }

    private static List<String> strings(JsonValue array) {
        var values = new ArrayList<String>();
        for (JsonValue value : array) {
            require(value.isString() && !value.asString().isBlank(), "Invalid text entry in " + array.name);
            values.add(value.asString());
        }
        return List.copyOf(values);
    }

    private static String field(JsonValue parent, String name) {
        JsonValue value = parent.get(name);
        require(value != null && value.isString() && !value.asString().isBlank(), "Invalid field: " + name);
        return value.asString();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
