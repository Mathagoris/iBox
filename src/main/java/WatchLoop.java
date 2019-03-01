import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchService;
import java.util.EventListenerProxy;
import java.util.List;

import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

public class WatchLoop implements IProcessLoop {
    private static ICloudFilesystem drive = new GoogleDrive();
    private static IFolderWatcher watchService = new FolderWatcher();
    private static IFileEventProcessor eventProcessor = new FileEventProcessor();
    private static String DIR_TO_WATCH;
    private static final String DRIVE_FOLDER_NAME = "iBox-App-Folder";

    public WatchLoop(String dirToWatch, String credsPath) throws IOException {
        DIR_TO_WATCH = dirToWatch;
        if (!drive.connect(credsPath)) {
            throw new IOException("Unable to Connect to Google Drive! Check error logs for more detail");
        }
        if (!drive.createNewFolder(DRIVE_FOLDER_NAME)) {
            throw new IOException("Unable to create a folder for this App on Google Drive! Check error logs for more detail");
        }
        if (!watchService.setup(DIR_TO_WATCH)) {
            throw new IOException("Unable to initialize folder watching service! Check error logs for more detail");
        }
    }

    public WatchLoop(FolderWatcher watchService, FileEventProcessor eventProcessor, GoogleDrive drive) {
        this.watchService = watchService;
        this.eventProcessor = eventProcessor;
        this.drive = drive;
    }

    public boolean loop(){
        List<WatchEvent<?>> events;
        try {
            events = watchService.captureEvents();
        } catch (InterruptedException e) {
            System.err.println("An error occurred while watching folder.");
            e.printStackTrace(System.err);
            return false;
        }
        for (WatchEvent<?> event: events) {
            WatchEvent.Kind<?> kind = event.kind();

            // This key is registered only for ENTRY_CREATE events,
            // but an OVERFLOW event can occur regardless if events
            // are lost or discarded.
            if (kind == OVERFLOW) {
                continue;
            }

            // The filename is the
            // context of the event.
            WatchEvent<Path> ev = (WatchEvent<Path>)event;
            Path fileName = ev.context();

            eventProcessor.processEvent(fileName.toString(), kind, drive, DIR_TO_WATCH);
        }

        // Reset the key -- this step is critical if you want to
        // receive further watch events.  If the key is no longer valid,
        // the directory is inaccessible so exit the loop.
        boolean valid = watchService.resetKey();
        return valid;
    }
}
