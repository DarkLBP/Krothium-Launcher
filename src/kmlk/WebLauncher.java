package kmlk;

import kmlk.exceptions.AuthenticationException;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import javax.net.ServerSocketFactory;

/**
 * @website http://krotium.com
 * @author DarkLBP
 */

public class WebLauncher {
    
    public static long lastKeepAlive;
    public static void main(String[] args) throws IOException, AuthenticationException
    {
        Kernel kernel = new Kernel();
        Console console = kernel.getConsole();
        console.setEnabled(true);
        console.includeTimestamps(true);
        kernel.setWorkingDir(new File("C:\\Minecraft"));
        kernel.loadVersions();
        kernel.loadProfiles();
        kernel.loadUsers();
        Authentication a = kernel.getAuthentication();
        if (a.hasSelectedUser()){
            try{
                a.validate();
            }catch(Exception ex){
                //
            }
        }
        ServerSocketFactory ssf = ServerSocketFactory.getDefault();
        Random rand = new Random();
        int portStart = 24000;
        int portEnd = 25000;
        int port = rand.nextInt((portEnd - portStart) + 1) + portStart;
        ServerSocket ss = new ServerSocket(port, 50, InetAddress.getLoopbackAddress());
        boolean status = true;
        console.printInfo("Started bundled web server in port " + port);
        Utils.openWebsite("http://krotium.com/launcher.php?p=" + port);
        WebLauncher.lastKeepAlive = System.nanoTime();
        Thread keepAlive = new Thread(){
            @Override
            public void run(){
                long diff = (System.nanoTime() - lastKeepAlive);
                long result = TimeUnit.MILLISECONDS.convert(diff, TimeUnit.NANOSECONDS);
                while (result < Constants.KEEPALIVE_TIMEOUT){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        console.printError("Error in KeepAlive thread.");
                    }
                    diff = (System.nanoTime() - lastKeepAlive);
                    result = TimeUnit.MILLISECONDS.convert(diff, TimeUnit.NANOSECONDS);
                }
                console.printError("KeepAlive timeout exceeded. Closing launcher...");
                kernel.saveProfiles();
                System.exit(0);
            }
        };
        keepAlive.start();
        while (status)
        {
            System.out.println("Awaiting connection.");
            Socket s = ss.accept();
            System.out.println("Connected from " + s.getInetAddress().toString());
            WebLauncherThread thread = new WebLauncherThread(s);
            thread.start();
            System.out.println("Thread ended.");
        }
    }
}
