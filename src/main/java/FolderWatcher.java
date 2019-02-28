import java.io.IOException;
import java.nio.file.*;
import java.util.List;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

public class FolderWatcher implements IFolderWatcher {
    private static String DIR_TO_WATCH;
    private static WatchKey key;
    private static WatchService watcher;

    @Override
    public boolean setup(String dirToWatch) {
        DIR_TO_WATCH = dirToWatch;
        Path dir = Paths.get(DIR_TO_WATCH);
        try {
            watcher = FileSystems.getDefault().newWatchService();
            key = dir.register(watcher,
                    ENTRY_CREATE,
                    ENTRY_DELETE,
                    ENTRY_MODIFY);
            System.out.println("Watching folder: " + dir);
        } catch (IOException e) {
            System.err.println("Unable to watch folder. It may not exist.");
            e.printStackTrace(System.err);
            return false;
        }
        return true;
    }

    @Override
    public List<WatchEvent<?>> captureEvents() throws InterruptedException {
        key = watcher.take();
        return key.pollEvents();
    }

    @Override
    public boolean resetKey() {
        return key.reset();
    }
}
