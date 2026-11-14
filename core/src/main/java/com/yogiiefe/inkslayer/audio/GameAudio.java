package com.yogiiefe.inkslayer.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;

import java.util.EnumMap;
import java.util.Locale;

/** Owns music streaming, short effects and application-wide volume preferences. */
public final class GameAudio implements Disposable {
    public enum Track { MENU, STAGE1, STAGE2, STAGE3, STAGE4, BOSS, ENDING }
    public enum Cue { UI, STROKE, ACCEPT, RETRY, WATER, FIRE, METAL, WOOD, EARTH, COUNTER, HIT, VICTORY, DEFEAT }
    public enum Channel { MASTER, SFX, BGM }

    private final Preferences preferences = Gdx.app.getPreferences("inkslayer-audio");
    private final EnumMap<Channel, Integer> levels = new EnumMap<>(Channel.class);
    private final EnumMap<Cue, Sound> sounds = new EnumMap<>(Cue.class);
    private final EnumMap<Cue, Long> voices = new EnumMap<>(Cue.class);
    private Track track;
    private Music music;
    private boolean paused;

    public GameAudio() {
        levels.put(Channel.MASTER, loadVolume(Channel.MASTER, 80));
        levels.put(Channel.SFX, loadVolume(Channel.SFX, 80));
        levels.put(Channel.BGM, loadVolume(Channel.BGM, 65));
    }

    public void music(Track next) {
        if (track == next) return;
        if (music != null) {
            music.stop();
            music.dispose();
            music = null;
        }
        track = next;
        if (next == null) return;
        music = Gdx.audio.newMusic(Gdx.files.internal("audio/bgm/" + key(next) + ".wav"));
        music.setLooping(true);
        music.setVolume(gain(Channel.BGM));
        if (!paused) music.play();
    }

    public void play(Cue cue) {
        float volume = gain(Channel.SFX);
        if (paused || volume == 0) return;
        Sound sound = sounds.computeIfAbsent(cue, item ->
            Gdx.audio.newSound(Gdx.files.internal("audio/sfx/" + key(item) + ".wav")));
        Long previous = voices.remove(cue);
        if (previous != null) sound.stop(previous);
        long voice = sound.play(volume);
        if (voice >= 0) voices.put(cue, voice);
    }

    public int volume(Channel channel) {
        return levels.get(channel);
    }

    public void volume(Channel channel, int percent) {
        levels.put(channel, Math.clamp(percent, 0, 100));
        if (music != null) music.setVolume(gain(Channel.BGM));
        float sfxGain = gain(Channel.SFX);
        voices.forEach((cue, voice) -> sounds.get(cue).setVolume(voice, sfxGain));
    }

    public void saveSettings() {
        levels.forEach((channel, level) -> preferences.putInteger(key(channel), level));
        preferences.flush();
    }

    public void pause() {
        paused = true;
        if (music != null) music.pause();
        sounds.values().forEach(Sound::stop);
        voices.clear();
    }

    public void resume() {
        paused = false;
        if (music != null) music.play();
    }

    @Override
    public void dispose() {
        music(null);
        sounds.values().forEach(Sound::dispose);
        sounds.clear();
        voices.clear();
    }

    private int loadVolume(Channel channel, int fallback) {
        return Math.clamp(preferences.getInteger(key(channel), fallback), 0, 100);
    }

    private float gain(Channel channel) {
        return levels.get(Channel.MASTER) * levels.get(channel) / 10_000f;
    }

    private static String key(Enum<?> item) {
        return item.name().toLowerCase(Locale.ROOT);
    }
}
