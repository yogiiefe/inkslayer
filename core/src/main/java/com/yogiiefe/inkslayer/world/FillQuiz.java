package com.yogiiefe.inkslayer.world;

import java.util.HashSet;
import java.util.List;

public record FillQuiz(String english, String japanese, String kanji, String reading, String romaji) {
    public static List<FillQuiz> parse(String source) {
        var ids = new HashSet<String>();
        var quizzes = source.lines().filter(line -> !line.isBlank()).map(line -> {
            String[] fields = line.split("\t", -1);
            if (fields.length != 5 || !fields[1].contains("［　］") || fields[4].isBlank()
                || fields[2].codePointCount(0, fields[2].length()) != 1 || !ids.add(fields[2]))
                throw new IllegalArgumentException("Invalid Fill the Blank quiz: " + line);
            return new FillQuiz(fields[0], fields[1], fields[2], fields[3], fields[4]);
        }).toList();
        if (quizzes.size() != 39) throw new IllegalArgumentException("Expected 39 monster quizzes");
        return quizzes;
    }

    public String katakana() {
        return reading.codePoints().collect(StringBuilder::new,
            (buffer, cp) -> buffer.appendCodePoint(cp >= 'ぁ' && cp <= 'ゖ' ? cp + 0x60 : cp),
            StringBuilder::append).toString();
    }

    public String completed() { return japanese.replace("［　］", kanji); }
}
