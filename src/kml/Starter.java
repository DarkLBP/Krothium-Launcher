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
        } else if (args.length >= 2){
            switch (args[0]){
                case "server":
                    File f = new File(args[1]);
                    if (args.length > 2){
                        String[] serverArgs = new String[args.length - 2];
                        for (int i = 2; i < args.length; i++){
                            serverArgs[i] = args[i + 2];
                        }
                        ServerLauncher.load(f, serverArgs);
                    }
                    ServerLauncher.load(f, new String[0]);
                    break;
            }
        }
    }
}
