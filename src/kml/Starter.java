package kml;

import java.io.File;

/**
 * @website https://krothium.com
 * @author DarkLBP
 */
public class Starter {
    public static void main(String[] args){
        if (args.length == 0){
            WebLauncher.load();
        } else if (args.length >= 1){
            File f = new File(args[0]);
            String[] stubArgs = new String[args.length - 1];
            if (args.length > 1){
                System.arraycopy(args, 1, stubArgs, 0, args.length - 1);
            }
            StubLauncher.load(f, stubArgs);
        }
    }
}
