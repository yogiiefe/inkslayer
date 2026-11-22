package com.yogiiefe.inkslayer.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;

import static com.yogiiefe.inkslayer.screens.PixelArtViewport.*;

final class TitleMenu implements Disposable {
    enum Option {
        NEW(128), SAVE(140), SETTINGS(152), EXIT(164);

        private static final Option[] OPTIONS = values();
        private static final int LABEL_X = 258, LABEL_WIDTH = 45, LABEL_HEIGHT = 8;
        private static final int HIT_X = 255, HIT_WIDTH = 64, HIT_PADDING = 2;
        private final int top;

        Option(int top) {
            this.top = top;
        }

        Option step(int direction) {
            return OPTIONS[Math.floorMod(ordinal() + direction, OPTIONS.length)];
        }

        int arrowTop() { return this == NEW ? top : top + 1; }

        static Option at(float x, float y) {
            for (Option option : OPTIONS) {
                int bottom = ART_HEIGHT - option.top - LABEL_HEIGHT - HIT_PADDING;
                int height = LABEL_HEIGHT + 2 * HIT_PADDING;
                if (GameScreen.hit(x, y, HIT_X * ART_PIXEL, bottom * ART_PIXEL,
                    HIT_WIDTH * ART_PIXEL, height * ART_PIXEL)) return option;
            }
            return null;
        }
    }

    private static final int ARROW_X = 308, ARROW_TRAVEL = 2, ARROW_WIDTH = 4, ARROW_HEIGHT = 7;
    private static final double DIM_SECONDS = 0.14, ARROW_PERIOD = 0.6;
    private final Texture logo = load("INKSLAYER! logo.png");
    private final Texture layout = load("title-menu.png");

    void draw(SpriteBatch batch, Option selected, Option hovered, double hoverAge) {
        batch.setColor(Color.WHITE);
        // Crop the transparent margins and place the artwork at its reference bounds.
        drawRegion(batch, logo, 11, 27, 175, 38, 9, 127);
        drawRegion(batch, layout, 9, 168, 59, 3, 9, 168);
        for (Option option : Option.OPTIONS) {
            float shade = option == hovered ? hoverShade(hoverAge) : 1f;
            batch.setColor(shade, shade, shade, 1f);
            drawRegion(batch, layout, Option.LABEL_X, option.top, Option.LABEL_WIDTH, Option.LABEL_HEIGHT,
                Option.LABEL_X, option.top);
        }
        batch.setColor(Color.WHITE);
        Option active = hovered == null ? selected : hovered;
        int offset = hovered == null ? 0 : hoverArrowOffset(hoverAge);
        drawRegion(batch, layout, ARROW_X, Option.NEW.arrowTop(), ARROW_WIDTH, ARROW_HEIGHT,
            ARROW_X - offset, active.arrowTop());
    }

    static float hoverShade(double hoverAge) {
        return 1f - 0.4f * (float) Math.min(1, hoverAge / DIM_SECONDS);
    }

    static int hoverArrowOffset(double hoverAge) {
        return (int) Math.round(ARROW_TRAVEL * (1 - Math.cos(hoverAge * 2 * Math.PI / ARROW_PERIOD)) / 2);
    }

    static void drawRegion(SpriteBatch batch, Texture texture, int sourceX, int sourceY,
                           int width, int height, int x, int top) {
        batch.draw(texture, x * ART_PIXEL, (ART_HEIGHT - top - height) * ART_PIXEL,
            width * ART_PIXEL, height * ART_PIXEL, sourceX, sourceY, width, height, false, false);
    }

    private static Texture load(String path) {
        var texture = new Texture(Gdx.files.internal(path));
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        return texture;
    }

    @Override public void dispose() {
        layout.dispose();
        logo.dispose();
    }
}
