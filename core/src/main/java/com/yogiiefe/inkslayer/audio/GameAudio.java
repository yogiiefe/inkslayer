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
    public enum Track { MENU, STAGE1, STAGE2, STAGE3, STAGE4, BOSS, STORY }
    public enum Cue { UI, STROKE, ACCEPT, RETRY, VICTORY, SKIP }
    public enum Channel { MASTER, SFX, BGM }

    private static final float STORY_FADE_SECONDS = 1f;
    private final Preferences preferences = Gdx.app.getPreferences("inkslayer-audio");
    private final EnumMap<Channel, Integer> levels = new EnumMap<>(Channel.class);
    private final EnumMap<Cue, Sound> sounds = new EnumMap<>(Cue.class);
    private final EnumMap<Cue, Long> voices = new EnumMap<>(Cue.class);
    private Track track;
    private Track targetTrack;
    private Music music;
    private float musicEnvelope = 1f;
    private boolean fadingMusic;
    private boolean paused;

    public GameAudio() {
        levels.put(Channel.MASTER, loadVolume(Channel.MASTER, 80));
        levels.put(Channel.SFX, loadVolume(Channel.SFX, 80));
        levels.put(Channel.BGM, loadVolume(Channel.BGM, 65));
    }

    public void music(Track next) {
        if (targetTrack == next) return;
        boolean shouldFade = next == Track.STORY || track == Track.STORY
            || targetTrack == Track.STORY || fadingMusic;
        targetTrack = next;
        if (!shouldFade) {
            startMusic(next, 1f);
            fadingMusic = false;
            return;
        }
        if (music == null) startMusic(next, 0f);
        fadingMusic = music != null;
    }

    public void update(float delta) {
        if (paused || !fadingMusic || !Float.isFinite(delta) || delta <= 0) return;
        float direction = track == targetTrack ? 1f : -1f;
        musicEnvelope = Math.clamp(musicEnvelope + direction * delta / STORY_FADE_SECONDS, 0f, 1f);
        music.setVolume(gain(Channel.BGM) * musicEnvelope);
        if (musicEnvelope == 0f && track != targetTrack) {
            startMusic(targetTrack, 0f);
            fadingMusic = music != null;
        } else if (musicEnvelope == 1f) fadingMusic = false;
    }

    private void startMusic(Track next, float envelope) {
        Music replacement = null;
        if (next != null) {
            String path = next == Track.STORY ? "audio/bgm/TEMPOREX - Numbers (Instrumental).mp3"
                : "audio/bgm/" + key(next) + ".wav";
            replacement = Gdx.audio.newMusic(Gdx.files.internal(path));
            replacement.setLooping(true);
            replacement.setVolume(gain(Channel.BGM) * envelope);
        }
        if (music != null) {
            music.stop();
            music.dispose();
        }
        track = next;
        music = replacement;
        musicEnvelope = envelope;
        if (music != null && !paused) music.play();
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
        if (music != null) music.setVolume(gain(Channel.BGM) * musicEnvelope);
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
        targetTrack = null;
        fadingMusic = false;
        startMusic(null, 0f);
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
