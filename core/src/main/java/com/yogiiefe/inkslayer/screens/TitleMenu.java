package com.yogiiefe.inkslayer.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;

/** Main menu layout and pixel art, drawn in the game's 1280 × 720 UI viewport. */
final class TitleMenu {
    enum Option {
        NEW("New", 352), SAVE("Save", 278), SETTINGS("Settings", 204), EXIT("Exit", 130);

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
    private static final Color HORIZON = Color.valueOf("1c1c1c");
    private static final Color FAR_RIDGE = Color.valueOf("343434");
    private static final Color NEAR_RIDGE = Color.valueOf("242424");
    private static final Color SHORE = Color.valueOf("181818");
    private static final Color EDGE = Color.valueOf("3d3d3d");
    private static final Color WATER = Color.valueOf("131313");
    private static final Color RIPPLE = Color.valueOf("242424");
    private static final Color MIST = new Color(0.34f, 0.34f, 0.34f, 0.15f);
    private static final Color PLUM = Color.valueOf("525252");
    private static final Color CYAN = Color.valueOf("d1d1d1");
    private static final Color WHITE = Color.valueOf("ececec");
    private static final Color MUTED = Color.valueOf("aeaeae");
    private static final Color MOON = Color.valueOf("666666");
    private static final Color STAMP = Color.valueOf("777777");
    private static final int[] FAR_PEAKS = {
        0, 360, 72, 376, 156, 468, 188, 452, 240, 392, 268, 404, 388, 288,
        480, 248, 560, 220, 700, 224, 784, 272, 848, 304, 920, 400,
        972, 372, 1080, 508, 1104, 496, 1172, 416, 1216, 444, 1280, 388
    };
    private static final int[] NEAR_PEAKS = {
        0, 288, 52, 316, 116, 384, 172, 328, 220, 348, 296, 272, 368, 228,
        480, 192, 584, 172, 684, 168, 780, 188, 904, 240, 972, 312,
        1032, 280, 1124, 364, 1184, 328, 1280, 288
    };
    private static final int[] SHORELINE = {
        0, 204, 72, 192, 132, 176, 188, 180, 264, 152, 392, 140, 536, 132,
        708, 132, 852, 144, 936, 152, 1016, 172, 1072, 168, 1160, 192, 1280, 216
    };
    private static final int[] LEFT_BANK = {0, 56, 52, 52, 116, 32, 172, 20, 264, 12};
    private static final int[] RIGHT_BANK = {1000, 12, 1120, 24, 1192, 44, 1280, 60};
    private static final String[] LOGO = {
        "11111 10001 10001 01111 10000 01110 10001 11111 11110 11",
        "01110 11001 10010 11000 11000 11001 11001 11000 11001 11",
        "01110 11001 10100 11000 11000 11001 01010 11000 11001 11",
        "01110 10101 11100 01110 11000 11111 00100 11110 11110 11",
        "01110 10011 10100 00011 11000 11001 00100 11000 10100 11",
        "01110 10011 10010 00011 11000 11001 00100 11000 10010 00",
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

    void draw(BitmapFont menuFont, BitmapFont small, Option selected, Option hovered, double time) {
        backdrop(time);
        inspectionSeal();

        for (Option option : Option.OPTIONS) {
            int x = Option.X;
            int y = option.y;
            menuFont.setColor(option == hovered ? Color.WHITE : option == selected ? WHITE : MUTED);
            menuFont.draw(batch, option.label, x, y + (Option.HEIGHT + menuFont.getCapHeight()) / 2,
                Option.WIDTH, Align.center, false);
        }
        small.setColor(MUTED);
        small.draw(batch, BUILD, 24, 36, 1232, Align.right, false);
    }

    private void inspectionSeal() {
        box(249, 477, 796, 166, VOID);
        box(242, 484, 796, 166, STAMP);
        box(250, 492, 780, 150, SKY);
        box(257, 499, 766, 136, EDGE);
        box(261, 503, 758, 128, SKY);

        wordmark(4, -4, PLUM);
        wordmark(0, 0, WHITE);

        // One corner of the inspection seal has split away.
        box(1002, 484, 36, 21, SKY);
        box(1014, 505, 24, 12, SKY);
        box(1026, 517, 12, 9, SKY);
        box(1023, 487, 13, 5, STAMP);
        box(1045, 475, 9, 6, STAMP);
        box(1059, 465, 5, 5, EDGE);

        // The protagonist's pen stroke cuts across the bottom of the seal.
        for (int i = 0; i < 24; i++) {
            int height = Math.min(11, Math.min(4 + i, 27 - i));
            box(532 + i * 9, 464 + (i / 3) * 3, 10, height, i < 3 ? PLUM : CYAN);
        }
        box(761, 489, 6, 4, CYAN);
        box(774, 494, 4, 4, MUTED);
        box(519, 458, 6, 4, PLUM);
    }

    private void wordmark(int dx, int dy, Color ink) {
        for (int row = 0; row < LOGO.length; row++) {
            for (int col = 0; col < LOGO[row].length(); col++) {
                if (LOGO[row].charAt(col) == '1') {
                    box(285 + col * 13 + dx, 609 - row * 15 + dy, 13, 15, ink);
                }
            }
        }
    }

    private void backdrop(double time) {
        box(0, 0, 1280, 720, SKY);
        box(0, 140, 1280, 276, HORIZON);
        box(0, 416, 1280, 64, SHORE);

        disk(158, 549, 60, MOON);
        disk(179, 567, 56, SKY);
        for (int i = 0; i < 40; i++) {
            int x = (i * 307 + 28) % 1280;
            int y = 452 + i * 37 % 240;
            box(x, y, i % 9 == 0 ? 4 : 2, 2, i % 9 == 0 ? EDGE : FAR_RIDGE);
        }

        box(0, 0, 1280, 140, WATER);
        ridge(FAR_PEAKS, 140, FAR_RIDGE);
        driftMist(time, 328, 9, 230);
        ridge(NEAR_PEAKS, 140, NEAR_RIDGE);
        ridge(SHORELINE, 132, SHORE);
        driftMist(time, 151, 5, 180);

        for (int i = 0; i < 46; i++) {
            int x = Math.floorMod(i * 151 - snap(time * (4 + i % 3), 4), 1376) - 96;
            int y = 12 + i * 47 % 116;
            box(x, y, 16 + i * 17 % 60 / 4 * 4, y < 64 ? 4 : 2, RIPPLE);
        }
        box(0, 0, 1280, 12, VOID);
        ridge(LEFT_BANK, 12, VOID);
        ridge(RIGHT_BANK, 12, VOID);
    }

    private void ridge(int[] points, int base, Color color) {
        for (int i = 0; i < points.length - 2; i += 2) {
            int left = points[i];
            int right = points[i + 2];
            int rise = points[i + 3] - points[i + 1];
            for (int x = left; x < right; x += 4) {
                int height = snap(points[i + 1] + (x - left) * (double) rise / (right - left), 4);
                box(x, base, 4, height - base, color);
            }
        }
    }

    private void driftMist(double time, int y, int speed, int width) {
        for (int i = 0; i < 4; i++) {
            int x = Math.floorMod(i * 371 + snap(time * speed, 4), 1600) - 260;
            int bandY = y - i % 3 * 12;
            box(x, bandY, width, 4, MIST);
            box(x + 24, bandY + 4, width - 68, 4, MIST);
            box(x + 56, bandY - 4, width - 104, 4, MIST);
        }
    }

    private void disk(int x, int y, int radius, Color color) {
        for (int dy = -radius; dy < radius; dy += 4) {
            int half = snap(Math.sqrt(radius * radius - dy * dy), 4);
            box(x - half, y + dy, half * 2, 4, color);
        }
    }

    private static int snap(double value, int grid) {
        return (int) (value / grid) * grid;
    }

    private void box(int x, int y, int width, int height, Color color) {
        batch.setColor(color);
        batch.draw(pixel, x, y, width, height);
    }
}
