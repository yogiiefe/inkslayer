import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;

/** Offline authoring tool for the original INKSLAYER synth score and cues. */
class ComposeGameAudio {
    private static final int RATE = 22050;
    private static final double TAU = 2 * Math.PI;
    private static final int MENU_BPM = 105;
    private static final int MENU_STEPS_PER_BAR = 16;
    private static final int MENU_STEP_SAMPLES = RATE * 60 / (MENU_BPM * 4);

    // Bass and upper voicing (MIDI) for the title theme's D-minor progression.
    private static final int[] DM = {38, 53, 57, 62, 64};
    private static final int[] BB = {34, 53, 57, 62, 65};
    private static final int[] F_A = {33, 53, 57, 60, 65};
    private static final int[] C = {36, 52, 55, 60, 62};
    private static final int[] GM = {43, 53, 58, 62, 67};
    private static final int[] F = {41, 52, 57, 60, 65};
    private static final int[] A7 = {45, 52, 55, 61, 64};

    // Each melody event is an onset in sixteenths, a MIDI pitch, and a length in sixteenths.
    private record MenuBar(int[] chord, int... melody) {}

    private static final MenuBar[] MENU_SCORE = {
        // Intro: the hook on its own, with a short answer.
        new MenuBar(DM, 0, 74, 5, 6, 77, 2, 8, 76, 3, 12, 69, 4),
        new MenuBar(BB, 2, 72, 2, 4, 74, 8),
        new MenuBar(GM, 0, 70, 4, 6, 74, 2, 8, 79, 6),
        new MenuBar(A7, 0, 76, 6, 8, 73, 4, 14, 69, 2),
        // A: establish the full theme, then answer it with a different cadence.
        new MenuBar(DM, 0, 74, 5, 6, 77, 2, 8, 76, 3, 12, 69, 4),
        new MenuBar(BB, 2, 72, 2, 4, 74, 4, 8, 77, 6, 14, 76, 2),
        new MenuBar(F_A, 0, 72, 6, 6, 69, 2, 8, 72, 4, 12, 77, 4),
        new MenuBar(C, 0, 76, 4, 4, 74, 2, 6, 72, 6, 14, 69, 2),
        new MenuBar(DM, 0, 74, 5, 6, 77, 2, 8, 76, 3, 12, 69, 4),
        new MenuBar(BB, 2, 72, 2, 4, 74, 4, 8, 77, 4, 12, 81, 4),
        new MenuBar(GM, 0, 79, 6, 6, 77, 2, 8, 74, 3, 12, 70, 4),
        new MenuBar(A7, 0, 69, 4, 4, 73, 2, 6, 76, 6, 14, 77, 2),
        // B: lift into F major, with longer notes and a higher register.
        new MenuBar(F, 0, 81, 8, 8, 79, 4, 12, 77, 4),
        new MenuBar(C, 0, 76, 6, 6, 79, 2, 8, 84, 7),
        new MenuBar(BB, 0, 81, 6, 6, 77, 2, 8, 74, 6),
        new MenuBar(A7, 0, 76, 4, 4, 79, 4, 8, 76, 3, 12, 73, 4),
        // A': restate the hook, bringing the high answer back down.
        new MenuBar(DM, 0, 74, 5, 6, 77, 2, 8, 76, 3, 12, 69, 4),
        new MenuBar(BB, 2, 72, 2, 4, 74, 4, 8, 77, 4, 12, 81, 4),
        new MenuBar(GM, 0, 82, 4, 4, 81, 2, 6, 79, 2, 8, 77, 4, 12, 74, 4),
        new MenuBar(DM, 0, 76, 4, 4, 74, 8, 14, 69, 2),
        // Turnaround: thin the arrangement and let C# lean into the first D.
        new MenuBar(BB, 0, 70, 6, 8, 74, 4, 12, 77, 4),
        new MenuBar(GM, 0, 79, 8, 8, 77, 4, 12, 74, 4),
        new MenuBar(DM, 0, 76, 4, 4, 74, 7, 12, 69, 4),
        new MenuBar(A7, 0, 67, 4, 4, 69, 4, 8, 76, 4, 14, 73, 2)
    };

    public static void main(String[] args) throws IOException {
        Path output = Path.of(args.length == 0 ? "assets/audio" : args[0]);
        Path bgm = Files.createDirectories(output.resolve("bgm"));
        Path sfx = Files.createDirectories(output.resolve("sfx"));

        menuTheme(bgm);
        song(bgm, "stage1", 102, new int[]{40, 41, 45, 43},
            new int[]{0, -1, 7, 12, 15, 12, 10, 7, 0, 3, 7, -1, 0, 7, 10, -1}, .29);
        song(bgm, "stage2", 112, new int[]{48, 45, 41, 43},
            new int[]{12, 7, 4, -1, 7, -1, 12, 14, 16, -1, 14, 12, 7, 4, -1, 7}, .36);
        song(bgm, "stage3", 88, new int[]{38, 41, 36, 43},
            new int[]{0, 12, 7, -1, 3, 10, -1, 7, 12, -1, 10, 3, 7, -1, 3, -1}, .18);
        song(bgm, "stage4", 115, new int[]{42, 41, 38, 37},
            new int[]{0, 6, 12, -1, 7, 6, 3, -1, 0, 3, 7, 12, 13, 12, 7, 6}, .4);
        song(bgm, "boss", 138, new int[]{40, 46, 41, 39},
            new int[]{0, 7, 12, 15, 12, 7, 0, 6, 12, 15, 18, 15, 7, 6, 3, 0}, .59);

        cue(sfx, "ui", .09, new int[]{76, 81}, .04);
        cue(sfx, "arrow", .10, new int[]{79, 74}, .13);
        sliderTick(sfx);
        cue(sfx, "stroke", .045, new int[]{54, 51}, .53);
        cue(sfx, "accept", .23, new int[]{67, 72, 76}, .08);
        cue(sfx, "retry", .27, new int[]{60, 55, 52}, .2);
        cue(sfx, "victory", .72, new int[]{60, 64, 67, 72, 76, 79}, .08);
        skipChime(sfx);
        typewriterClick(sfx);
    }

    private static void menuTheme(Path directory) throws IOException {
        double step = MENU_STEP_SAMPLES / (double) RATE;
        double[] samples = new double[MENU_SCORE.length * MENU_STEPS_PER_BAR * MENU_STEP_SAMPLES];
        for (int bar = 0; bar < MENU_SCORE.length; bar++) {
            MenuBar score = MENU_SCORE[bar];
            double start = bar * MENU_STEPS_PER_BAR * step;
            boolean full = bar >= 4 && bar < 20;
            boolean bridge = bar >= 12 && bar < 16;
            boolean finalBar = bar == MENU_SCORE.length - 1;

            // Sustained voicings overlap into the next bar. tone() wraps the last tail into bar 1.
            for (int voice = 1; voice < score.chord().length; voice++) {
                double length = finalBar && voice == 3 ? 16 : 17.5; // Release the C# before the returning D.
                tone(samples, start, length * step, score.chord()[voice] - 12, .029, .19, .36);
            }
            int bass = score.chord()[0];
            tone(samples, start, 8 * step, bass, .17, .022, .24);
            tone(samples, start + 8 * step, 8.75 * step, bass + (full && !bridge ? 12 : 0), .13, .024, .31);

            int[] voicing = {1, 3, 2, 4};
            for (int beat = 0; beat < voicing.length; beat++) {
                double offbeat = start + (beat * 4 + 2) * step;
                int key = score.chord()[voicing[beat]];
                menuPluck(samples, offbeat, 3 * step, key, bridge ? .075 : .055);
                if (full && beat % 2 == 0) percussion(samples, offbeat, .04, .042, false);
            }
            if (full) {
                percussion(samples, start, .16, .105, true);
                percussion(samples, start + (bridge ? 8 : 10) * step, .13, .082, true);
                percussion(samples, start + 4 * step, .085, .082, false);
                percussion(samples, start + 12 * step, .085, .082, false);
            }

            int[] melody = score.melody();
            for (int i = 0; i < melody.length; i += 3) {
                double onset = start + melody[i] * step;
                double length = melody[i + 2] * step;
                int key = melody[i + 1];
                menuPluck(samples, onset, length + .045, key, full ? .31 : .27);
                menuPluck(samples, onset + 3 * step, Math.min(length, 5 * step), key, .033);
            }
        }
        write(directory.resolve("menu.wav"), samples);
    }

    private static void menuPluck(double[] samples, double start, double duration, int key, double gain) {
        int offset = (int) Math.round(start * RATE);
        int length = (int) Math.round(duration * RATE);
        double phaseStep = TAU * 440 * Math.pow(2, (key - 69) / 12.0) / RATE;
        for (int i = 0; i < length; i++) {
            double time = i / (double) RATE;
            double phase = i * phaseStep;
            double edge = Math.min(1, Math.min(time / .009, (duration - time) / .09));
            double body = .78 * Math.sin(phase) + .17 * Math.sin(2 * phase);
            double attack = (.15 * Math.sin(3 * phase) + .06 * Math.sin(5 * phase)) * Math.exp(-time * 24);
            samples[(offset + i) % samples.length] += gain * edge * Math.exp(-time * 2.35) * (body + attack);
        }
    }

    private static void sliderTick(Path directory) throws IOException {
        double[] samples = new double[(int) Math.round(.055 * RATE)];
        tone(samples, 0, .045, 72, .16, .002, .037);
        tone(samples, 0, .023, 84, .045, .002, .017);
        percussion(samples, 0, .024, .025, false);
        write(directory.resolve("slider.wav"), samples);
    }

    private static void skipChime(Path directory) throws IOException {
        double[] samples = new double[(int) Math.round(.34 * RATE)];
        tone(samples, 0, .11, 79, .12, .004, .09);
        tone(samples, .045, .15, 84, .16, .004, .12);
        tone(samples, .105, .22, 88, .14, .004, .18);
        write(directory.resolve("skip.wav"), samples);
    }

    private static void typewriterClick(Path directory) throws IOException {
        double[] samples = new double[(int) Math.round(.028 * RATE)];
        percussion(samples, 0, .014, .14, false);
        tone(samples, 0, .025, 65, .08, .001, .018);
        tone(samples, .004, .012, 91, .035, .001, .009);
        write(directory.resolve("typewriter.wav"), samples);
    }

    private static void song(Path directory, String name, int bpm, int[] roots, int[] melody, double beatMix)
        throws IOException {
        double beat = 60.0 / bpm;
        double[] samples = new double[(int) Math.round(16 * beat * RATE)];
        for (int bar = 0; bar < 4; bar++) {
            double offset = bar * 4 * beat;
            int root = roots[bar];
            int third = name.equals("stage2") ? 4 : 3;
            tone(samples, offset, 3.96 * beat, root, .09, .18, .8);
            tone(samples, offset, 3.96 * beat, root + third + 12, .041, .32, .85);
            tone(samples, offset, 3.96 * beat, root + 19, .041, .32, .85);
            for (int step = 0; step < 16; step++) {
                double time = offset + step * beat / 4;
                int note = melody[(step + (bar == 3 ? 4 : 0)) % 16];
                if (note >= 0) {
                    tone(samples, time, .34 * beat, root + 12 + note, .12, .009, .09);
                    tone(samples, time + .375 * beat, .26 * beat, root + 12 + note, .033, .009, .09);
                }
                if (step % 2 == 0) percussion(samples, time, .036, beatMix * .11, false);
                if (step == 0 || step == 7 || step == 10) percussion(samples, time, .14, beatMix * .28, true);
                if (step % 4 == 2) tone(samples, time, .36 * beat, root - 12, .07, .014, .15);
            }
        }
        write(directory.resolve(name + ".wav"), samples);
    }

    private static void tone(double[] samples, double start, double duration, int key, double gain,
                             double attack, double release) {
        int offset = (int) Math.round(start * RATE);
        int length = (int) (duration * RATE);
        double frequency = 440 * Math.pow(2, (key - 69) / 12.0);
        for (int i = 0; i < length; i++) {
            double time = i / (double) RATE;
            double phase = TAU * frequency * time;
            double wave = .8 * Math.sin(phase) + .13 * Math.sin(phase * 2) + .07 * Math.sin(phase * 3);
            double envelope = Math.min(1, Math.min(time / attack, (duration - time) / release));
            samples[(offset + i) % samples.length] += wave * envelope * gain;
        }
    }

    private static void percussion(double[] samples, double start, double duration, double gain, boolean kick) {
        int offset = (int) Math.round(start * RATE);
        long noise = 78901L;
        for (int i = 0; i < duration * RATE; i++) {
            double time = i / (double) RATE;
            noise = (noise * 1664525 + 1013904223) & 0xffffffffL;
            double wave = kick ? Math.sin(TAU * (53 * time + 4 * (1 - Math.exp(-time * 32))))
                : (noise / 2147483648.0 - 1);
            samples[(offset + i) % samples.length] += wave * gain * Math.exp(-time * (kick ? 28 : 100));
        }
    }

    private static void cue(Path directory, String name, double duration, int[] notes, double grit) throws IOException {
        double[] samples = new double[(int) Math.round(duration * RATE)];
        double step = duration / (notes.length + .65);
        for (int i = 0; i < notes.length; i++) {
            double onset = step * i;
            tone(samples, onset, step * 1.6, notes[i], .27, .005, step);
            percussion(samples, onset, step, grit * .27, false);
        }
        write(directory.resolve(name + ".wav"), samples);
    }

    private static void write(Path path, double[] samples) throws IOException {
        ByteBuffer pcm = ByteBuffer.allocate(samples.length * 2).order(ByteOrder.LITTLE_ENDIAN);
        for (double sample : samples) pcm.putShort((short) Math.round(Math.clamp(sample, -.85, .85) * 32767));
        AudioFormat format = new AudioFormat(RATE, 16, 1, true, false);
        try (var stream = new AudioInputStream(new ByteArrayInputStream(pcm.array()), format, samples.length)) {
            AudioSystem.write(stream, AudioFileFormat.Type.WAVE, path.toFile());
        }
    }
}
