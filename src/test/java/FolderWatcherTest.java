import static org.mockito.Mockito.*;
import static java.nio.file.StandardWatchEventKinds.*;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.WatchEvent;
import java.util.List;

public class FolderWatcherTest {
    private static final String TEST_DIR_PATH = "target/testWatchDir";
    @Test
    public void testSetupFolder() {
        File testWatchDir = new File(TEST_DIR_PATH);
        testWatchDir.mkdir();
        FolderWatcher watchService = new FolderWatcher();
        Assert.assertTrue(watchService.setup(testWatchDir.getPath()));
        testWatchDir.delete();

        Assert.assertFalse(watchService.setup("Non-ExistingFolder"));
    }

    @Test
    public void testEventCapture() throws Exception {
        File testWatchDir = new File(TEST_DIR_PATH);
        testWatchDir.mkdir();
        FolderWatcher watchService = new FolderWatcher();
        Assert.assertTrue(watchService.setup(testWatchDir.getPath()));

        File tempFile = new File(TEST_DIR_PATH + "/tempfile.txt");
        tempFile.createNewFile();
        List<WatchEvent<?>> events = watchService.captureEvents();
        Assert.assertTrue(events.size() == 1);
        Assert.assertTrue(events.get(0).kind() == ENTRY_CREATE);
        watchService.resetKey();

        Files.write(tempFile.toPath(), "testing...".getBytes());
        events = watchService.captureEvents();
        Assert.assertTrue(events.size() == 1);
        Assert.assertTrue(events.get(0).kind() == ENTRY_MODIFY);
        watchService.resetKey();

        tempFile.delete();
        events = watchService.captureEvents();
        Assert.assertTrue(events.size() == 1);
        Assert.assertTrue(events.get(0).kind() == ENTRY_DELETE);

        testWatchDir.delete();
    }

    @Test
    public void testEventCaptureInterrupt() {
        File testWatchDir = new File(TEST_DIR_PATH);
        testWatchDir.mkdir();
        FolderWatcher watchService = new FolderWatcher();

        Thread.currentThread().interrupt();
        watchService.setup(testWatchDir.getPath());
        boolean thrown = false;

        try {
            watchService.captureEvents();
        } catch (InterruptedException e) {
            thrown = true;
        }

        Assert.assertTrue(thrown);
    }
}
