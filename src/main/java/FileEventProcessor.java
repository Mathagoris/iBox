import java.nio.file.WatchEvent;

import static java.nio.file.StandardWatchEventKinds.*;

public class FileEventProcessor implements IFileEventProcessor {

    @Override
    public boolean processEvent(String fileName, WatchEvent.Kind<?> kind, ICloudFilesystem drive, String parentDir) {
        boolean eventSuccess = false;

        if (fileName.startsWith("."))
            return false;

        System.out.println("***Directory changed: " + fileName + "***");
        System.out.println("File action: " + kind);

        if (kind == ENTRY_CREATE) {
            eventSuccess = drive.uploadFile(parentDir, fileName);
            if (eventSuccess)
                System.out.println("File uploaded to Google Drive!");
            else
                System.out.println("Unable to upload file! :(");
        } else if (kind == ENTRY_MODIFY) {
            eventSuccess = drive.updateFile(parentDir, fileName);
            if (eventSuccess)
                System.out.println("File updated on Google Drive!");
            else
                System.out.println("Unable to update file! :(");
        } else if (kind == ENTRY_DELETE) {
            eventSuccess = drive.deleteFile(fileName);
            if (eventSuccess)
                System.out.println("File deleted from Google Drive!");
            else
                System.out.println("Unable to delete file! :(");
        }
        return eventSuccess;
    }
}
