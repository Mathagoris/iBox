import java.nio.file.WatchEvent;

public interface IFileEventProcessor {
    boolean processEvent(String fileName, WatchEvent.Kind<?> kind, ICloudFilesystem drive, String parentDir);
}
