package kml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * @website https://krothium.com
 * @author DarkLBP
 */
public class Starter {
    public static void main(String[] args){
        if (args.length == 0){
            if (existsResource()){
                bootFromResource(args);
            } else {
                WebLauncher.load();
            }
        } else if (args.length >= 1){
            String[] stubArgs = new String[args.length - 1];
            if (args.length > 1){
                System.arraycopy(args, 1, stubArgs, 0, args.length - 1);
            }
            if (existsResource()){
                bootFromResource(stubArgs);
            } else {
                File f = new File(args[0]);
                StubLauncher.load(f, stubArgs);
            }
        }
    }
    private static boolean existsResource() {
        try {
            InputStream in = Starter.class.getResourceAsStream("/resource.jar");
            return in != null;
        } catch (Exception ex){
            return false;
        }
    }
    private static void bootFromResource(String[] passedArgs) {
        InputStream in = Starter.class.getResourceAsStream("/resource.jar");
        File workingDir = Utils.getWorkingDirectory();
        if (!workingDir.exists() || !workingDir.isDirectory()){
            workingDir.mkdirs();
        }
        File resource = new File(workingDir + File.separator + "resource.jar");
        boolean copy = true;
        if (resource.exists() && resource.isFile()){
            copy = resource.delete();
        }
        if (copy) {
            try {
                FileOutputStream out = new FileOutputStream(resource);
                byte[] buffer = new byte[4096];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                in.close();
                out.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        StubLauncher.load(resource, passedArgs);
    }
}
