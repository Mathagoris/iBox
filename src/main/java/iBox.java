import java.io.IOException;

public class iBox {
    private static IProcessLoop watchLoop;

    public iBox(String dirToWatch, String credsPath) throws IOException {
        watchLoop = new WatchLoop(dirToWatch, credsPath);
    }

    void watch() {
        // Watch folder in infinite loop
        while (watchLoop.loop()) {
        }
    }
}

