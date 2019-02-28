import static org.mockito.Mockito.*;
import static java.nio.file.StandardWatchEventKinds.*;

import org.junit.Assert;
import org.junit.Test;

public class FileEventProcessorTest {
    @Test
    public void testInvalidFile() {
        ICloudFilesystem drive = mock(GoogleDrive.class);
        IFileEventProcessor eventProcessor = new FileEventProcessor();

        Assert.assertTrue(!eventProcessor.processEvent(".invalid.txt", ENTRY_CREATE, drive, "/"));
    }

    @Test
    public void testUploadFileEvents() {
        ICloudFilesystem drive = mock(GoogleDrive.class);
        IFileEventProcessor eventProcessor = new FileEventProcessor();

        // Drive uploaded file
        when(drive.uploadFile(anyString(), anyString())).thenReturn(true);
        Assert.assertTrue(eventProcessor.processEvent("someFile.txt", ENTRY_CREATE, drive, "/"));

        // Drive failed to upload
        when(drive.uploadFile(anyString(), anyString())).thenReturn(false);
        Assert.assertTrue(!eventProcessor.processEvent("someFile.txt", ENTRY_CREATE, drive, "/"));
    }

    @Test
    public void testUpdateFileEvents() {
        ICloudFilesystem drive = mock(GoogleDrive.class);
        IFileEventProcessor eventProcessor = new FileEventProcessor();

        // Drive uploaded file
        when(drive.updateFile(anyString(), anyString())).thenReturn(true);
        Assert.assertTrue(eventProcessor.processEvent("someFile.txt", ENTRY_MODIFY, drive, "/"));

        // Drive failed to upload
        when(drive.updateFile(anyString(), anyString())).thenReturn(false);
        Assert.assertTrue(!eventProcessor.processEvent("someFile.txt", ENTRY_MODIFY, drive, "/"));
    }

    @Test
    public void testDeleteFileEvents() {
        ICloudFilesystem drive = mock(GoogleDrive.class);
        IFileEventProcessor eventProcessor = new FileEventProcessor();

        // Drive uploaded file
        when(drive.deleteFile(anyString())).thenReturn(true);
        Assert.assertTrue(eventProcessor.processEvent("someFile.txt", ENTRY_DELETE, drive, "/"));

        // Drive failed to upload
        when(drive.deleteFile(anyString())).thenReturn(false);
        Assert.assertTrue(!eventProcessor.processEvent("someFile.txt", ENTRY_DELETE, drive, "/"));
    }
}
