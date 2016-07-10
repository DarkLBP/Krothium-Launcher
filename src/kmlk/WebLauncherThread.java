package kmlk;

import kmlk.exceptions.WebLauncherException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @website http://krotium.com
 * @author DarkLBP
 */

public class WebLauncherThread extends Thread{
    private final Socket clientSocket;
    private final Kernel kernel;
    private final Console console;
    public WebLauncherThread(Socket cl)
    {
        this.clientSocket = cl;
        this.kernel = Kernel.getKernel();
        this.console = kernel.getConsole();
    }
    
    @Override
    public void run()
    {
        try {
            InputStream in = this.clientSocket.getInputStream();
            OutputStream out = this.clientSocket.getOutputStream();
            String request = null;
            int i;
            char c;
            int read = 0;
            StringBuilder b = new StringBuilder();
            int available = in.available();
            if (available == 0){
                throw new WebLauncherException(400, out);
            }
            System.out.println("Input available: " + available);
            while((i=in.read())!=-1)
            {
               read++;
               c=(char)i;
               b.append(c);
               if (read == available)
               {
                   break;
               }
            }
            request = b.toString();
            System.out.println(request);
            if (request.startsWith("GET")){
                String[] requestChunks = request.split(" ");
                String path = requestChunks[1];
                if (path.equals("/"))
                {
                    if (!kernel.getAuthentication().isAuthenticated())
                    {
                        out.write("HTTP/1.1 301 Moved Permanently\r\n".getBytes());
                        out.write("Location: /login.html\r\n".getBytes());
                        out.write("\r\n".getBytes());
                    }
                    else
                    {
                        out.write("HTTP/1.1 200 OK\r\n".getBytes());
                        out.write("<html><h1>LOGGED IN</h1></html>".getBytes());
                    }
                }
                else if (path.startsWith("/action/")){
                    
                }else{
                    File abstractFile = new File(path);
                    String fileName = abstractFile.getName();
                    String extension = Utils.getExtension(fileName);
                    String contentTag = "";
                    switch (extension){
                        case "css":
                            contentTag = "text/css";
                            break;
                        case "png":
                            contentTag = "image/png";
                            break;
                        case "js":
                            contentTag = "application/javascript";
                            break;
                        case "html":
                            contentTag = "text/html";
                            break;
                        case "gif":
                            contentTag = "image/gif";
                            break;
                        case "jpg":
                            contentTag = "image/jpeg";
                            break;
                    }
                    if (contentTag.isEmpty()){
                        throw new WebLauncherException(path, 404, out);
                    }
                    InputStream s = WebLauncher.class.getResourceAsStream("/kmlk/web/" + abstractFile.getName());
                    if (s == null)
                    {
                        throw new WebLauncherException(path, 404, out);
                    }
                    out.write("HTTP/1.1 200 OK\r\n".getBytes());
                    out.write(("Content-Type: " + contentTag + "\r\n").getBytes());
                    out.write("\r\n".getBytes());
                    try
                    {
                        byte[] buffer = new byte[4096];
                        while((i=s.read(buffer))!=-1)
                        {
                           out.write(buffer, 0, i);
                        }
                        s.close();
                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
                if (path.endsWith(".png"))
                {
                    File abstractFile = new File(path);
                    InputStream s = WebLauncher.class.getResourceAsStream("/kmlk/web/" + abstractFile.getName());
                    if (s == null)
                    {
                        throw new WebLauncherException(path, 404, out);
                    }
                    out.write("HTTP/1.1 200 OK\r\n".getBytes());
                    out.write("Content-Type: image/png\r\n".getBytes());
                    out.write("\r\n".getBytes());
                    try
                    {
                        
                        while((i=s.read())!=-1)
                        {
                           out.write(i);
                        }
                        s.close();
                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                    
                    /*bufferedWriter.write("HTTP/1.1 200 OK\r\n");
                    bufferedWriter.write("Content-Type: image/png\r\n");
                    bufferedWriter.write("\r\n");*/

                }
                switch (path)
                {
                    case "/":
                        console.printInfo("Reached root");
                        Authentication a = this.kernel.getAuthentication();
                        if (!a.isAuthenticated())
                        {
                            console.printInfo("Not authenticated");
                            /*bufferedWriter.write("HTTP/1.1 301 Moved Permanently\r\n");
                            bufferedWriter.write("Location: /login\r\n");
                            bufferedWriter.write("\r\n");*/
                        }
                        else
                        {
                            console.printInfo("Authenticated");
                            /*bufferedWriter.write("HTTP/1.1 200 OK\r\n");
                            bufferedWriter.write("\r\n");
                            bufferedWriter.write("<html><h1>AUTHENTICATED</h1></html>");*/
                        }
                        break;
                    case "/login":
                        console.printInfo("Reached.");
                        /*bufferedWriter.write("HTTP/1.1 200 OK\r\n");
                        bufferedWriter.write("\r\n");
                        bufferedWriter.write("<html><h1>IT WORKS</h1></html>");*/
                        break;
                }
                
            }
            else if (request.startsWith("POST"))
            {
                out.write("HTTP/1.1 200 OK\r\n".getBytes());
                out.write("\r\n".getBytes());
                out.write("<html><h1>UNSUPPORTED POST REQUESTS</h1></html>".getBytes());
            }
            out.close();
            in.close();
            console.printInfo("Connection ended");
        } catch (IOException ex) {
            Logger.getLogger(WebLauncherThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (WebLauncherException ex) {
            console.printError(ex.getMessage());
        }
        console.printInfo("Ended thread task");
    }
}
