import static java.nio.file.StandardWatchEventKinds.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.util.ArrayList;
import java.util.List;

public class WatchLoopTest {

    private WatchEvent<?> event = new WatchEvent<Path>() {
        @Override
        public Kind<Path> kind() {
            return ENTRY_CREATE;
        }

        @Override
        public int count() {
            return 1;
        }

        @Override
        public Path context() {
            return Paths.get("some/path");
        }
    };

    @Test
    public void testLoop() throws InterruptedException {
        GoogleDrive drive = mock(GoogleDrive.class);
        FolderWatcher watcher = mock(FolderWatcher.class);
        FileEventProcessor eventProcessor = mock(FileEventProcessor.class);

        List<WatchEvent<?>> events = new ArrayList<WatchEvent<?>>();
        events.add(event);
        when(watcher.captureEvents()).thenReturn(events);
        when(watcher.resetKey()).thenReturn(true);

        when(eventProcessor.processEvent(Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.anyString()))
                .thenReturn(true);

        IProcessLoop looper = new WatchLoop(watcher, eventProcessor, drive);

        Assert.assertTrue(looper.loop());

        when(watcher.captureEvents()).thenThrow(new InterruptedException());
        Assert.assertFalse(looper.loop());

    }
}
