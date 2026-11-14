package com.yogiiefe.inkslayer.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;

/** Original grayscale sprites rasterized onto a 64-pixel grid at startup. */
final class ActorArt implements Disposable {
    private static final int SIZE = 64;
    private static final Color[] INK = {
        Color.valueOf("282828"), Color.valueOf("464646"), Color.valueOf("727272"),
        Color.valueOf("a0a0a0"), Color.valueOf("d0d0d0"), Color.valueOf("f0f0f0")
    };
    private final Texture[] traveler = {traveler(0), traveler(1), traveler(-1)};
    private final Texture[][] enemy = new Texture[4][10];
    private final Texture unmasked = unmaskedSprite();

    ActorArt() {
        for (int stage = 0; stage < enemy.length; stage++)
            for (int index = 0; index < enemy[stage].length; index++)
                enemy[stage][index] = creature(stage, index);
    }

    void player(SpriteBatch batch, float x, float floor, float scale, int facing, int pose) {
        batch.setColor(Color.WHITE);
        batch.draw(traveler[pose], Math.round(x), floor, SIZE * scale, SIZE * scale, 0, 0, SIZE, SIZE, facing < 0, false);
    }

    void enemy(SpriteBatch batch, float x, float floor, int stage, int index, int lift) {
        batch.setColor(Color.WHITE);
        batch.draw(enemy[stage][index], Math.round(x), floor + lift, 256, 256);
    }

    void unmasked(SpriteBatch batch, float x, float floor) {
        batch.setColor(Color.WHITE);
        batch.draw(unmasked, x, floor, 256, 256);
    }

    private static Texture unmaskedSprite() {
        var p = canvas();
        elemental(p, 0);
        shape(p, 1, 12,17, 22,10, 37,11, 45,19, 42,32, 30,38, 20,34, 13,27);
        shape(p, 3, 16,21, 23,16, 36,16, 42,21, 39,29, 31,35, 23,30, 18,28);
        rect(p, 0, 23, 23, 3, 2);
        rect(p, 0, 34, 22, 3, 2);
        rect(p, 1, 27, 29, 6, 1);
        shape(p, 4, 2,54, 9,51, 17,53, 19,59, 10,63, 3,60);
        rect(p, 0, 6, 55, 4, 1);
        rect(p, 0, 12, 55, 4, 1);
        return texture(p);
    }

    private static Texture traveler(int stride) {
        var p = canvas();
        shape(p, 1, 35,22, 17,25, 7,33, 0,35, 10,24, 23,19, 37,17);
        shape(p, 3, 36,20, 18,24, 4,33, 11,26, 23,19, 35,17);
        shape(p, 1, 25,35, 39,36, 42,52, 36+stride*5,60, 27+stride*5,60, 29,49, 18-stride*5,57, 10-stride*5,57, 13,51);
        rect(p, 3, 26+stride*5, 59, 13, 4);
        rect(p, 2, 10-stride*5, 56, 12, 4);
        shape(p, 2, 25,22, 39,22, 43,34, 39,39, 43,51, 32,49, 29,44, 23,50, 17,45);
        shape(p, 3, 26,24, 33,24, 31,39, 24,46, 20,44);
        shape(p, 1, 36,25, 41,26, 43,35, 39,38, 41,47, 33,46);
        rect(p, 4, 23, 35, 16, 3);
        rect(p, 1, 30, 35, 4, 4);
        shape(p, 1, 24,8, 30,4, 39,4, 46,10, 48,17, 43,24, 32,27, 24,21, 21,15);
        shape(p, 3, 24,10, 30,6, 39,6, 43,10, 33,9, 28,16, 23,16);
        shape(p, 4, 33,12, 43,12, 46,17, 43,22, 35,22, 31,17);
        rect(p, 0, 40, 15, 3, 2);
        shape(p, 3, 29,24, 40,22, 46,20, 44,26, 38,30, 31,28);
        shape(p, 2, 36,26, 41,26, 45,32, 52,30, 54,34, 43,39, 36,34);
        rect(p, 4, 49, 30, 5, 4);
        shape(p, 5, 47,37, 54,24, 59,19, 59,23, 51,38);
        return texture(p);
    }

    private static Texture creature(int stage, int index) {
        var p = canvas();
        int type = index % 5;
        switch (stage) {
            case 0 -> elemental(p, type);
            case 1 -> familiar(p, type);
            case 2 -> wisp(p, type);
            case 3 -> inspector(p, type, index == 9);
            default -> throw new IllegalArgumentException("Unknown stage " + stage);
        }
        if (index >= 5 && !(stage == 3 && index == 9)) {
            shape(p, 1, 27,31, 34,36, 30,47, 23,42);
            rect(p, 4, 27, 36, 3, 6);
        }
        return texture(p);
    }

    private static void elemental(Pixmap p, int type) {
        shape(p, 1, 6,47, 9,33, 17,29, 16,18, 23,7, 39,9, 46,20, 45,29, 54,35, 59,49, 53,61, 35,61, 31,54, 25,61, 8,61);
        switch (type) {
            case 0 -> shape(p, 3, 14,27, 13,18, 22,21, 22,10, 34,2, 31,15, 42,10, 39,23, 47,30);
            case 1 -> shape(p, 3, 15,25, 16,10, 23,5, 42,5, 47,12, 46,27, 37,20, 29,20);
            case 2 -> shape(p, 3, 16,27, 7,7, 19,16, 23,4, 30,16, 44,4, 41,16, 54,12, 47,26);
            case 3 -> shape(p, 3, 12,27, 12,17, 22,8, 40,8, 48,17, 48,27, 36,23, 21,23);
            default -> shape(p, 3, 17,28, 15,20, 24,7, 36,4, 48,14, 43,24, 50,31);
        }
        shape(p, 2, 8,47, 12,35, 22,31, 25,38, 16,47, 17,54, 10,56);
        shape(p, 2, 37,36, 43,31, 51,37, 55,53, 49,57, 46,47);
        shape(p, 3, 21,30, 28,28, 39,30, 43,41, 37,53, 25,51, 18,41);
        shape(p, 4, 17,20, 25,15, 39,16, 44,23, 39,32, 30,35, 18,30, 12,25);
        mask(p, 18, 23);
        rect(p, 5, 27, 40, 8, 2);
        rect(p, 4, 30, 37, 2, 9);
    }

    private static void familiar(Pixmap p, int type) {
        if (type == 2 || type == 3) {
            shape(p, 1, 3,27, 10,17, 26,16, 40,25, 55,17, 60,20, 57,34, 61,46, 48,44, 37,40, 27,47, 13,43, 7,35);
            shape(p, 3, 5,28, 13,19, 25,19, 37,27, 49,24, 55,20, 52,33, 55,42, 39,36, 27,42, 14,38);
            if (type == 2) {
                shape(p, 2, 17,29, 30,8, 51,5, 38,17, 47,17, 30,33, 43,43, 20,36);
                shape(p, 4, 10,25, 0,30, 13,33, 18,28);
                rect(p, 2, 18, 44, 5, 12);
                rect(p, 3, 8, 55, 30, 5);
            } else {
                shape(p, 2, 21,23, 34,19, 43,25, 33,32, 38,43, 26,43, 21,35);
                rect(p, 4, 5, 48, 4, 4);
                rect(p, 3, 13, 56, 3, 3);
            }
            rect(p, 0, 13, 25, 5, 4);
            rect(p, 5, 13, 25, 2, 2);
            return;
        }
        shape(p, 1, 18,27, 37,25, 48,31, 57,22, 62,25, 59,37, 51,40, 53,52, 50,61, 41,61, 42,48, 28,46, 25,60, 16,60, 16,48, 13,43);
        shape(p, 3, 20,31, 36,29, 48,35, 47,44, 36,46, 19,42);
        shape(p, 2, 30,42, 46,42, 49,53, 46,57, 43,50, 26,48, 23,57, 19,57, 20,47);
        if (type == 4) shape(p, 2, 10,11, 18,3, 28,6, 35,13, 34,27, 40,37, 23,36, 17,28, 8,29, 4,24);
        else shape(p, 2, 6,9, 12,15, 21,14, 28,7, 34,20, 30,33, 19,40, 8,34, 2,29);
        shape(p, 4, 7,21, 14,17, 24,18, 28,23, 22,34, 12,34, 5,29);
        rect(p, 0, 11, 22, 4, 2);
        rect(p, 0, 21, 21, 4, 2);
        rect(p, 1, 7, 28, 5, 3);
        rect(p, 5, 23, 34, 6, 2);
        if (type == 1) rect(p, 4, 54, 18, 5, 10);
    }

    private static void wisp(Pixmap p, int type) {
        shape(p, 1, 7,37, 9,23, 21,15, 24,7, 36,4, 46,13, 53,20, 56,32, 50,40, 44,42, 43,50, 31,59, 33,48, 22,53, 22,42, 14,47);
        shape(p, 2, 8,32, 19,25, 22,14, 33,8, 45,13, 49,22, 49,34, 42,42, 31,45, 22,39, 13,40);
        shape(p, 4, 14,23, 21,15, 33,14, 41,18, 43,27, 37,36, 24,38, 15,33, 10,28);
        mask(p, 15, 24);
        switch (type) {
            case 0 -> { rect(p, 3, 14, 46, 3, 9); rect(p, 5, 26, 51, 3, 8); rect(p, 3, 42, 46, 3, 10); }
            case 1 -> { rect(p, 4, 4, 44, 18, 2); rect(p, 3, 1, 49, 13, 2); rect(p, 4, 42, 10, 17, 2); }
            case 2 -> { rect(p, 5, 13, 49, 5, 5); rect(p, 3, 27, 55, 4, 4); rect(p, 5, 47, 46, 4, 4); }
            case 3 -> shape(p, 3, 34,37, 26,48, 34,48, 28,59, 43,44, 35,44, 43,37);
            default -> { rect(p, 4, 49, 8, 6, 2); rect(p, 4, 51, 6, 2, 6); rect(p, 3, 8, 48, 3, 3); }
        }
    }

    private static void inspector(Pixmap p, int type, boolean boss) {
        shape(p, 1, 17,27, 26,21, 40,22, 49,30, 46,44, 55,61, 31,63, 25,57, 19,63, 8,59, 18,42, 11,36);
        shape(p, 2, 22,27, 31,28, 26,42, 20,57, 11,58, 19,37, 14,34);
        shape(p, 3, 33,27, 40,26, 43,35, 40,43, 47,58, 31,60, 28,53);
        shape(p, 4, 26,25, 31,27, 28,42, 25,46, 21,43);
        shape(p, 4, 36,27, 41,26, 46,42, 43,46, 38,41);
        shape(p, 1, 17,8, 27,3, 39,4, 46,11, 49,13, 42,18, 21,18, 13,13);
        shape(p, 3, 15,12, 27,11, 30,5, 39,6, 44,12, 49,13, 41,15, 19,16);
        shape(p, 4, 20,17, 28,14, 40,15, 44,20, 40,29, 30,33, 20,28, 16,24);
        mask(p, 21, 21);
        rect(p, 0, 26, 27, 8, 2);
        rect(p, 5, 29, 27, 3, 1);
        shape(p, 0, 5,37, 15,34, 27,36, 29,45, 17,51, 7,48);
        shape(p, 4, 6,37, 16,35, 25,37, 26,43, 17,48, 8,46);
        rect(p, 2, 15, 37, 2, 10);
        for (int i = 0; i < 3; i++) rect(p, 2, 9, 39 + i * 2, 4, 1);
        if (boss) {
            shape(p, 3, 17,11, 18,3, 24,8, 29,1, 34,8, 41,3, 45,11);
            rect(p, 5, 28, 11, 5, 3);
            shape(p, 4, 44,28, 51,25, 56,33, 52,41, 47,35);
            rect(p, 5, 47, 30, 5, 1);
        } else {
            rect(p, 3, 31, 7, 3 + type, 3);
        }
    }

    private static void mask(Pixmap p, int x, int y) {
        rect(p, 0, x, y, 6, 3);
        rect(p, 0, x + 13, y, 6, 3);
        rect(p, 5, x, y, 2, 1);
        rect(p, 5, x + 13, y, 2, 1);
    }

    private static Pixmap canvas() { return new Pixmap(SIZE, SIZE, Pixmap.Format.RGBA8888); }

    private static void rect(Pixmap p, int shade, int x, int y, int width, int height) {
        p.setColor(INK[shade]);
        p.fillRectangle(x, y, width, height);
    }

    private static void shape(Pixmap p, int shade, int... coordinates) {
        p.setColor(INK[shade]);
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                boolean inside = false;
                for (int i = 0, j = coordinates.length - 2; i < coordinates.length; j = i, i += 2) {
                    int ax = coordinates[i], ay = coordinates[i + 1], bx = coordinates[j], by = coordinates[j + 1];
                    if ((ay > y + 0.5) != (by > y + 0.5)
                        && x + 0.5 < (bx - ax) * (y + 0.5 - ay) / (by - ay) + ax) inside = !inside;
                }
                if (inside) p.drawPixel(x, y);
            }
        }
    }

    private static Texture texture(Pixmap p) {
        try {
            var sprite = new Texture(p);
            sprite.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            return sprite;
        } finally {
            p.dispose();
        }
    }

    @Override public void dispose() {
        unmasked.dispose();
        for (Texture frame : traveler) frame.dispose();
        for (Texture[] stage : enemy) for (Texture sprite : stage) sprite.dispose();
    }
}
