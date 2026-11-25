package com.yogiiefe.inkslayer.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import static com.yogiiefe.inkslayer.screens.PixelArtViewport.ART_HEIGHT;
import static com.yogiiefe.inkslayer.screens.PixelArtViewport.ART_PIXEL;

/** Cumulative pixel frames in writing order, with a faint silhouette of the whole word. */
final class GlossaryWriting implements Disposable {
    private static final int CELL = 32;
    private static final int LETTER_GAP = 2;
    private static final double FRAME_SECONDS = .12, HOLD_SECONDS = 1.4;
    private static final Color GUIDE = Color.valueOf("dedbd7"), INK = Color.valueOf("464646");
    private final Texture sheet = TitleMenu.load("glossary/writing.png");
    private final JsonValue glyphs = new JsonReader().parse(Gdx.files.internal("glossary/writing.json"));

    double duration(String glyph) {
        return duration(glyphs.get(glyph).getInt(1));
    }

    private static double duration(int frames) {
        return frames * FRAME_SECONDS + HOLD_SECONDS;
    }

    void drawWord(SpriteBatch batch, String word, double age, int left, int top, int width, int height) {
        int length = word.length();
        int totalFrames = 0;
        for (int i = 0; i < length; i++)
            totalFrames += glyphs.get(String.valueOf(word.charAt(i))).getInt(1);
        int progress = (int) ((age % duration(totalFrames)) / FRAME_SECONDS);
        int totalGap = (length - 1) * LETTER_GAP;
        int size = Math.min(height, (width - totalGap) / length);
        int x = left + (width - length * size - totalGap) / 2;
        int glyphTop = top + (height - size) / 2;
        for (int i = 0; i < length; i++) {
            var glyph = glyphs.get(String.valueOf(word.charAt(i)));
            drawGlyph(batch, glyph, progress, x, glyphTop, size);
            progress -= glyph.getInt(1);
            x += size + LETTER_GAP;
        }
        batch.setColor(Color.WHITE);
    }

    void drawGuide(SpriteBatch batch, String glyph, double age, int x, int top, int size) {
        drawGlyph(batch, glyphs.get(glyph), (int) (age / FRAME_SECONDS), x, top, size);
        batch.setColor(Color.WHITE);
    }

    private void drawGlyph(SpriteBatch batch, JsonValue glyph, int progress, int x, int top, int size) {
        int first = glyph.getInt(0), count = glyph.getInt(1);
        if (progress < count) frame(batch, first + count - 1, x, top, size, GUIDE);
        if (progress > 0) frame(batch, first + Math.min(progress, count) - 1, x, top, size, INK);
    }

    private void frame(SpriteBatch batch, int frame, int x, int top, int size, Color color) {
        int columns = sheet.getWidth() / CELL;
        batch.setColor(color);
        batch.draw(sheet, x * ART_PIXEL, (ART_HEIGHT - top - size) * ART_PIXEL, size * ART_PIXEL, size * ART_PIXEL,
            frame % columns * CELL, frame / columns * CELL, CELL, CELL, false, false);
    }

    @Override public void dispose() { sheet.dispose(); }
}
