package kmlk;

import java.io.BufferedReader;
import kmlk.exceptions.WebLauncherException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import kmlk.enums.VersionType;
import kmlk.exceptions.AuthenticationException;
import kmlk.objects.Profile;
import kmlk.objects.Version;

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
            BufferedReader in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            OutputStream out = this.clientSocket.getOutputStream();
            String request;
            StringBuilder b = new StringBuilder();
            String line = in.readLine();
            boolean isPost = line.startsWith("POST");
            int contentLength = 0;
            b.append(line).append("\n");
            while (!(line = in.readLine()).equals("")) {
                b.append(line).append("\n");
                if (isPost){
                    final String contentHeader = "Content-Length: ";
                    if (line.startsWith(contentHeader)){
                        contentLength = Integer.parseInt(line.substring(contentHeader.length()));
                    }
                }
            }
            if (isPost && contentLength > 0){
                int read;
                for (int i = 0; i < contentLength; i++){
                    read = in.read();
                    b.append((char)read);
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
                    String finalPath = (path.contains("?") ? path.split("\\?")[0] : path);
                    File abstractFile = new File(finalPath);
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
                        case "woff":
                            contentTag = "application/font-woff";
                            break;
                        case "woff2":
                            contentTag = "application/font-woff2";
                            break;
                        case "svg":
                            contentTag = "image/svg+xml";
                            break;
                        case "eot":
                            contentTag = "application/vnd.ms-fontobject";
                            break;
                        case "ttf":
                            contentTag = "application/x-font-ttf";
                            break;
                    }
                    if (contentTag.isEmpty()){
                        throw new WebLauncherException(path, 404, out);
                    }
                    InputStream s = WebLauncher.class.getResourceAsStream("/kmlk/web" + finalPath);
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
                String response = "";
                if (path.startsWith("/action/")){
                    String function = path.replace("/action/", "");
                    String[] requestData = request.split("\n");
                    String parameters = "";
                    if (contentLength > 0){
                        parameters = requestData[requestData.length - 1];
                        if (parameters.isEmpty()){
                            throw new WebLauncherException(path, 400, out);
                        }
                    }
                    String profile;
                    switch (function){
                        case "authenticate":
                            if (contentLength > 0){
                                String[] userArray = parameters.split(":");
                                if (userArray.length != 2){
                                    throw new WebLauncherException(path, 400, out);
                                }
                                String userName = Utils.fromBase64(userArray[0]);
                                String password = Utils.fromBase64(userArray[1]);
                                try{
                                    kernel.authenticate(userName, password);
                                    kernel.saveProfiles();
                                    response = "OK";
                                }catch (AuthenticationException ex){
                                    response = ex.getMessage();
                                }
                            } else {
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
                                response = "1";
                            } else if (kernel.isGameStarted()){
                                response = "2";
                            } else {
                                response = "0";
                            }
                            response += ":";
                            response += String.valueOf(kernel.getDownloadProgress());
                            break;
                        case "close":
                            kernel.saveProfiles();
                            closeWhenFinished = true;
                            break;
                        case "keepalive":
                            WebLauncher.lastKeepAlive = System.nanoTime();
                            break;
                        case "signature":
                            response = "Krotium Minecraft Launcher rev " + String.valueOf(Constants.KERNEL_REVISION);
                            break;
                        case "logout":
                            if (kernel.logOut()){
                               kernel.saveProfiles();
                                response = "OK"; 
                            }
                            break;
                        case "profiles":
                            Map<String, Profile> p = kernel.getProfileDB();
                            Set keys = p.keySet();
                            Iterator i = keys.iterator();
                            while (i.hasNext()){
                                response += Utils.toBase64(i.next().toString());
                                if (i.hasNext()){
                                    response += ":";
                                }
                            }
                            break;
                        case "selectedprofile":
                            response = Utils.toBase64(kernel.getSelectedProfile());
                            break;
                        case "setselectedprofile":
                            profile = Utils.fromBase64(parameters);
                            if (kernel.existsProfile(profile)){
                                if (kernel.setSelectedProfile(profile)){
                                    response = "OK";
                                    kernel.saveProfiles();
                                } else {
                                    response = "ERROR";
                                }
                            } else {
                                response = "ERROR";
                            }
                            break;
                        case "selectedversion":
                            response = Utils.toBase64(kernel.getProfile(kernel.getSelectedProfile()).getVersion().getID());
                            break;
                        case "deleteprofile":
                            profile = Utils.fromBase64(parameters);
                            if (!kernel.existsProfile(profile)){
                                response = "ERROR";
                            } else {
                                if (kernel.deleteProfile(profile)){
                                    response = "OK";
                                    kernel.saveProfiles();
                                } else {
                                    response = "ERROR";
                                }
                            }
                            break;
                        case "versions":
                            Map<String, Version> v = kernel.getVersionDB();
                            Set vkeys = v.keySet();
                            Iterator vi = vkeys.iterator();
                            boolean first = true;
                            List<VersionType> allowedTypes = new ArrayList();
                            if (contentLength > 0){
                                String[] types = parameters.split(":");
                                if (types.length != 3){
                                    throw new WebLauncherException(path, 400, out);
                                }
                                boolean snapshot = Boolean.valueOf(Utils.fromBase64(types[0]));
                                boolean oldBeta = Boolean.valueOf(Utils.fromBase64(types[1]));
                                boolean oldAlpha = Boolean.valueOf(Utils.fromBase64(types[2]));
                                allowedTypes.add(VersionType.RELEASE);
                                if (snapshot){
                                    allowedTypes.add(VersionType.SNAPSHOT);
                                }
                                if (oldBeta){
                                    allowedTypes.add(VersionType.OLD_BETA);
                                }
                                if (oldAlpha){
                                    allowedTypes.add(VersionType.OLD_ALPHA);
                                }
                                while (vi.hasNext()){
                                    String index = vi.next().toString();
                                    Version version = v.get(index);
                                    if (allowedTypes.contains(version.getType())){
                                        if (first){
                                            first = false;
                                        } else {
                                            response += ":";
                                        }
                                        response += Utils.toBase64(version.getID());
                                    }
                                }
                            } else {
                                Profile prof = kernel.getProfile(kernel.getSelectedProfile());
                                allowedTypes = prof.getAllowedVersionTypes();
                                while (vi.hasNext()){
                                    String index = vi.next().toString();
                                    Version version = v.get(index);
                                    if (allowedTypes.contains(version.getType())){
                                        if (first){
                                            first = false;
                                        } else {
                                            response += ":";
                                        }
                                        response += Utils.toBase64(version.getID());
                                    }
                                }
                            }
                            break;
                        case "saveprofile":
                            String[] profileArray = parameters.split(":");
                            if (profileArray.length != 7){
                                throw new WebLauncherException(path, 400, out);
                            }
                            String profileName = Utils.fromBase64(profileArray[0]);
                            String profileNameNew = Utils.fromBase64(profileArray[1]);
                            String profileVersion = Utils.fromBase64(profileArray[2]);
                            boolean snapshot = Boolean.valueOf(Utils.fromBase64(profileArray[3]));
                            boolean oldBeta = Boolean.valueOf(Utils.fromBase64(profileArray[4]));
                            boolean oldAlpha = Boolean.valueOf(Utils.fromBase64(profileArray[5]));
                            String javaArgs = Utils.fromBase64(profileArray[6]);
                            if (!kernel.existsProfile(profileName)){
                                response += "ERROR";
                            } else {
                                if (!profileNameNew.isEmpty()){
                                    Profile up = kernel.getProfile(profileName);
                                    if (!kernel.existsVersion(profileVersion)){
                                        response += "ERROR";
                                    } else {
                                        up.setVersion(kernel.getVersion(profileVersion));
                                        if (!javaArgs.isEmpty()){
                                            up.setJavaArgs(javaArgs);
                                        }
                                        if (snapshot){
                                            up.allowVersionType(VersionType.SNAPSHOT);
                                        } else {
                                            up.removeVersionType(VersionType.SNAPSHOT);
                                        }
                                        if (oldBeta){
                                            up.allowVersionType(VersionType.OLD_BETA);
                                        } else {
                                            up.removeVersionType(VersionType.OLD_BETA);
                                        }
                                        if (oldAlpha){
                                            up.allowVersionType(VersionType.OLD_ALPHA);
                                        } else {
                                            up.removeVersionType(VersionType.OLD_ALPHA);
                                        }
                                        kernel.updateProfile(up);
                                        if (!profileName.equals(profileNameNew)){
                                            kernel.renameProfile(profileName, profileNameNew);
                                        }
                                        response += "OK";
                                        kernel.saveProfiles();
                                    }
                                } else {
                                    response += "ERROR";
                                }
                            }
                            break;
                        case "profiledata":
                            profile = Utils.fromBase64(parameters);
                            if (kernel.existsProfile(profile)){
                                Profile rp = kernel.getProfile(profile);
                                response += Utils.toBase64(rp.getName());
                                response += ":";
                                response += Utils.toBase64(rp.getVersion().getID());
                                response += ":";
                                response += Utils.toBase64((String.valueOf(rp.isAllowedVersionType(VersionType.SNAPSHOT))));
                                response += ":";
                                response += Utils.toBase64(String.valueOf(rp.isAllowedVersionType(VersionType.OLD_BETA)));
                                response += ":";
                                response += Utils.toBase64(String.valueOf(rp.isAllowedVersionType(VersionType.OLD_ALPHA)));
                                response += ":";
                                response += (rp.hasJavaArgs() ? Utils.toBase64(rp.getJavaArgs()) : "noset");
                            } else {
                                response = "ERROR";
                            }
                            break;
                    }
                }
                out.write("HTTP/1.1 200 OK\r\n".getBytes());
                out.write("Content-Type: text/html\r\n".getBytes());
                out.write("\r\n".getBytes());
                out.write(response.getBytes());
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
