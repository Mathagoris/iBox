
import com.google.api.services.drive.Drive;

import javax.tools.Diagnostic;
import java.io.IOException;
import java.nio.file.*;
import static java.nio.file.StandardWatchEventKinds.*;

public class iBox {
    private static ICloudFilesystem drive = new GoogleDrive();
    private static String DIR_TO_WATCH;
    private static final String DRIVE_FOLDER_NAME = "iBox-App-Folder";

    public iBox(String dirToWatch, String credsPath) throws Exception {
        DIR_TO_WATCH = dirToWatch;
        if (!drive.connect(credsPath)) {

        }
        drive.createNewFolder(DRIVE_FOLDER_NAME);
    }
    public iBox(GoogleDrive drive) { this.drive = drive; }

    public void watch() {
        Path dir = Paths.get(DIR_TO_WATCH);
        WatchService watcher;
        WatchKey key;
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
            return;
        }

        // Watch folder in infinite loop
        while(true) {
            try {
                key = watcher.take();
            } catch (InterruptedException e) {
                System.err.println("An error occurred while watching folder.");
                e.printStackTrace(System.err);
                return;
            }

            for (WatchEvent<?> event: key.pollEvents()) {
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

                processEvent(fileName, kind);
            }

            // Reset the key -- this step is critical if you want to
            // receive further watch events.  If the key is no longer valid,
            // the directory is inaccessible so exit the loop.
            boolean valid = key.reset();
            if (!valid) {
                break;
            }
        }
    }

    private boolean processEvent(Path fileName, WatchEvent.Kind<?> kind) {
        boolean eventSuccess = false;

        if (fileName.getFileName().toString().startsWith("."))
            return false;

        System.out.println("***Directory changed: " + fileName + "***");
        System.out.println("File action: " + kind);

        if (kind == ENTRY_CREATE) {
            eventSuccess = drive.uploadFile(DIR_TO_WATCH, fileName.toString());
            if (eventSuccess)
                System.out.println("File uploaded to Google Drive!");
            else
                System.out.println("Unable to upload file! :(");
        } else if (kind == ENTRY_MODIFY) {
            eventSuccess = drive.updateFile(DIR_TO_WATCH, fileName.toString());
            if (eventSuccess)
                System.out.println("File updated on Google Drive!");
            else
                System.out.println("Unable to update file! :(");
        } else if (kind == ENTRY_DELETE) {
            eventSuccess = drive.deleteFile(fileName.toString());
            if (eventSuccess)
                System.out.println("File deleted from Google Drive!");
            else
                System.out.println("Unable to delete file! :(");
        }
        return eventSuccess;
    }
}
