public class Main {
    private static final String DEFAULTDIR = "/something";

    public static void main(String[] args){
        iBox myBox;
        if (args.length != 0)
            myBox = new iBox(args[0]);
        else
            myBox = new iBox(DEFAULTDIR);
        myBox.watch();
    }
}
