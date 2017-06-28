package kml;

import javafx.application.Application;
import javafx.stage.Stage;
import kml.handlers.BrowserHandler;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.Objects;
import java.util.Properties;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public class Starter extends Application {
    public static void main(String[] args) throws IOException, FontFormatException, ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        if (args.length == 0) {
            if (existsResource()) {
                bootFromResource(args);
            } else {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                URL.setURLStreamHandlerFactory(new BrowserHandler());
                launch(args);
            }
        } else if (args.length >= 1) {
            String[] stubArgs = new String[args.length - 1];
            if (args.length > 1) {
                System.arraycopy(args, 1, stubArgs, 0, args.length - 1);
            }
            if (existsResource()) {
                bootFromResource(stubArgs);
            } else {
                File f = new File(args[0]);
                StubLauncher.load(f, stubArgs);
            }
        }
    }

    private static boolean existsResource() {
        try {
            File custom = new File("resource.ini");
            if (custom.exists() && custom.isFile()) {
                return true;
            }
            InputStream in = Starter.class.getResourceAsStream("/resource.jar");
            return Objects.nonNull(in);
        } catch (Exception ex) {
            return false;
        }
    }

    private static void bootFromResource(String[] passedArgs) {
        File custom = new File("resource.ini");
        if (custom.exists() && custom.isFile()) {
            Properties p = new Properties();
            try {
                FileInputStream fin = new FileInputStream(custom);
                p.load(fin);
                File resource = new File(p.getProperty("path"));
                StubLauncher.load(resource, passedArgs);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            InputStream in = Starter.class.getResourceAsStream("/resource.jar");
            File workingDir = Utils.getWorkingDirectory();
            if (!workingDir.exists() || !workingDir.isDirectory()) {
                workingDir.mkdirs();
            }
            File resource = new File(workingDir + File.separator + "resource.jar");
            boolean copy = true;
            if (resource.exists() && resource.isFile()) {
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

    @Override
    public void start(Stage primaryStage) throws Exception {
        Utils.testNetwork();
        Kernel kernel = new Kernel(primaryStage, getHostServices());
    }
}
