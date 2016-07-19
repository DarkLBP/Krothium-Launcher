package kmlk;

import java.io.BufferedReader;
import kmlk.exceptions.WebLauncherException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
            char[] data;
            int read = 0;
            StringBuilder b = new StringBuilder();
            int available = in.available();
            byte[] tmp = new byte[4096];
            while((read = in.read(tmp)) != -1){
               data = new char[read];
               for (int i = 0; i < read; i++){
                   data[i] = (char)tmp[i];
               }
               b.append(data);
               if (in.available() == 0){
                   break;
               }
            }
            request = b.toString();
            if (request.isEmpty()){
                throw new WebLauncherException(null, 400, out);
            }
            System.out.println(request);
            String[] requestChunks = request.split(" ");
            String path = requestChunks[1];
            boolean closeWhenFinished = false;
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
                    InputStream s = WebLauncher.class.getResourceAsStream("/kmlk/web" + path);
                    if (s == null){
                        throw new WebLauncherException(path, 404, out);
                    }
                    out.write("HTTP/1.1 200 OK\r\n".getBytes());
                    out.write(("Content-Type: " + contentTag + "\r\n").getBytes());
                    out.write("\r\n".getBytes());
                    try{
                        int i;
                        byte[] buffer = new byte[4096];
                        while((i=s.read(buffer))!=-1){
                           out.write(buffer, 0, i);
                        }
                        s.close();
                    }catch (Exception ex){
                        throw new WebLauncherException(path, 500, out);
                    }
                }                
            } else if (request.startsWith("POST")){
                String responseCode = "";
                if (path.startsWith("/action/")){
                    String function = path.replace("/action/", "");
                    switch (function){
                        case "authenticate":
                            try{
                                String[] requestData = request.split("\r\n");
                                String userData = requestData[requestData.length - 1];
                                String userName = userData.split("&")[0].replace("u=", "");
                                String password = userData.split("&")[1].replace("p=", "");
                                try{
                                    kernel.authenticate(userName, password);
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
                            Thread t = new Thread(){
                                @Override
                                public void run(){
                                    kernel.downloadAssets();
                                    kernel.downloadVersion();
                                    kernel.downloadLibraries();
                                    kernel.downloadNatives();
                                    kernel.launchGame();
                                    Thread u = new Thread(){
                                        @Override
                                        public void run(){
                                            InputStreamReader isr = new InputStreamReader(kernel.getGameInputStream());
                                            BufferedReader br = new BufferedReader(isr);
                                            String lineRead;
                                            try{
                                                while (kernel.isGameStarted())
                                                {
                                                    if ((lineRead = br.readLine()) != null)
                                                    {
                                                        System.out.println(lineRead);
                                                    }
                                                }
                                            } catch (Exception ex){
                                                console.printError("Game stopped unexpectedly.");
                                            }
                                            
                                        }
                                    };
                                    u.start();
                                }
                            };
                            t.start();
                            break;
                        case "status":
                            if (kernel.isDownloading()){
                                responseCode = "1";
                            } else if (kernel.isGameStarted()){
                                responseCode = "2";
                            } else {
                                responseCode = "0";
                            }
                            responseCode += "\n";
                            responseCode += String.valueOf(kernel.getDownloadProgress());
                            break;
                        case "close":
                            kernel.saveProfiles();
                            closeWhenFinished = true;
                            break;
                        case "keepalive":
                            WebLauncher.lastKeepAlive = System.nanoTime();
                            break;
                        case "signature":
                            responseCode = "Krotium Minecraft Launcher rev " + String.valueOf(Constants.KERNEL_REVISION);
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
            if (closeWhenFinished){
                System.exit(0);
            }
        } catch (IOException ex) {
            Logger.getLogger(WebLauncherThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (WebLauncherException ex) {
            console.printError(ex.getMessage());
        }
        console.printInfo("Ended thread task");
    }
}
