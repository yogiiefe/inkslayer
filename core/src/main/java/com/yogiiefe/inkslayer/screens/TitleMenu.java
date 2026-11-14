package com.yogiiefe.inkslayer.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;

/** Main menu layout and pixel art, drawn in the game's 1280 × 720 UI viewport. */
final class TitleMenu {
    enum Option {
        NEW("New", 340), SAVE("Save", 272), SETTINGS("Settings", 204), EXIT("Exit", 136);

        private static final Option[] OPTIONS = values();
        private static final int X = 484;
        private static final int WIDTH = 312;
        private static final int HEIGHT = 56;
        final String label;
        final int y;

        Option(String label, int y) {
            this.label = label;
            this.y = y;
        }

        Option step(int direction) {
            return OPTIONS[Math.floorMod(ordinal() + direction, OPTIONS.length)];
        }

        static Option at(float x, float y) {
            for (Option option : OPTIONS) {
                if (GameScreen.hit(x, y, X, option.y, WIDTH, HEIGHT)) return option;
            }
            return null;
        }
    }

    private static final Color SKY = Color.valueOf("111111");
    private static final Color VOID = Color.valueOf("0a0a0a");
    private static final Color FAR = Color.valueOf("1e1e1e");
    private static final Color STONE = Color.valueOf("272727");
    private static final Color EDGE = Color.valueOf("3d3d3d");
    private static final Color WATER = Color.valueOf("171717");
    private static final Color PINK = Color.valueOf("858585");
    private static final Color PLUM = Color.valueOf("525252");
    private static final Color CYAN = Color.valueOf("d1d1d1");
    private static final Color WHITE = Color.valueOf("ececec");
    private static final Color MUTED = Color.valueOf("aeaeae");
    private static final String[] LOGO = {
        "11111 10001 10001 01111 10000 01110 10001 11111 11110 11",
        "00100 11001 10010 10000 10000 10001 10001 10000 10001 11",
        "00100 11001 10100 10000 10000 10001 01010 10000 10001 11",
        "00100 10101 11000 01110 10000 11111 00100 11110 11110 11",
        "00100 10011 10100 00001 10000 10001 00100 10000 10100 11",
        "00100 10011 10010 00001 10000 10001 00100 10000 10010 00",
        "11111 10001 10001 11110 11111 10001 00100 11111 10001 11"
    };
    private static final String VERSION = TitleMenu.class.getPackage().getImplementationVersion();
    private static final String BUILD = VERSION == null ? "Development build" : "Build " + VERSION;

    private final SpriteBatch batch;
    private final Texture pixel;

    TitleMenu(SpriteBatch batch, Texture pixel) {
        this.batch = batch;
        this.pixel = pixel;
    }

    void draw(BitmapFont body, BitmapFont small, Option selected, Option hovered) {
        backdrop();
        logo(8, -14, VOID);
        logo(4, -8, PLUM);
        logo(0, 0, WHITE);

        box(476, 454, 144, 2, EDGE);
        box(660, 454, 144, 2, EDGE);
        box(632, 452, 16, 6, PLUM);
        box(638, 448, 4, 14, PINK);

        for (Option option : Option.OPTIONS) {
            boolean active = option == (hovered == null ? selected : hovered);
            int x = Option.X;
            int y = option.y;
            box(x + 4, y - 4, Option.WIDTH, Option.HEIGHT, VOID);
            box(x, y, Option.WIDTH, Option.HEIGHT, active ? EDGE : STONE);
            box(x + 2, y + 2, Option.WIDTH - 4, Option.HEIGHT - 4, active ? FAR : SKY);
            if (active) {
                box(x, y, 4, Option.HEIGHT, PINK);
                box(x + 24, y + 26, 12, 4, CYAN);
                box(x + 28, y + 22, 4, 12, CYAN);
                box(x + Option.WIDTH - 24, y + 26, 8, 4, PINK);
            }
            body.setColor(active ? WHITE : MUTED);
            body.draw(batch, option.label, x, y + (Option.HEIGHT + body.getCapHeight()) / 2,
                Option.WIDTH, Align.center, false);
        }
        small.setColor(MUTED);
        small.draw(batch, BUILD, 24, 36, 1232, Align.right, false);
    }

    private void logo(int dx, int dy, Color shadow) {
        for (int row = 0; row < LOGO.length; row++) {
            for (int col = 0; col < LOGO[row].length(); col++) {
                if (LOGO[row].charAt(col) == '1') {
                    Color color = shadow == WHITE && row >= 5 ? (row == 5 ? CYAN : MUTED) : shadow;
                    box(192 + col * 16 - row * 4 + dx, 610 - row * 16 + dy, 16, 16, color);
                }
            }
        }
    }

    private void backdrop() {
        box(0, 0, 1280, 720, SKY);
        box(0, 170, 1280, 138, FAR);
        box(0, 0, 1280, 170, WATER);
        for (int i = 0; i < 64; i++) {
            int x = (i * 307 + 28) % 1280;
            int y = 316 + (i * 37 % 388);
            box(x, y, i % 7 == 0 ? 8 : 4, 2, i % 9 == 0 ? EDGE : FAR);
        }
        for (int i = 0; i < 20; i++) {
            int x = i * 64;
            int h = 24 + (i * 29 % 100);
            box(x, 172, 60, h, SKY);
            box(x + 8, 172 + h, 40, 8, SKY);
            box(x + 12, 194 + h / 4, 4, 12, EDGE);
            box(x + 34, 198 + h / 4, 12, 4, STONE);
        }
        for (int i = 0; i < 63; i++) {
            int x = (i * 151 + 56) % 1280;
            int y = 12 + i * 47 % 152;
            box(x, y, 12 + (i * 17 % 104) / 4 * 4, 2, i % 6 == 0 ? EDGE : FAR);
        }

        ruins(84, 176, 396, false);
        ruins(974, 148, 320, true);

        ledge(0, 88, 340);
        ledge(974, 72, 306);
        ledge(0, 28, 220);
        ledge(1092, 16, 188);

        // A lone traveler and two scarf pixels on the near ledge.
        box(244, 96, 12, 24, FAR);
        box(248, 108, 12, 16, EDGE);
        box(248, 124, 12, 12, STONE);
        box(256, 124, 8, 4, CYAN);
        box(240, 116, 16, 4, PINK);
        box(232, 112, 12, 4, PLUM);
    }

    private void ruins(int x, int y, int h, boolean reverse) {
        box(x - 30, y - 12, 314, 12, VOID);
        box(x - 12, y, 280, 16, STONE);
        column(x, y + 16, h - 16, reverse);
        column(x + 172, y + 16, h - 16, reverse);
        box(x - 16, y + h - 88, 284, 12, VOID);
        box(x - 28, y + h - 30, 308, 22, STONE);
        box(x - 44, y + h - 12, 340, 12, EDGE);
        box(x - 60, y + h, 372, 8, VOID);
        box(x + 20, y + h + 8, 228, 8, VOID);
        box(x + 104, y + h - 80, 36, 60, VOID);
        box(x + 112, y + h - 72, 20, 40, EDGE);
        box(x + 120, y + h - 62, 4, 20, PINK);
        box(x + 116, y + h - 54, 12, 4, PINK);
    }

    private void column(int x, int y, int h, boolean reverse) {
        box(x, y, 64, h, VOID);
        box(x + 8, y + 12, 48, h - 12, STONE);
        int trim = x + (reverse ? 12 : 48);
        box(trim, y + 60, 4, h - 68, EDGE);
        for (int i = 0; i < 4; i++) {
            box(x + 8, y + 32 + i * 48, 48, 4, FAR);
        }
        box(trim, y + 40, 4, 16, CYAN);
        box(trim, y + 32, 4, 4, CYAN);
    }

    private void ledge(int x, int y, int w) {
        box(x, y - 16, w - 16, 16, VOID);
        box(x, y, w, 8, STONE);
        for (int i = 12; i < w - 26; i += 44) {
            box(x + i, y + 4, 20, 4, EDGE);
        }
    }

    private void box(int x, int y, int width, int height, Color color) {
        batch.setColor(color);
        batch.draw(pixel, x, y, width, height);
    }
}
