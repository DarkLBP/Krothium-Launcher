package kmlk;

import kmlk.exceptions.AuthenticationException;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import javax.net.ServerSocketFactory;

/**
 * @website http://krotium.com
 * @author DarkLBP
 */

public class WebLauncher {
    public static void main(String[] args) throws IOException, AuthenticationException
    {
        Kernel k = new Kernel();
        Console c = k.getConsole();
        k.setWorkingDir(new File("C:\\Minecraft"));
        c.setEnabled(true);
        c.includeTimestamps(true);
        ServerSocketFactory ssf = ServerSocketFactory.getDefault();
        Random rand = new Random();
        int portStart = 24000;
        int portEnd = 25000;
        int port = rand.nextInt((portEnd - portStart) + 1) + portStart;
        ServerSocket ss = new ServerSocket(8080);
        boolean status = true;
        c.printInfo("Started bundled web server in port " + port);
        //Runtime.getRuntime().exec("cmd /c start http://localhost:" + port + "/");
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
