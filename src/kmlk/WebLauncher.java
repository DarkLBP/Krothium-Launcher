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
        k.getConsole().setEnabled(true);
        k.getConsole().includeTimestamps(true);
        k.setWorkingDir(new File("C:\\Minecraft"));
        k.loadVersions();
        k.loadProfiles();
        k.loadUsers();
        Authentication a = k.getAuthentication();
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
        ServerSocket ss = new ServerSocket(port);
        boolean status = true;
        k.getConsole().printInfo("Started bundled web server in port " + port);
        Runtime.getRuntime().exec("cmd /c start http://localhost:" + port + "/");
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
