package kmlk;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import kmlk.exceptions.WebLauncherException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import kmlk.enums.VersionType;
import kmlk.exceptions.AuthenticationException;
import kmlk.exceptions.DownloaderException;
import kmlk.exceptions.GameLauncherException;
import kmlk.objects.Profile;
import kmlk.objects.VersionMeta;

/**
 * @website https://krothium.com
 * @author DarkLBP
 */

public class WebLauncherThread extends Thread{
    private final Socket clientSocket;
    private final Kernel kernel;
    private final Console console;
    public WebLauncherThread(Socket cl, Kernel k){
        this.clientSocket = cl;
        this.kernel = k;
        this.console = kernel.getConsole();
    }
    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            OutputStream out = this.clientSocket.getOutputStream();
            String request;
            StringBuilder b = new StringBuilder();
            String line = in.readLine();
            boolean isPost = line.startsWith("POST");
            int contentLength = 0;
            String contentType = null;
            ByteArrayOutputStream binary = new ByteArrayOutputStream();
            boolean isBinary = false;
            b.append(line).append("\n");
            while (!(line = in.readLine()).equals("")) {
                b.append(line).append("\n");
                if (isPost){
                    final String contentHeader = "Content-Length: ";
                    final String typeHeader = "Content-Type: ";
                    if (line.startsWith(contentHeader)){
                        contentLength = Integer.parseInt(line.substring(contentHeader.length()));
                    }
                    if (line.startsWith(typeHeader)){
                        contentType = line.substring(typeHeader.length());
                    }
                }
            }
            if (isPost && contentLength > 0 && contentType != null){
                if (contentType.startsWith("text/")){
                    int read;
                    for (int i = 0; i < contentLength; i++){
                        read = in.read();
                        b.append((char)read);
                    }
                } else {
                    isBinary = true;
                    int read;
                    InputStream in2 = this.clientSocket.getInputStream();
                    for (int i = 0; i < contentLength; i++){
                        read = in2.read();
                        binary.write(read);
                    }
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
                if (path.equals("/")){
                    if (!kernel.getAuthentication().isAuthenticated()){
                        out.write("HTTP/1.1 301 Moved Permanently\r\n".getBytes());
                        out.write("Location: /login.html\r\n".getBytes());
                        out.write("\r\n".getBytes());
                    }else{
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
                                    try{
                                        kernel.download();
                                        kernel.launchGame();
                                    } catch (GameLauncherException | DownloaderException ex){
                                        console.printError(ex.getMessage());
                                    }
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
                            response = "Krothium Minecraft Launcher v" + Constants.KERNEL_BUILD_NAME;
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
                            Profile sprof = kernel.getProfile(kernel.getSelectedProfile());
                            if (sprof.hasVersion()){
                                response = Utils.toBase64(Utils.toBase64(sprof.getVersionID())) + ":" + Utils.toBase64(sprof.getVersionID());
                            } else {
                                response = Utils.toBase64("latest") + ":" + Utils.toBase64(kernel.getLatestVersion());
                            }
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
                            LinkedHashSet<String> v = kernel.getVersionDB();
                            Iterator vi = v.iterator();
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
                                response = "latest";
                                while (vi.hasNext()){
                                    String index = vi.next().toString();
                                    VersionMeta version = kernel.getVersionMeta(index);
                                    if (allowedTypes.contains(version.getType())){
                                        response += ":" + Utils.toBase64(version.getID());
                                    }
                                }
                            } else {
                                Profile prof = kernel.getProfile(kernel.getSelectedProfile());
                                allowedTypes = prof.getAllowedVersionTypes();
                                response = "latest";
                                while (vi.hasNext()){
                                    String index = vi.next().toString();
                                    VersionMeta version = kernel.getVersionMeta(index);
                                    if (allowedTypes.contains(version.getType())){
                                        response += ":" + Utils.toBase64(version.getID());
                                    }
                                }
                            }
                            break;
                        case "saveprofile":
                            String[] profileArray = parameters.split(":");
                            if (profileArray.length != 10){
                                throw new WebLauncherException(path, 400, out);
                            }
                            if (profileArray[0].equals("noset")){
                                response = "Profile name not specified.";
                            } else if (profileArray[1].equals("noset")){
                                response = "Profile name cannot be blank.";
                            } else {
                                String profileName = Utils.fromBase64(profileArray[0]);
                                String profileNameNew = Utils.fromBase64(profileArray[1]);
                                String profileVersion;
                                if (profileArray[2].equals("latest")){
                                    profileVersion = null;
                                } else {
                                    profileVersion = Utils.fromBase64(profileArray[2]);
                                }
                                boolean snapshot = Boolean.valueOf(Utils.fromBase64(profileArray[3]));
                                boolean oldBeta = Boolean.valueOf(Utils.fromBase64(profileArray[4]));
                                boolean oldAlpha = Boolean.valueOf(Utils.fromBase64(profileArray[5]));
                                String gameDir = (profileArray[6].equals("noset") ? "" : Utils.fromBase64(profileArray[6]));
                                String resolution = (profileArray[7].equals("noset") ? "" : Utils.fromBase64(profileArray[7]));
                                String javaExec = (profileArray[8].equals("noset") ? "" : Utils.fromBase64(profileArray[8]));
                                String javaArgs = (profileArray[9].equals("noset") ? "" : Utils.fromBase64(profileArray[9]));
                                if (!kernel.existsProfile(profileName)){
                                    response = "Profile " + profileName + " is specified but does not exist.";
                                } else {
                                    Profile up = kernel.getProfile(profileName);
                                    boolean error = false;
                                    if (profileVersion == null){
                                        up.setVersionID(null);
                                    } else if (!kernel.existsVersion(profileVersion)){
                                        error = true;
                                        response += "Selected version " + profileVersion + " does not exist." + "\n";
                                    } else {
                                        up.setVersionID(profileVersion);
                                    }
                                    if (!gameDir.isEmpty()){
                                        File dir = new File(gameDir);
                                        up.setGameDir(dir);
                                    } else {
                                        up.setGameDir(null);
                                    }
                                    if (!resolution.isEmpty()){
                                        try{
                                            int x = Integer.parseInt(resolution.split("x")[0]);
                                            int y = Integer.parseInt(resolution.split("x")[1]);
                                            up.setResolution(x, y);
                                        } catch (Exception ex){
                                            error = true;
                                            response += "Invalid resolution values." + "\n";
                                        }
                                    }
                                    else{
                                        up.setResolution(-1, -1);
                                    }
                                    if (!javaExec.isEmpty()){
                                        File file = new File(javaExec);
                                        if (file.exists()){
                                            if (file.isFile()){
                                                up.setJavaDir(file); 
                                            }
                                            else {
                                                error = true;
                                                response += "Invalid java executable file." + "\n";
                                            }

                                        } else {
                                            error = true;
                                            response += "Java executable does not exist." + "\n";
                                        }
                                    } else {
                                        up.setJavaDir(null);
                                    }
                                    if (!javaArgs.isEmpty()){
                                        up.setJavaArgs(javaArgs);
                                    } else {
                                        up.setJavaArgs(null);
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
                                    if (!error){
                                        response = "OK";
                                        kernel.saveProfiles();
                                    }
                                }
                            }
                            break;
                        case "profiledata":
                            profile = Utils.fromBase64(parameters);
                            if (kernel.existsProfile(profile)){
                                Profile rp = kernel.getProfile(profile);
                                response += Utils.toBase64(rp.getName());
                                response += ":";
                                response += (rp.hasVersion() ? Utils.toBase64(rp.getVersionID()) : "latest");
                                response += ":";
                                response += Utils.toBase64((String.valueOf(rp.isAllowedVersionType(VersionType.SNAPSHOT))));
                                response += ":";
                                response += Utils.toBase64(String.valueOf(rp.isAllowedVersionType(VersionType.OLD_BETA)));
                                response += ":";
                                response += Utils.toBase64(String.valueOf(rp.isAllowedVersionType(VersionType.OLD_ALPHA)));
                                response += ":";
                                response += (rp.hasGameDir() ? Utils.toBase64(rp.getGameDir().getAbsolutePath()) : "noset");
                                response += ":";
                                response += (rp.hasResolution() ? Utils.toBase64(String.valueOf(rp.getResolutionWidth()) + "x" + String.valueOf(rp.getResolutionHeight())) : "noset");
                                response += ":";
                                response += (rp.hasJavaDir() ? Utils.toBase64(rp.getJavaDir().getAbsolutePath()) : "noset");
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
        } catch (IOException | WebLauncherException  ex) {
            console.printError(ex.getMessage());
        }
        console.printInfo("Ended thread task");
    }
}
