package kmlk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.UUID;
import javax.net.ssl.HttpsURLConnection;

/**
 * @website http://krotium.com
 * @author DarkLBP
 */

public class Utils {
    public static OS getPlatform()
    {
        final String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win"))
        {
            return OS.WINDOWS;
        }
        else if (osName.contains("mac"))
        {
            return OS.OSX;
        }
        else if (osName.contains("linux") || osName.contains("unix"))
        {
            return OS.LINUX;
        }
        return OS.UNKNOWN;
    }
    public static File getWorkingDirectory()
    {
        final String userHome = System.getProperty("user.home", ".");
        File workingDirectory;
        switch (getPlatform())
        {
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
    public static boolean downloadFile(URL url, File output)
    {
        try
        {
            File parent = output.getParentFile();
            if (!parent.exists())
            {
                parent.mkdirs();
            }
            ReadableByteChannel rb = Channels.newChannel(url.openStream());
            FileOutputStream fo = new FileOutputStream(output);
            fo.getChannel().transferFrom(rb, 0, Long.MAX_VALUE);
            return true;
        }
        catch (Exception ex)
        {
            return false;
        }
    }
    public static String readURL(URL url)
    {
        try
        {
            StringBuilder content = new StringBuilder();
            URLConnection con = url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null)
            {
              content.append(line);
              content.append(System.lineSeparator());
            }
            bufferedReader.close();
            return content.toString();
        }
        catch (Exception ex)
        {
            return null;
        }
    }
    public static boolean verifyChecksum(File file, String sha)
    {
        try
        {
            MessageDigest sha1 = MessageDigest.getInstance("SHA1");
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[4096];
            int read; 
            while ((read = fis.read(data)) != -1) {
                sha1.update(data, 0, read);
            }
            byte[] hashBytes = sha1.digest();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < hashBytes.length; i++) {
              sb.append(Integer.toString((hashBytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            String fileHash = sb.toString();
            return fileHash.equals(sha);
        }
        catch (Exception ex)
        {
            return false;
        }
    }
    public static boolean writeToFile(String o, File f)
    {
        try {
         FileOutputStream out = new FileOutputStream(f);
         out.write(o.getBytes());
         out.close();
         return true;
        } catch (Exception ex) {
            return false;
        }     
    }
    public static OSArch getOSArch()
    {
        String arch = System.getenv("PROCESSOR_ARCHITECTURE");
        String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");
        String realArch = arch.endsWith("64") || wow64Arch != null && wow64Arch.endsWith("64") ? "64" : "32";
        return (realArch.equals("32") ? OSArch.OLD : OSArch.NEW);
    }
    public static File getArtifactFile(String artifact, String ext) {

        final String[] parts = artifact.split(":", 3);
        return new File(String.format("%s/%s/%s/%s." + ext, parts[0].replaceAll("\\.", "/"), parts[1], parts[2], parts[1] + "-" + parts[2]));
    }
    public static UUID stringToUUID(String s)
    {
        return UUID.fromString(s.substring(0, 8) + "-" + s.substring(8, 12) + "-" + s.substring(12, 16) + "-" + s.substring(16, 20) + "-" + s.substring(20, s.length()));
    }
    public static URL stringToURL(String url)
    {
        try
        {
            return new URL(url);
        }
        catch(Exception ex){}
        return null;
    }
    public static String sendJSONPost(URL url, String data) throws Exception {
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        byte[] postAsBytes = data.getBytes(Charset.forName("UTF-8"));
        con.setDoOutput(true);
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        con.setRequestProperty("Content-Length", "" + postAsBytes.length);
        OutputStream out = con.getOutputStream();
        out.write(postAsBytes);
        out.close();
        int responseCode = con.getResponseCode();
        InputStream i = null;
        StringBuilder response = new StringBuilder();
        try
        {
            i = con.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(i));
            String inputLine;
            response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
            }
            in.close();
            i.close();
        }
        catch (Exception ex)
        {
            i = con.getErrorStream();
            if (i != null)
            {
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
}
