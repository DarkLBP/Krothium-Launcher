package kml;

import SevenZip.Compression.LZMA.Decoder;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import kml.enums.OS;
import kml.enums.OSArch;
import kml.enums.ProfileIcon;

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

public class Utils {
    private static final HashMap<ProfileIcon, Image> ICON_CACHE = new HashMap<>();

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

    public static void testNetwork() {
        try {
            HttpsURLConnection con = (HttpsURLConnection) Constants.HANDSHAKE_URL.openConnection();
            int responseCode = con.getResponseCode();
            Constants.USE_LOCAL = responseCode != 204;
        } catch (IOException ex) {
            Constants.USE_LOCAL = true;
        }
    }

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

    public static File getWorkingDirectory() {
        final String userHome = System.getProperty("user.home", ".");
        File workingDirectory;
        switch (getPlatform()) {
            case LINUX:
                workingDirectory = new File(userHome, ".minecraft/");
                break;
            case WINDOWS:
                final String applicationData = System.getenv("APPDATA");
                final String folder = Objects.nonNull(applicationData) ? applicationData : userHome;
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
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (Objects.nonNull(files)) {
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


    public static boolean downloadFile(URLConnection con, File output) {
        try {
            ReadableByteChannel rbc = Channels.newChannel(con.getInputStream());
            File parent = output.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
            FileOutputStream fo = new FileOutputStream(output);
            fo.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            rbc.close();
            fo.close();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

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
            ex.printStackTrace();
            return false;
        }
    }

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

    public static OSArch getOSArch() {
        String arch = System.getProperty("os.arch");
        String realArch = arch.endsWith("64") ? "64" : "32";
        return (realArch.equals("32") ? OSArch.OLD : OSArch.NEW);
    }

    public static String getArtifactPath(String artifact, String ext) {
        final String[] parts = artifact.split(":", 3);
        return String.format("%s/%s/%s/%s." + ext, parts[0].replaceAll("\\.", "/"), parts[1], parts[2], parts[1] + "-" + parts[2]);
    }

    public static URL stringToURL(String url) {
        try {
            return new URL(url);
        } catch (Exception ex) {
            return null;
        }
    }

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
            while (Objects.nonNull((inputLine = in.readLine()))) {
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
            if (Objects.nonNull(i)) {
                BufferedReader in = new BufferedReader(new InputStreamReader(i));
                String inputLine;
                response = new StringBuilder();
                while (Objects.nonNull((inputLine = in.readLine()))) {
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

    public static Image getProfileIcon(ProfileIcon p) {
        if (ICON_CACHE.containsKey(p)) {
            return ICON_CACHE.get(p);
        }
        WritableImage wi = new WritableImage(136, 136);
        PixelWriter pw = wi.getPixelWriter();
        int blockX = 0;
        int blockY = 0;
        try {
            PixelReader pr = Constants.PROFILE_ICONS.getPixelReader();
            switch (p) {
                case LEAVES_OAK:
                    blockX = 0;
                    blockY = 0;
                    break;
                case BEDROCK:
                    blockX = 1;
                    blockY = 0;
                    break;
                case CLAY:
                    blockX = 2;
                    blockY = 0;
                    break;
                case DIAMOND_BLOCK:
                    blockX = 3;
                    blockY = 0;
                    break;
                case END_STONE:
                    blockX = 4;
                    blockY = 0;
                    break;
                case GRAVEL:
                    blockX = 5;
                    blockY = 0;
                    break;
                case LOG_BIRCH:
                    blockX = 6;
                    blockY = 0;
                    break;
                case PLANKS_OAK:
                    blockX = 7;
                    blockY = 0;
                    break;
                case TNT:
                    blockX = 8;
                    blockY = 0;
                    break;
                case BRICK:
                    blockX = 0;
                    blockY = 1;
                    break;
                case CHEST:
                    blockX = 1;
                    blockY = 1;
                    break;
                case COAL_BLOCK:
                    blockX = 2;
                    blockY = 1;
                    break;
                case DIAMOND_ORE:
                    blockX = 3;
                    blockY = 1;
                    break;
                case FARMLAND:
                    blockX = 4;
                    blockY = 1;
                    break;
                case HARDENED_CLAY:
                    blockX = 5;
                    blockY = 1;
                    break;
                case LOG_DARKOAK:
                    blockX = 6;
                    blockY = 1;
                    break;
                case PLANKS_SPRUCE:
                    blockX = 7;
                    blockY = 1;
                    break;
                case WOOL:
                    blockX = 8;
                    blockY = 1;
                    break;
                case COAL_ORE:
                    blockX = 0;
                    blockY = 2;
                    break;
                case COBBLESTONE:
                    blockX = 1;
                    blockY = 2;
                    break;
                case CRAFTING_TABLE: //Default for Latest Snapshot
                    blockX = 2;
                    blockY = 2;
                    break;
                case DIRT:
                    blockX = 3;
                    blockY = 2;
                    break;
                case FURNACE: //Default for custom profiles
                    blockX = 4;
                    blockY = 2;
                    break;
                case ICE_PACKED:
                    blockX = 5;
                    blockY = 2;
                    break;
                case LOG_JUNGLE:
                    blockX = 6;
                    blockY = 2;
                    break;
                case QUARTZ_ORE:
                    blockX = 7;
                    blockY = 2;
                    break;
                case DIRT_PODZOL:
                    blockX = 0;
                    blockY = 3;
                    break;
                case DIRT_SNOW:
                    blockX = 1;
                    blockY = 3;
                    break;
                case EMERALD_BLOCK:
                    blockX = 2;
                    blockY = 3;
                    break;
                case EMERALD_ORE:
                    blockX = 3;
                    blockY = 3;
                    break;
                case FURNACE_ON:
                    blockX = 4;
                    blockY = 3;
                    break;
                case IRON_BLOCK:
                    blockX = 5;
                    blockY = 3;
                    break;
                case LOG_OAK:
                    blockX = 6;
                    blockY = 3;
                    break;
                case RED_SAND:
                    blockX = 7;
                    blockY = 3;
                    break;
                case GLASS:
                    blockX = 0;
                    blockY = 4;
                    break;
                case GLOWSTONE:
                    blockX = 1;
                    blockY = 4;
                    break;
                case GOLD_BLOCK:
                    blockX = 2;
                    blockY = 4;
                    break;
                case GOLD_ORE:
                    blockX = 3;
                    blockY = 4;
                    break;
                case GRASS: //Default for Latest Release
                    blockX = 4;
                    blockY = 4;
                    break;
                case IRON_ORE:
                    blockX = 5;
                    blockY = 4;
                    break;
                case LOG_SPRUCE:
                    blockX = 6;
                    blockY = 4;
                    break;
                case RED_SANDSTONE:
                    blockX = 7;
                    blockY = 4;
                    break;
                case LAPIS_ORE:
                    blockX = 0;
                    blockY = 5;
                    break;
                case LEAVES_BIRCH:
                    blockX = 1;
                    blockY = 5;
                    break;
                case LEAVES_JUNGLE:
                    blockX = 2;
                    blockY = 5;
                    break;
                case BOOKSHELF:
                    blockX = 3;
                    blockY = 5;
                    break;
                case LEAVES_SPRUCE:
                    blockX = 4;
                    blockY = 5;
                    break;
                case LOG_ACACIA:
                    blockX = 5;
                    blockY = 5;
                    break;
                case MYCELIUM:
                    blockX = 6;
                    blockY = 5;
                    break;
                case REDSTONE_BLOCK:
                    blockX = 7;
                    blockY = 5;
                    break;
                case NETHER_BRICK:
                    blockX = 0;
                    blockY = 6;
                    break;
                case NETHERRACK:
                    blockX = 1;
                    blockY = 6;
                    break;
                case OBSIDIAN:
                    blockX = 2;
                    blockY = 6;
                    break;
                case PLANKS_ACACIA:
                    blockX = 3;
                    blockY = 6;
                    break;
                case PLANKS_BIRCH:
                    blockX = 4;
                    blockY = 6;
                    break;
                case PLANKS_DARKOAK:
                    blockX = 5;
                    blockY = 6;
                    break;
                case PLANKS_JUNGLE:
                    blockX = 6;
                    blockY = 6;
                    break;
                case REDSTONE_ORE:
                    blockX = 7;
                    blockY = 6;
                    break;
                case SAND:
                    blockX = 0;
                    blockY = 7;
                    break;
                case SANDSTONE:
                    blockX = 1;
                    blockY = 7;
                    break;
                case SNOW:
                    blockX = 2;
                    blockY = 7;
                    break;
                case SOUL_SAND:
                    blockX = 3;
                    blockY = 7;
                    break;
                case STONE:
                    blockX = 4;
                    blockY = 7;
                    break;
                case STONE_ANDESITE:
                    blockX = 5;
                    blockY = 7;
                    break;
                case STONE_DIORITE:
                    blockX = 6;
                    blockY = 7;
                    break;
                case STONE_GRANITE:
                    blockX = 7;
                    blockY = 7;
                    break;
            }

            pw.setPixels(0, 0, 136, 136, pr, blockX * 136, blockY * 136);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ICON_CACHE.put(p, wi);
        return wi;
    }

    public static Image resampleImage(Image input, int scaleFactor) {
        final int W = (int) input.getWidth();
        final int H = (int) input.getHeight();

        WritableImage output = new WritableImage(
                W * scaleFactor,
                H * scaleFactor
        );

        PixelReader reader = input.getPixelReader();
        PixelWriter writer = output.getPixelWriter();

        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                final int argb = reader.getArgb(x, y);
                for (int dy = 0; dy < scaleFactor; dy++) {
                    for (int dx = 0; dx < scaleFactor; dx++) {
                        writer.setArgb(x * scaleFactor + dx, y * scaleFactor + dy, argb);
                    }
                }
            }
        }

        return output;
    }

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
