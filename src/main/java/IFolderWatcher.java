import java.nio.file.WatchEvent;
import java.util.List;

public interface IFolderWatcher {
    boolean setup(String dirToWatch);
    List<WatchEvent<?>> captureEvents() throws InterruptedException;
    boolean resetKey();
}
