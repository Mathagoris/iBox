import java.io.File;

public class Main {
    private static final String DEFAULT_DIR = "target/watch-folder";
    private static final String CREDS_PATH = "/client_secret.json"; //resource file

    public static void main(String[] args) {
        iBox myBox;
        try {
            if (args.length != 0)
                myBox = new iBox(args[0], args[1]);
            else {
                File watchDir = new File(DEFAULT_DIR);
                watchDir.mkdir();
                myBox = new iBox(DEFAULT_DIR, CREDS_PATH);
            }
            myBox.watch();
        } catch(Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
