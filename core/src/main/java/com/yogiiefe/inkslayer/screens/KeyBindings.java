package com.yogiiefe.inkslayer.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Preferences;

import java.util.EnumMap;

final class KeyBindings {
    enum Action {
        LEFT("Move left", Keys.A, Keys.LEFT), RIGHT("Move right", Keys.D, Keys.RIGHT),
        SUBMIT("Submit / Continue", Keys.ENTER, 0), HELP("Show kanji guide", Keys.H, 0),
        CLEAR("Clear", Keys.R, 0), UNDO("Undo stroke", Keys.Z, 0);

        final String label;
        final int defaultKey, alternate;

        Action(String label, int defaultKey, int alternate) {
            this.label = label;
            this.defaultKey = defaultKey;
            this.alternate = alternate;
        }
    }

    private final Preferences preferences = Gdx.app.getPreferences("inkslayer-keybinds");
    private final EnumMap<Action, Integer> keys = new EnumMap<>(Action.class);

    KeyBindings() {
        for (Action action : Action.values()) {
            int key = preferences.getInteger(action.name(), action.defaultKey);
            keys.put(action, valid(key) ? key : action.defaultKey);
        }
    }

    boolean matches(Action action, int key) {
        int primary = keys.get(action);
        return key == primary || action.alternate != 0 && primary == action.defaultKey && key == action.alternate;
    }

    boolean held(Action action) {
        return Gdx.input.isKeyPressed(keys.get(action))
            || action.alternate != 0 && keys.get(action) == action.defaultKey && Gdx.input.isKeyPressed(action.alternate);
    }

    String keycap(Action action) {
        String name = Keys.toString(keys.get(action));
        return action == Action.UNDO ? "Ctrl+" + name : name;
    }

    String label(Action action) {
        int primary = keys.get(action);
        String label = keycap(action);
        return primary == action.defaultKey && action.alternate != 0
            ? label + " / " + Keys.toString(action.alternate) : label;
    }

    String bind(Action action, int key) {
        if (!valid(key)) return "That key is reserved. Choose another key.";
        for (Action other : Action.values()) {
            if (other != action && (matches(other, key)
                || key == action.defaultKey && action.alternate != 0 && matches(other, action.alternate)))
                return "Already used by " + other.label + ".";
        }
        keys.put(action, key);
        try {
            preferences.putInteger(action.name(), key).flush();
            return "";
        } catch (com.badlogic.gdx.utils.GdxRuntimeException ex) {
            Gdx.app.error("Keybinds", "Could not save keybind", ex);
            return "Changed for this session. Could not save.";
        }
    }

    String reset() {
        for (Action action : Action.values()) {
            keys.put(action, action.defaultKey);
            preferences.remove(action.name());
        }
        try {
            preferences.flush();
            return "";
        } catch (com.badlogic.gdx.utils.GdxRuntimeException ex) {
            Gdx.app.error("Keybinds", "Could not save default keybinds", ex);
            return "Defaults restored for this session.";
        }
    }

    private static boolean valid(int key) {
        return key > 0 && key <= Keys.MAX_KEYCODE && Keys.toString(key) != null && switch (key) {
            case Keys.ESCAPE, Keys.SPACE, Keys.BACKSPACE, Keys.CONTROL_LEFT, Keys.CONTROL_RIGHT, Keys.SHIFT_LEFT, Keys.SHIFT_RIGHT,
                Keys.ALT_LEFT, Keys.ALT_RIGHT, Keys.SYM -> false;
            default -> true;
        };
    }
}
