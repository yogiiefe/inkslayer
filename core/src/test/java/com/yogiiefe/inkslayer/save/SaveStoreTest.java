package com.yogiiefe.inkslayer.save;

import com.yogiiefe.inkslayer.save.SaveStore.Learning;
import com.yogiiefe.inkslayer.save.SaveStore.Progress;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SaveStoreTest {
    private Path directory;

    @BeforeEach
    void createSaveDirectory() throws IOException {
        directory = Files.createTempDirectory("inkslayer-save-test");
    }

    @AfterEach
    void removeSaveFiles() throws IOException {
        if (directory == null) return;
        // Direct children only. JUnit's recursive TempDir cleanup uses toRealPath on this Windows host.
        try (var files = Files.newDirectoryStream(directory)) {
            for (Path file : files) Files.delete(file);
        }
        Files.delete(directory);
    }

    @Test
    void maintainsSeparateSlotsAndLearningHistory() throws IOException {
        var store = new SaveStore(directory);
        var learning = Map.of("水", new Learning(2, 1, 1, 100));
        store.write(0, new Progress(1, 3, 0, false, false, 121, 0, learning));
        store.write(1, checkpoint(0));

        var first = store.inspect(0).progress();
        assertEquals(13, first.battlesWon());
        assertEquals(121, first.playSeconds());
        assertEquals(learning, first.learning());
        assertTrue(first.savedAt() > 0);
        assertEquals(0, store.inspect(1).progress().battlesWon());
        assertFalse(store.inspect(2).occupied());
    }

    @Test
    void recoversFromInterruptedOrDamagedPrimaryWithoutRotatingItIntoBackup() throws IOException {
        var store = new SaveStore(directory);
        store.write(0, checkpoint(2));
        store.write(0, checkpoint(3));
        Path primary = directory.resolve("slot-01.json");
        byte[] damaged = "{".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        Files.write(primary, damaged);

        var recovered = store.inspect(0);
        assertTrue(recovered.recovered());
        assertEquals(2, recovered.progress().encounter());
        store.write(0, checkpoint(4));
        assertEquals(4, store.inspect(0).progress().encounter());
        assertArrayEquals(damaged, Files.readAllBytes(directory.resolve("slot-01.json.corrupt")));

        Files.write(primary, damaged);
        assertEquals(2, store.inspect(0).progress().encounter());
    }

    @Test
    void preservesNewerVersionEvenWhenItLacksCurrentSchemaFields() throws IOException {
        var store = new SaveStore(directory);
        Path primary = directory.resolve("slot-01.json");
        String future = "{\"version\":99}";
        Files.writeString(primary, future);

        assertFalse(store.inspect(0).loadable());
        assertThrows(IOException.class, () -> store.write(0, checkpoint(0)));
        assertEquals(future, Files.readString(primary));
    }

    @Test
    void persistsTheEndingCheckpointBeforeCampaignCompletion() throws IOException {
        var store = new SaveStore(directory);
        var pending = new Progress(3, 9, 0, true, false, 600, 0, Map.of());
        store.write(2, pending);
        assertTrue(store.inspect(2).progress().endingPending());
        assertEquals(40, store.inspect(2).progress().battlesWon());

        store.write(2, new Progress(3, 9, 0, false, true, 650, 0, Map.of()));
        assertTrue(store.inspect(2).progress().completed());
    }

    private static Progress checkpoint(int encounter) {
        return new Progress(0, encounter, 0, false, false, 0, 0, Map.of());
    }
}
