package kml;

import SevenZip.Compression.LZMA.Decoder;
import kml.enums.OS;
import kml.enums.OSArch;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author DarkLBP
 * website https://krothium.com
 */

public final class Utils {

    /**
     * Ignores HTTPS certificate issuer
     * @return A boolean that indicates if the custom certificate validator has been installed
     */
    public static boolean ignoreHTTPSCert() {
        try {
            SSLContext t = SSLContext.getInstance("SSL");
            t.init(null, new X509TrustManager[]{new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, null);
            HttpsURLConnection.setDefaultSSLSocketFactory(t.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((s, sslSession) -> true);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Tests if there is connectivity to the server
     */
    public static void testNetwork() {
        try {
            HttpsURLConnection con = (HttpsURLConnection) Constants.HANDSHAKE_URL.openConnection();
            int responseCode = con.getResponseCode();
            Constants.USE_LOCAL = responseCode != 204;
        } catch (IOException ex) {
            Constants.USE_LOCAL = true;
        }
    }

    /**
     * Gets the current operating system
     * @return An OS enum with the detected OS
     */
    public static OS getPlatform() {
        final String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            return OS.WINDOWS;
        } else if (osName.contains("mac")) {
            return OS.OSX;
        } else if (osName.contains("linux") || osName.contains("unix")) {
            return OS.LINUX;
        }
        return OS.UNKNOWN;
    }

    /**
     * Gets the default working directory for the launcher depending of the OS
     * @return The working directory
     */
    public static File getWorkingDirectory() {
        final String userHome = System.getProperty("user.home", ".");
        File workingDirectory;
        switch (getPlatform()) {
            case LINUX:
                workingDirectory = new File(userHome, ".minecraft/");
                break;
            case WINDOWS:
                final String applicationData = System.getenv("APPDATA");
                final String folder = applicationData != null ? applicationData : userHome;
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

    /**
     * Deletes a directory recursively
     * @param directory The target directory
     */
    public static void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) {
                        deleteDirectory(f);
                    } else {
                        f.delete();
                    }
                }
            }
        }
        directory.delete();
    }

    /**
     * Downloads a file using channel transfer
     * @param con An established connection
     * @param output The output file
     * @return A boolean that indicates if the download has completed
     */
    public static boolean downloadFile(URLConnection con, File output) {
        try {
            ReadableByteChannel rbc = Channels.newChannel(con.getInputStream());
            File parent = output.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            FileOutputStream fo = new FileOutputStream(output);
            fo.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            rbc.close();
            fo.close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Downloads a file to the cache using server's ETAG header
     * @param url The url that will be used to download the file
     * @return The path of the cached file
     */
    public static File downloadFileCached(URL url) {
        //Requires server ETAG
        //Returns the path of the cached file
        try {
            String hash = calculateChecksum(url.toString(), "SHA1");
            File output = new File(Constants.APPLICATION_CACHE, hash);
            if (!Constants.USE_LOCAL) {
                URLConnection con = url.openConnection();
                String ETag = con.getHeaderField("ETag");
                if (verifyChecksum(output, ETag.replace("\"", ""), "MD5") || downloadFile(con, output)) {
                    return output;
                }
            } else {
                if (output.exists() && output.isFile()) {
                    return output;
                }
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Downloads a file and even caches it if server ETAG is existent
     * @param url The source URL
     * @param output The output file
     * @return A boolean that indicated if the download has completed
     */
    public static boolean downloadFile(URL url, File output) {
        try {
            if (url.getProtocol().equalsIgnoreCase("file")) {
                return true;
            }
            URLConnection con = url.openConnection();
            String ETag = con.getHeaderField("ETag");
            if (output.exists() && output.isFile()) {
                //Match ETAG with existing file
                if (ETag != null && verifyChecksum(output, ETag.replace("\"", ""), "MD5")) {
                    return true;
                }
            }
            return downloadFile(con, output);
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Reads a String from the source URL
     * @param url The source URL
     * @return The read String or null if an error occurred
     */
    public static String readURL(URL url) {
        try {
            URLConnection con = null;
            StringBuilder content = new StringBuilder();
            if (url.getProtocol().startsWith("http")) {
                String hash = calculateChecksum(url.toString(), "SHA1");
                File cachedFile = new File(Constants.APPLICATION_CACHE, hash);
                if (!Constants.USE_LOCAL) {
                    con = url.openConnection();
                    String ETag = con.getHeaderField("ETag");
                    if (ETag != null) {
                        if (!verifyChecksum(cachedFile, ETag.replace("\"", ""), "MD5")) {
                            if (!downloadFile(con, cachedFile)) {
                                return null;
                            }
                        }
                    } else {
                        if (!downloadFile(con, cachedFile)) {
                            return null;
                        }
                    }
                    con = cachedFile.toURI().toURL().openConnection();
                } else {
                    if (cachedFile.exists() && cachedFile.isFile()) {
                        con = cachedFile.toURI().toURL().openConnection();
                    } else {
                        return null;
                    }
                }
            }
            if (con == null) {
                con = url.openConnection();
            }
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream(), Charset.forName("UTF-8")), 16384);
            String line;
            boolean first = true;
            while ((line = bufferedReader.readLine()) != null) {
                if (!first) {
                    content.append(System.lineSeparator());
                } else {
                    first = false;
                }
                content.append(line);
            }
            return content.toString();
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Verifies a checksum from a file
     * @param file The file to be checked
     * @param hash The hash or checksum to check
     * @param method The hash format (md5, sha1...)
     * @return A boolean that indicated if the hash matches
     */
    public static boolean verifyChecksum(File file, String hash, String method) {
        if (hash == null || method == null || !file.exists() || file.isDirectory()) {
            return false;
        }
        try {
            String fileHash = calculateChecksum(file, method);
            return hash.equals(fileHash);
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Calculates a checksum from a String
     * @param txt The input String
     * @param method The hash method (md5, sha1...)
     * @return The calculated hash
     */
    public static String calculateChecksum(String txt, String method) {
        try {
            MessageDigest sha1 = MessageDigest.getInstance(method);
            byte[] data = txt.getBytes();
            sha1.update(data);
            byte[] hashBytes = sha1.digest();
            StringBuilder sb = new StringBuilder();
            for (byte hashByte : hashBytes) {
                sb.append(Integer.toString((hashByte & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Calculates a checksum from a File
     * @param file The input File
     * @param method The hash method (md5, sha1...)
     * @return The calculated hash
     */
    public static String calculateChecksum(File file, String method) {
        try {
            MessageDigest sha1 = MessageDigest.getInstance(method);
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[16384];
            int read;
            while ((read = fis.read(data)) != -1) {
                sha1.update(data, 0, read);
            }
            byte[] hashBytes = sha1.digest();
            StringBuilder sb = new StringBuilder();
            for (byte hashByte : hashBytes) {
                sb.append(Integer.toString((hashByte & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Writes a String to a File
     * @param o The String to be written
     * @param f The output File
     * @return A boolean that indicated if the text has been written
     */
    public static boolean writeToFile(String o, File f) {
        try {
            if (!f.getParentFile().exists()) {
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

    /**
     * Checks whether the current OS arch is 32 or 64 bits
     * @return An OSArch enum with the detected architecture
     */
    public static OSArch getOSArch() {
        String arch = System.getProperty("os.arch");
        String realArch = arch.endsWith("64") ? "64" : "32";
        return (realArch.equals("32") ? OSArch.OLD : OSArch.NEW);
    }

    /**
     * Gets a real path from an artifact path
     * @param artifact The artifact path
     * @param ext The extension
     * @return The real path
     */
    public static String getArtifactPath(String artifact, String ext) {
        final String[] parts = artifact.split(":", 3);
        return String.format("%s/%s/%s/%s." + ext, parts[0].replaceAll("\\.", "/"), parts[1], parts[2], parts[1] + "-" + parts[2]);
    }

    /**
     * Converts a String to an URL
     * @param url Input url String
     * @return Return the URL or null if it is not a valid URL
     */
    public static URL stringToURL(String url) {
        try {
            return new URL(url);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Sends a post to the desired target
     * @param url The POST URL
     * @param data The data to be sent
     * @param params The headers to be sent
     * @return The response of the server
     * @throws IOException If connection failed
     */
    public static String sendPost(URL url, byte[] data, Map<String, String> params) throws IOException {
        URLConnection con = url.openConnection();
        con.setDoOutput(true);
        if (con instanceof HttpsURLConnection) {
            ((HttpsURLConnection) con).setRequestMethod("POST");
        } else {
            ((HttpURLConnection) con).setRequestMethod("POST");
        }
        if (params.size() > 0) {
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
        try {
            i = con.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(i));
            String inputLine;
            response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            i.close();
        } catch (Exception ex) {
            if (con instanceof HttpsURLConnection) {
                i = ((HttpsURLConnection) con).getErrorStream();
            } else {
                i = ((HttpURLConnection) con).getErrorStream();
            }
            if (i != null) {
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

    /**
     * Gets the java executable path
     * @return The java executable path
     */
    public static String getJavaDir() {
        final String separator = System.getProperty("file.separator");
        final String path = System.getProperty("java.home") + separator + "bin" + separator;
        if (getPlatform() == OS.WINDOWS && new File(path + "javaw.exe").isFile()) {
            return path + "javaw.exe";
        }
        return path + "java";
    }

    /**
     * Converts a Base64 String to text
     * @param st The Base64 String
     * @return The decoded text
     */
    public static String fromBase64(String st) {
        if (st == null || st.isEmpty()) {
            return null;
        }
        String conversion;
        try {
            conversion = new String(Base64.getDecoder().decode(st), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            conversion = "";
        }
        return conversion;
    }

    /**
     * Unpacks a LZMA file
     * @param input The LZMA file
     * @param output The output file
     * @throws IOException If an error occurred
     */
    public static void decompressLZMA(File input, File output) throws IOException {
        Decoder LZMADecoder = new SevenZip.Compression.LZMA.Decoder();
        try (FileInputStream in = new FileInputStream(input);
             FileOutputStream out = new FileOutputStream(output)){
            // Read the decoder properties
            byte[] properties = new byte[5];
            in.read(properties, 0, 5);

            // Read in the decompress file size.
            byte [] fileLengthBytes = new byte[8];
            in.read(fileLengthBytes, 0, 8);
            ByteBuffer bb = ByteBuffer.wrap( fileLengthBytes );
            bb.order(ByteOrder.LITTLE_ENDIAN);
            long fileLength = bb.getLong();

            LZMADecoder.SetDecoderProperties(properties);
            LZMADecoder.Code(in, out, fileLength);
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * Decompresses a ZIP file
     * @param input The ZIP file
     * @param output The output directory
     * @param exclusions Any extraction exclusions
     * @throws IOException If the process failed
     */
    public static void decompressZIP(File input, File output, List<String> exclusions) throws IOException {
        if(!output.exists() || !output.isDirectory()){
            output.mkdir();
        }
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(input))){
            ZipEntry ze = zis.getNextEntry();
            while(ze != null){
                String fileName = ze.getName();
                File newFile = new File(output + File.separator + fileName);
                if (ze.isDirectory()) {
                    newFile.mkdir();
                } else {
                    boolean excluded = false;
                    if (exclusions != null) {
                        for (String e : exclusions) {
                            if (fileName.startsWith(e)) {
                                excluded = true;
                            }
                        }
                    }
                    if (excluded) {
                        zis.closeEntry();
                        ze = zis.getNextEntry();
                        continue;
                    }
                    byte[] buffer = new byte[16384];
                    new File(newFile.getParent()).mkdirs();
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            new File(output, "OK").createNewFile();
        } catch (Exception ex) {
            throw ex;
        }
    }
}
