package com.yogiiefe.inkslayer.save;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Local checkpoint files. Only the game owns a writer for a given save directory. */
public final class SaveStore {
    public static final int SLOT_COUNT = 3;
    private static final int VERSION = 1;
    private static final int CONTENT_VERSION = 1;
    private static final int MAX_BYTES = 64 * 1024;
    private static final int MAX_LEARNING_ENTRIES = 128;

    public record Learning(int assisted, int independent, int errors, long lastReviewed) {
        public Learning {
            if (assisted < 0 || independent < 0 || errors < 0 || lastReviewed < 0) {
                throw new IllegalArgumentException("Learning counts and timestamps must be nonnegative.");
            }
        }
    }

    /** Encounter points to the next unfinished battle. Previous battles and unlocks follow from it. */
    public record Progress(int stage, int encounter, int bossRound, boolean endingPending,
                           boolean completed, long playSeconds, long savedAt, Map<String, Learning> learning) {
        public Progress {
            if (stage < 0 || stage >= 4 || encounter < 0 || encounter >= 10
                || bossRound < 0 || bossRound >= 3 || playSeconds < 0 || savedAt < 0) {
                throw new IllegalArgumentException("Checkpoint values are out of range.");
            }
            boolean finalEncounter = stage == 3 && encounter == 9;
            if ((endingPending && completed)
                || (!finalEncounter && (bossRound != 0 || endingPending || completed))) {
                throw new IllegalArgumentException("Checkpoint and ending state do not agree.");
            }
            learning = Map.copyOf(learning);
            if (learning.size() > MAX_LEARNING_ENTRIES || learning.keySet().stream().anyMatch(id -> !validLearningId(id))) {
                throw new IllegalArgumentException("Invalid learning progress identifier.");
            }
        }

        public int battlesWon() {
            return completed || endingPending ? 40 : stage * 10 + encounter;
        }
    }

    public record Slot(Progress progress, boolean recovered, String message) {
        public boolean occupied() {
            return progress != null || !message.isEmpty();
        }

        public boolean loadable() {
            return progress != null;
        }
    }

    private record Candidate(Progress progress, byte[] bytes, String error, boolean newer) {
        boolean missing() {
            return progress == null && error.isEmpty();
        }
    }

    private final Path directory;

    public SaveStore(Path directory) {
        this.directory = Objects.requireNonNull(directory);
    }

    public static Path defaultDirectory() {
        String override = System.getProperty("inkslayer.saves");
        if (override != null && !override.isBlank()) return Path.of(override);

        Path home = Path.of(System.getProperty("user.home"));
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        Path dataDirectory;
        if (os.startsWith("windows")) {
            String local = System.getenv("LOCALAPPDATA");
            dataDirectory = local == null || local.isBlank() ? home.resolve("AppData/Local") : Path.of(local);
        } else if (os.contains("mac")) {
            dataDirectory = home.resolve("Library/Application Support");
        } else {
            String xdg = System.getenv("XDG_DATA_HOME");
            dataDirectory = xdg == null || xdg.isBlank() ? home.resolve(".local/share") : Path.of(xdg);
        }
        return dataDirectory.resolve("INKSLAYER").resolve("saves");
    }

    public synchronized Slot inspect(int slot) {
        Path primaryPath = primary(slot);
        Candidate primary = read(primaryPath, slot);
        if (primary.progress != null) return new Slot(primary.progress, false, "");
        if (primary.newer) return new Slot(null, false, primary.error);

        Candidate backup = read(backup(primaryPath), slot);
        if (backup.progress != null) {
            return new Slot(backup.progress, true, "Loaded the previous checkpoint from backup.");
        }
        if (backup.newer) return new Slot(null, false, backup.error);
        if (primary.missing() && backup.missing()) return new Slot(null, false, "");
        return new Slot(null, false, "This slot could not be read. Select another slot or start a new game.");
    }

    public synchronized void write(int slot, Progress progress) throws IOException {
        Path primaryPath = primary(slot);
        Path backupPath = backup(primaryPath);
        Candidate previous = read(primaryPath, slot);
        Candidate backup = read(backupPath, slot);
        if (previous.newer || backup.newer) throw new IOException("This slot requires a newer game version.");
        // A read failure without bytes may be a permissions or I/O problem. Leave that file alone.
        if ((!previous.missing() && previous.bytes == null) || (!backup.missing() && backup.bytes == null)) {
            throw new IOException("This slot could not be read for saving.");
        }

        Progress stamped = new Progress(progress.stage, progress.encounter, progress.bossRound, progress.endingPending,
            progress.completed, progress.playSeconds, System.currentTimeMillis(), progress.learning);
        byte[] bytes = encode(slot, stamped);
        Files.createDirectories(directory);
        if (previous.progress != null) {
            replace(backupPath, previous.bytes);
        } else if (previous.bytes != null) {
            replace(primaryPath.resolveSibling(primaryPath.getFileName() + ".corrupt"), previous.bytes);
        }
        replace(primaryPath, bytes);
    }

    private Path primary(int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) throw new IllegalArgumentException("Invalid save slot.");
        return directory.resolve(String.format(Locale.ROOT, "slot-%02d.json", slot + 1));
    }

    private static Path backup(Path primary) {
        return primary.resolveSibling(primary.getFileName() + ".bak");
    }

    private static Candidate read(Path file, int slot) {
        byte[] bytes;
        try (var input = Files.newInputStream(file)) {
            bytes = input.readNBytes(MAX_BYTES + 1);
        } catch (NoSuchFileException missing) {
            return new Candidate(null, null, "", false);
        } catch (IOException | SecurityException failure) {
            return new Candidate(null, null, "This save file could not be read.", false);
        }
        if (bytes.length > MAX_BYTES) return new Candidate(null, null, "This save file is too large.", false);
        try {
            String text = StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(bytes)).toString();
            JsonValue root = new JsonReader().parse(text);
            int version = integer(root, "version");
            if (version > VERSION) return new Candidate(null, bytes, "This slot requires a newer game version.", true);
            int contentVersion = integer(root, "contentVersion");
            if (contentVersion > CONTENT_VERSION) {
                return new Candidate(null, bytes, "This slot requires a newer game version.", true);
            }
            return new Candidate(decode(root, slot, version, contentVersion), bytes, "", false);
        } catch (IOException | RuntimeException invalid) {
            return new Candidate(null, bytes, "This save file is damaged.", false);
        }
    }

    private static Progress decode(JsonValue root, int slot, int version, int contentVersion) {
        requireFields(root, Set.of("version", "contentVersion", "slot", "stageId", "encounterId", "bossRound",
            "endingPending", "completed", "playSeconds", "savedAt", "learning"));
        if (version != VERSION || contentVersion != CONTENT_VERSION || integer(root, "slot") != slot + 1) {
            throw new IllegalArgumentException("Unsupported save version or unexpected slot.");
        }
        String stageId = string(root, "stageId");
        String encounterId = string(root, "encounterId");
        if (!stageId.matches("stage0[1-4]") || !encounterId.matches("s" + stageId.charAt(6) + "-(0[1-9]|10)")) {
            throw new IllegalArgumentException("Unknown checkpoint.");
        }
        int stage = Integer.parseInt(stageId.substring(5)) - 1;
        int encounter = Integer.parseInt(encounterId.substring(3)) - 1;
        JsonValue learningObject = root.require("learning");
        if (!learningObject.isObject() || learningObject.size > MAX_LEARNING_ENTRIES) {
            throw new IllegalArgumentException("Invalid learning progress.");
        }
        Map<String, Learning> learning = new LinkedHashMap<>();
        for (JsonValue entry : learningObject) {
            requireFields(entry, Set.of("assisted", "independent", "errors", "lastReviewed"));
            if (!validLearningId(entry.name) || learning.containsKey(entry.name)) {
                throw new IllegalArgumentException("Invalid learning ID.");
            }
            learning.put(entry.name, new Learning(integer(entry, "assisted"), integer(entry, "independent"),
                integer(entry, "errors"), wholeNumber(entry, "lastReviewed")));
        }
        return new Progress(stage, encounter, integer(root, "bossRound"), flag(root, "endingPending"),
            flag(root, "completed"), wholeNumber(root, "playSeconds"), wholeNumber(root, "savedAt"), learning);
    }

    private static byte[] encode(int slot, Progress progress) throws IOException {
        StringWriter output = new StringWriter();
        try (JsonWriter writer = new JsonWriter(output)) {
            writer.setOutputType(JsonWriter.OutputType.json);
            String stageId = "stage0" + (progress.stage + 1);
            writer.object().set("version", VERSION).set("contentVersion", CONTENT_VERSION).set("slot", slot + 1)
                .set("stageId", stageId)
                .set("encounterId", String.format(Locale.ROOT, "s%d-%02d", progress.stage + 1, progress.encounter + 1))
                .set("bossRound", progress.bossRound).set("endingPending", progress.endingPending)
                .set("completed", progress.completed).set("playSeconds", progress.playSeconds).set("savedAt", progress.savedAt)
                .object("learning");
            for (var entry : progress.learning.entrySet()) {
                Learning item = entry.getValue();
                writer.object(entry.getKey()).set("assisted", item.assisted).set("independent", item.independent)
                    .set("errors", item.errors).set("lastReviewed", item.lastReviewed).pop();
            }
            writer.pop().pop();
        }
        return output.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static void replace(Path target, byte[] bytes) throws IOException {
        Path temporary = Files.createTempFile(target.getParent(), target.getFileName().toString(), ".tmp");
        try {
            try (FileChannel channel = FileChannel.open(temporary, StandardOpenOption.WRITE)) {
                ByteBuffer buffer = ByteBuffer.wrap(bytes);
                while (buffer.hasRemaining()) channel.write(buffer);
                channel.force(true);
            }
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException unsupported) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static boolean validLearningId(String id) {
        return id != null && id.matches("[\\p{L}\\p{M}\\p{N}_.-]{1,64}");
    }

    private static void requireFields(JsonValue value, Set<String> fields) {
        if (!value.isObject() || value.size != fields.size()) throw new IllegalArgumentException("Invalid save object.");
        for (String name : fields) value.require(name);
    }

    private static long wholeNumber(JsonValue parent, String name) {
        JsonValue value = parent.require(name);
        if (!value.isLong()) throw new IllegalArgumentException("Expected integer " + name);
        return value.asLong();
    }

    private static int integer(JsonValue parent, String name) {
        return Math.toIntExact(wholeNumber(parent, name));
    }

    private static String string(JsonValue parent, String name) {
        JsonValue value = parent.require(name);
        if (!value.isString()) throw new IllegalArgumentException("Expected string " + name);
        return value.asString();
    }

    private static boolean flag(JsonValue parent, String name) {
        JsonValue value = parent.require(name);
        if (!value.isBoolean()) throw new IllegalArgumentException("Expected flag " + name);
        return value.asBoolean();
    }
}
