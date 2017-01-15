package kml;

import kml.enums.OS;
import kml.enums.OSArch;
import kml.enums.ProfileIcon;
import javax.imageio.ImageIO;
import javax.net.ssl.*;
import javax.swing.*;
import javax.xml.bind.DatatypeConverter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * @author DarkLBP
 * website https://krothium.com
 */

public class Utils {
    public static boolean ignoreHTTPSCert(){
        try {
            SSLContext t = SSLContext.getInstance("SSL");
            t.init(null, new X509TrustManager[]{new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, null);
            HttpsURLConnection.setDefaultSSLSocketFactory(t.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            });
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
    public static void testNetwork(){
        try {
            HttpsURLConnection con = (HttpsURLConnection)Constants.HANDSHAKE_URL.openConnection();
            int responseCode = con.getResponseCode();
            Constants.USE_LOCAL = responseCode != 204;
        } catch (IOException ex){
            Constants.USE_LOCAL = true;
        }
    }
    public static OS getPlatform(){
        final String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")){
            return OS.WINDOWS;
        }else if (osName.contains("mac")){
            return OS.OSX;
        }else if (osName.contains("linux") || osName.contains("unix")){
            return OS.LINUX;
        }
        return OS.UNKNOWN;
    }
    public static File getWorkingDirectory(){
        final String userHome = System.getProperty("user.home", ".");
        File workingDirectory;
        switch (getPlatform()){
            case LINUX:
                workingDirectory = new File(userHome, ".minecraft/");
                break;
            case WINDOWS:
                final String applicationData = System.getenv("APPDATA");
                final String folder = (applicationData != null) ? applicationData : userHome;
                workingDirectory = new File(folder, ".minecraft/");
                break;
            case OSX:
                workingDirectory = new File(userHome, "Library/Application Support/minecraft");
                break;
            default:
                workingDirectory = new File(userHome, "minecraft/");
        }
        return workingDirectory;
    }
    public static void deleteDirectory(File directory) {
        if(directory.exists()){
            File[] files = directory.listFiles();
            if(files != null){
                for (File f : files) {
                    if(f.isDirectory()) {
                        deleteDirectory(f);
                    }
                    else {
                        f.delete();
                    }
                }
            }
        }
        directory.delete();
    }
    public static void openWebsite(String url) throws IOException{
        String os = System.getProperty("os.name").toLowerCase();
        Runtime rt = Runtime.getRuntime();
        if (os.contains("win")) {
            rt.exec( "rundll32 url.dll,FileProtocolHandler " + url);
        } else if (os.contains("mac")) {
            rt.exec( "open " + url);
        } else if (os.contains("nix") || os.contains("nux")) {
            String[] browsers = {"firefox", "epiphany", "mozilla", "konqueror", "netscape","opera","links","lynx", "chromium"};
            StringBuilder cmd = new StringBuilder();
            for (int i=0; i<browsers.length; i++)
                cmd.append(i==0  ? "" : " || ").append(browsers[i]).append(" \"").append(url).append("\" ");
            rt.exec(new String[] { "sh", "-c", cmd.toString() });
       }
    }
    public static boolean downloadFile(URL url, File output){
        try{
            File parent = output.getParentFile();
            if (!parent.exists()){
                parent.mkdirs();
            }
            InputStream in = url.openStream();
            FileOutputStream fo = new FileOutputStream(output);
            byte[] buffer = new byte[16384];
            int read;
            while ((read = in.read(buffer)) != -1){
                fo.write(buffer, 0, read);
            }
            in.close();
            fo.close();
            return true;
        }catch (Exception ex){
            return false;
        }
    }
    public static String getExtension(String s){
        String extension = "";
        int i = s.lastIndexOf('.');
        if (i >= 0) {
            extension = s.substring(i+1);
        }
        return extension;
    }
    public static String readURL(URL url){
        try{
            StringBuilder content = new StringBuilder();
            URLConnection con = url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream(), Charset.forName("UTF-8")));
            String line;
            while ((line = bufferedReader.readLine()) != null){
              content.append(line);
              content.append(System.lineSeparator());
            }
            bufferedReader.close();
            return content.toString();
        }catch (Exception ex){
            return null;
        }
    }
    public static boolean verifyChecksum(File file, String sha){
        try{
            MessageDigest sha1 = MessageDigest.getInstance("SHA1");
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[4096];
            int read; 
            while ((read = fis.read(data)) != -1) {
                sha1.update(data, 0, read);
            }
            byte[] hashBytes = sha1.digest();
            StringBuilder sb = new StringBuilder();
            for (byte hashByte : hashBytes) {
                sb.append(Integer.toString((hashByte & 0xff) + 0x100, 16).substring(1));
            }
            String fileHash = sb.toString();
            return fileHash.equals(sha);
        }catch (Exception ex){
            return false;
        }
    }
    public static boolean writeToFile(String o, File f){
        try {
            if (!f.getParentFile().exists()){
                f.getParentFile().mkdirs();
            }
            FileOutputStream out = new FileOutputStream(f);
            out.write(o.getBytes(Charset.forName("UTF-8")));
            out.close();
            return true;
        } catch (Exception ex) {
            return false;
        }     
    }
    public static OSArch getOSArch(){
        String arch = System.getProperty("os.arch");
        String realArch = arch.endsWith("64") ? "64" : "32";
        return (realArch.equals("32") ? OSArch.OLD : OSArch.NEW);
    }
    public static String getArtifactPath(String artifact, String ext) {
        final String[] parts = artifact.split(":", 3);
        return String.format("%s/%s/%s/%s." + ext, parts[0].replaceAll("\\.", "/"), parts[1], parts[2], parts[1] + "-" + parts[2]);
    }
    public static UUID stringToUUID(String s){return UUID.fromString(s.substring(0, 8) + "-" + s.substring(8, 12) + "-" + s.substring(12, 16) + "-" + s.substring(16, 20) + "-" + s.substring(20, s.length()));}
    public static URL stringToURL(String url){
        try{
            return new URL(url);
        }
        catch(Exception ex){
            return null;
        }
    }
    public static String sendPost(URL url, byte[] data, Map<String, String> params) throws IOException {
        URLConnection con = url.openConnection();
        con.setDoOutput(true);
        if (con instanceof HttpsURLConnection){
            ((HttpsURLConnection)con).setRequestMethod("POST");
        } else {
            ((HttpURLConnection)con).setRequestMethod("POST");
        }
        if (params.size() > 0){
            Set keys = params.keySet();
            for (Object key : keys) {
                String param = key.toString();
                con.setRequestProperty(param, params.get(param));
            }
        }
        OutputStream out = con.getOutputStream();
        out.write(data);
        out.close();
        InputStream i;
        StringBuilder response = new StringBuilder();
        try{
            i = con.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(i));
            String inputLine;
            response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
            }
            in.close();
            i.close();
        }catch (Exception ex){
            if (con instanceof HttpsURLConnection){
                i = ((HttpsURLConnection)con).getErrorStream();
            } else {
                i = ((HttpURLConnection)con).getErrorStream();
            }
            if (i != null){
                BufferedReader in = new BufferedReader(new InputStreamReader(i));
                String inputLine;
                response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                }
                in.close();
                i.close();
            }
        }
        return response.toString();
    }
    public static String getJavaDir() {
        final String separator = System.getProperty("file.separator");
        final String path = System.getProperty("java.home") + separator + "bin" + separator;
        if (getPlatform() == OS.WINDOWS && new File(path + "javaw.exe").isFile()) {
            return path + "javaw.exe";
        }
        return path + "java";
    }
    public static String fromBase64(String st){
        if (st == null || st.isEmpty()){
            return null;
        }
        String conversion;
        try{
            conversion = new String(DatatypeConverter.parseBase64Binary(st), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            conversion = null;
        }
        return conversion;
    }
    public static ImageIcon getProfileIcon(ProfileIcon p){
        BufferedImage bImg = new BufferedImage(32,32,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bImg.createGraphics();
        int blockX = 0;
        int blockY = 0;
        try {
            Image img = ImageIO.read(Constants.PROFILE_ICONS);
            switch (p){
                case GRASS:
                    blockX = 4;
                    blockY = 4;
                    break;
            }
            g.drawImage(img, 0, 0, 32, 32, blockX*136, blockY*136, blockX*136+136, blockY*136+136, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ImageIcon(bImg);
    }
}
