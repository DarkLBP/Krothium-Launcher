package kml;

import java.io.File;
import java.io.FileOutputStream;
import kml.exceptions.AuthenticationException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @website https://krothium.com
 * @author DarkLBP
 */

public class WebLauncher {
    
    public static long lastKeepAlive;
    public static void main(String[] args) throws IOException, AuthenticationException{
        Kernel kernel = new Kernel();
        Console console = kernel.getConsole();
        console.includeTimestamps(true);
        kernel.loadVersions();
        kernel.loadProfiles();
        kernel.loadUsers();
        Authentication a = kernel.getAuthentication();
        if (a.hasSelectedUser()){
            try{
                a.refresh();
            }catch(AuthenticationException ex){
                console.printError(ex.getMessage());
            }
            kernel.saveProfiles();
        }
        Random rand = new Random();
        int portStart = 24000;
        int portEnd = 25000;
        int port = rand.nextInt((portEnd - portStart) + 1) + portStart;
        ServerSocket ss = new ServerSocket(port, 100, InetAddress.getLoopbackAddress());
        boolean status = true;
        console.printInfo("Started bundled web server in port " + port);
        Utils.openWebsite("http://mc.krothium.com/launcher/?p=" + port);
        WebLauncher.lastKeepAlive = System.nanoTime();
        Thread keepAlive = new Thread(){
            @Override
            public void run(){
                long diff = (System.nanoTime() - lastKeepAlive);
                long result = TimeUnit.MILLISECONDS.convert(diff, TimeUnit.NANOSECONDS);
                while (result < Constants.KEEPALIVE_TIMEOUT){
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException ex) {
                        console.printError("Error in KeepAlive thread.");
                    }
                    diff = (System.nanoTime() - lastKeepAlive);
                    result = TimeUnit.MILLISECONDS.convert(diff, TimeUnit.NANOSECONDS);
                }
                console.printInfo("KeepAlive timeout exceeded. Saving logs and closing launcher...");
                try {
                    File log;
                    if (console.usesCompression()){
                        console.stopCompressing();
                        log = new File(kernel.getWorkingDir() + File.separator + "logs" + File.separator + "weblauncher.log.gz");
                    } else {
                        log = new File(kernel.getWorkingDir() + File.separator + "logs" + File.separator + "weblauncher.log");
                    }
                    if (log.exists()){
                        log.delete();
                    }
                    if (!log.getParentFile().exists()){
                        log.getParentFile().mkdirs();
                    }
                    FileOutputStream out = new FileOutputStream(log);
                    out.write(console.getData());
                    out.close();
                } catch (Exception ex) {}
                System.exit(0);
            }
        };
        keepAlive.start();
        while (status){
            Socket s = ss.accept();
            WebLauncherThread thread = new WebLauncherThread(s, kernel);
            thread.start();
        }
    }
}
