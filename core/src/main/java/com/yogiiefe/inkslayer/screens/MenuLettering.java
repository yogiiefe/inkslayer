package com.yogiiefe.inkslayer.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import java.util.Locale;

/** The five-pixel alphabet painted in the left margin of savesLayout.png. */
final class MenuLettering implements Disposable {
    private static final int CELL = 6, HEIGHT = 5;
    private final int[] widths = new int[128];
    private final Texture atlas;

    MenuLettering() {
        var source = new Pixmap(Gdx.files.internal("savesLayout.png"));
        var glyphs = new Pixmap(16 * CELL, 8 * HEIGHT, Pixmap.Format.RGBA8888);
        glyphs.setBlending(Pixmap.Blending.None);
        try {
            char letter = 'A';
            for (int top : new int[]{44, 50, 56}) {
                int x = 8;
                while (x < 60) {
                    if (!hasInk(source, x, top)) { x++; continue; }
                    int left = x;
                    while (hasInk(source, x, top)) x++;
                    widths[letter] = x - left;
                    for (int y = 0; y < HEIGHT; y++) {
                        for (int col = left; col < x; col++) {
                            if ((source.getPixel(col, top + y) & 255) != 0)
                                glyphs.drawPixel(atlasX(letter) + col - left, atlasY(letter) + y, 0xffffffff);
                        }
                    }
                    letter++;
                }
            }
            if (letter != 'Z' + 1) throw new IllegalArgumentException("Expected A-Z on the Saves layout");
            widths[' '] = 2;
            // Numerals and key-name punctuation share the sheet's five-pixel baseline.
            String symbols = "0123456789:/+-.?!,'[]=;\\`*()_";
            String[] patterns = {
                "75557","26227","71747","71717","55711","74717","74757","71111","75757","75717",
                "01010","11244","02720","00700","00001","71202","22202","00012","22000",
                "64446","31113","07070","01012","44211","42000","05250","24442","21112","00007"
            };
            for (int i = 0; i < symbols.length(); i++) {
                char c = symbols.charAt(i);
                widths[c] = 3;
                for (int y = 0; y < HEIGHT; y++) {
                    int bits = patterns[i].charAt(y) - '0';
                    for (int x = 0; x < 3; x++) {
                        if ((bits & (4 >> x)) != 0) glyphs.drawPixel(atlasX(c) + x, atlasY(c) + y, 0xffffffff);
                    }
                }
            }
            atlas = new Texture(glyphs);
            atlas.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        } finally {
            source.dispose();
            glyphs.dispose();
        }
    }

    private static boolean hasInk(Pixmap source, int x, int top) {
        for (int y = 0; y < HEIGHT; y++) if ((source.getPixel(x, top + y) & 255) != 0) return true;
        return false;
    }

    private static int atlasX(char c) { return (c % 16) * CELL; }
    private static int atlasY(char c) { return (c / 16) * HEIGHT; }

    private char glyph(char c) { return c < widths.length && widths[c] > 0 ? c : '?'; }

    int measure(String value) {
        return width(value.toUpperCase(Locale.ROOT));
    }

    private int width(String text) {
        int total = -1;
        for (int i = 0; i < text.length(); i++) total += widths[glyph(text.charAt(i))] + 1;
        return Math.max(0, total);
    }

    void wrapped(SpriteBatch batch, String value, int x, int top, int available, Color ink) {
        String line = "";
        for (String word : value.toUpperCase(Locale.ROOT).split(" ")) {
            String next = line.isEmpty() ? word : line + " " + word;
            if (!line.isEmpty() && width(next) > available) {
                draw(batch, line, x, top, available, HEIGHT, ink, true);
                top += HEIGHT + 2;
                line = word;
            } else line = next;
        }
        draw(batch, line, x, top, available, HEIGHT, ink, true);
    }

    void draw(SpriteBatch batch, String value, int x, int top, int available, int height, Color ink, boolean center) {
        String text = value.toUpperCase(Locale.ROOT);
        if (width(text) > available) {
            int end = text.length();
            while (end > 0 && width(text.substring(0, end)) + 12 > available) end--;
            text = text.substring(0, end) + "...";
        }
        if (center) x += (available - width(text)) / 2;
        top += (height - HEIGHT) / 2;
        batch.setColor(ink);
        for (int i = 0; i < text.length(); i++) {
            char c = glyph(text.charAt(i));
            TitleMenu.drawRegion(batch, atlas, atlasX(c), atlasY(c), widths[c], HEIGHT, x, top);
            x += widths[c] + 1;
        }
    }

    @Override public void dispose() { atlas.dispose(); }
}
