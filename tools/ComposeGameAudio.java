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

    public static void main(String[] args) throws IOException {
        Path output = Path.of(args.length == 0 ? "assets/audio" : args[0]);
        Path bgm = Files.createDirectories(output.resolve("bgm"));
        Path sfx = Files.createDirectories(output.resolve("sfx"));

        song(bgm, "menu", 79, new int[]{45, 41, 48, 43},
            new int[]{0, 7, 12, -1, 10, 7, 3, -1, 0, 3, 7, 12, -1, 7, 3, -1}, .0);
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
        sliderTick(sfx);
        cue(sfx, "stroke", .045, new int[]{54, 51}, .53);
        cue(sfx, "accept", .23, new int[]{67, 72, 76}, .08);
        cue(sfx, "retry", .27, new int[]{60, 55, 52}, .2);
        cue(sfx, "victory", .72, new int[]{60, 64, 67, 72, 76, 79}, .08);
        skipChime(sfx);
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
