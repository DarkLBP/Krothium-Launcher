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
import kmlk.exceptions.AuthenticationException;

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
                   available+=in.available();
                   if (read == available)
                   {
                       break;
                   }
               }
            }
            request = b.toString();
            System.out.println(request);
            String[] requestChunks = request.split(" ");
            String path = requestChunks[1];
            if (request.startsWith("GET")){
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
                        out.write("HTTP/1.1 301 Moved Permanently\r\n".getBytes());
                        out.write("Location: /play.html\r\n".getBytes());
                        out.write("\r\n".getBytes());
                    }
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
                    System.out.println("/kmlk/web" + path);
                    InputStream s = WebLauncher.class.getResourceAsStream("/kmlk/web" + path);
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
            }
            else if (request.startsWith("POST"))
            {
                String responseCode = "ERROR";
                if (path.startsWith("/action/")){
                    String function = path.replace("/action/", "");
                    switch (function){
                        case "authenticate":
                            try{
                                String[] requestData = request.split("\r\n");
                                String userData = requestData[requestData.length - 1];
                                System.out.println("DATOS DE USUARIO: " + userData);
                                String userName = userData.split("&")[0].replace("u=", "");
                                String password = userData.split("&")[1].replace("p=", "");
                                try{
                                    kernel.getAuthentication().authenticate(userName, password);
                                    kernel.saveProfiles();
                                    responseCode = "OK";
                                }catch (AuthenticationException ex){
                                    responseCode = ex.getMessage();
                                }
                            }catch (Exception ex){
                                throw new WebLauncherException(path, 400, out);
                            }
                            break;
                        case "play":
                            GameLauncher g = kernel.getGameLauncher();
                            g.launch(kernel.getSelectedProfile());
                            break;
                    }
                }
                out.write("HTTP/1.1 200 OK\r\n".getBytes());
                out.write("Content-Type: text/html\r\n".getBytes());
                out.write("\r\n".getBytes());
                out.write(responseCode.getBytes());
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
