package com.yogiiefe.inkslayer.screens;

import com.badlogic.gdx.utils.viewport.FitViewport;

/** Fits the 320 × 180 art grid into the physical backbuffer. */
final class PixelArtViewport extends FitViewport {
    static final int ART_WIDTH = 320, ART_HEIGHT = 180;
    static final int UI_WIDTH = 1280, UI_HEIGHT = 720;
    static final int ART_PIXEL = UI_WIDTH / ART_WIDTH;

    PixelArtViewport() {
        super(UI_WIDTH, UI_HEIGHT);
    }

    @Override public void update(int width, int height, boolean centerCamera) {
        if (width <= 0 || height <= 0) return;

        int scale = Math.min(width / ART_WIDTH, height / ART_HEIGHT);
        if (scale == 0) {
            super.update(width, height, centerCamera);
            return;
        }

        int contentWidth = ART_WIDTH * scale;
        int contentHeight = ART_HEIGHT * scale;
        setScreenBounds((width - contentWidth) / 2, (height - contentHeight) / 2, contentWidth, contentHeight);
        apply(centerCamera);
    }
}
