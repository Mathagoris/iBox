public class Main {
    private static final String DEFAULT_DIR = "/watch-folder";
    private static final String DATA_PATH = "/credentials.json";

    public static void main(String[] args){
        iBox myBox;
        try {
            if (args.length != 0)
                myBox = new iBox(args[0], args[1]);
            else
                myBox = new iBox(DEFAULT_DIR, DATA_PATH);
            myBox.watch();
        } catch(Exception e) {
            System.out.println(e);
        }
    }
}
