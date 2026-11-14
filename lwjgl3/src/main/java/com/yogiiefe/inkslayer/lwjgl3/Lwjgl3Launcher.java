package com.yogiiefe.inkslayer.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.yogiiefe.inkslayer.Main;
import com.yogiiefe.inkslayer.save.SaveStore;
import com.yogiiefe.inkslayer.screens.DisplayMode;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) return; // This handles macOS support and helps on Windows.
        createApplication();
    }

    private static Lwjgl3Application createApplication() {
        var display = new DesktopDisplay();
        return new Lwjgl3Application(new Main(display::apply), getDefaultConfiguration());
    }

    private static final class DesktopDisplay {
        private DisplayMode current = DisplayMode.WINDOWED;
        private int width, height, x, y;

        boolean apply(DisplayMode mode) {
            if (mode == current) return true;
            var graphics = (com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics) Gdx.graphics;
            var window = graphics.getWindow();
            var monitor = graphics.getMonitor();
            var desktop = graphics.getDisplayMode(monitor);
            if (current == DisplayMode.WINDOWED) {
                width = graphics.getLogicalWidth();
                height = graphics.getLogicalHeight();
                x = window.getPositionX();
                y = window.getPositionY();
            }
            boolean changed;
            switch (mode) {
                case WINDOWED -> {
                    changed = graphics.setWindowedMode(width, height);
                    if (changed) window.setPosition(x, y);
                }
                case BORDERLESS -> {
                    changed = graphics.setWindowedMode(desktop.width, desktop.height);
                    if (changed) window.setPosition(monitor.virtualX, monitor.virtualY);
                }
                case FULLSCREEN -> changed = graphics.setFullscreenMode(desktop);
                default -> throw new AssertionError(mode);
            }
            if (changed) {
                graphics.setUndecorated(mode == DisplayMode.BORDERLESS);
                current = mode;
            }
            return changed;
        }
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("INKSLAYER!");
        // Keep the settings file beside the save directory, independently of the working directory.
        configuration.setPreferencesConfig(SaveStore.defaultDirectory().toAbsolutePath()
            .resolveSibling("settings").toString().replace('\\', '/') + "/", Files.FileType.Absolute);
        //// Vsync limits the frames per second to what your hardware can display, and helps eliminate
        //// screen tearing. This setting doesn't always work on Linux, so the line after is a safeguard.
        configuration.useVsync(true);
        //// Limits FPS to the refresh rate of the currently active monitor, plus 1 to try to match fractional
        //// refresh rates. The Vsync setting above should limit the actual FPS to match the monitor.
        configuration.setForegroundFPS(Math.max(30, Integer.getInteger("inkslayer.fps", 60)));
        //// If you remove the above line and set Vsync to false, you can get unlimited FPS, which can be
        //// useful for testing performance, but can also be very stressful to some hardware.
        //// You may also need to configure GPU drivers to fully disable Vsync; this can cause screen tearing.

        configuration.setWindowedMode(Math.max(960, Integer.getInteger("inkslayer.width", 1280)), Math.max(540, Integer.getInteger("inkslayer.height", 720)));
        configuration.setWindowSizeLimits(960, 540, -1, -1);
        configuration.setPauseWhenLostFocus(true);
        //// You can change these files; they are in lwjgl3/src/main/resources/ .
        //// They can also be loaded from the root of assets/ .
        configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");

        //// This could improve compatibility with Windows machines with buggy OpenGL drivers, Macs
        //// with Apple Silicon that have to emulate compatibility with OpenGL anyway, and more.
        //// This uses the dependency `com.badlogicgames.gdx:gdx-lwjgl3-angle` to function.
        //// You would need to add this line to lwjgl3/build.gradle , below the dependency on `gdx-backend-lwjgl3`:
        ////     implementation "com.badlogicgames.gdx:gdx-lwjgl3-angle:$gdxVersion"
        //// You can choose to add the following line and the mentioned dependency if you want; they
        //// are not intended for games that use GL30 (which is compatibility with OpenGL ES 3.0).
        //// Know that it might not work well in some cases.
//        configuration.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.ANGLE_GLES20, 0, 0);

        return configuration;
    }
}
