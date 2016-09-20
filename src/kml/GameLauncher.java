package kml;

import kml.enums.OSArch;
import kml.objects.Version;
import kml.objects.Library;
import kml.objects.Profile;
import kml.objects.User;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import kml.exceptions.GameLauncherException;
import org.json.JSONObject;

/**
 * @website https://krothium.com
 * @author DarkLBP
 */

public class GameLauncher {
    
    private final Console console;
    private Process process = null;
    private final Kernel kernel;
    
    public GameLauncher(Kernel k){
        this.kernel = k;
        this.console = k.getConsole();
    }
    public void launch() throws GameLauncherException{
        Profile p = kernel.getProfile(kernel.getSelectedProfile());
        if (this.isStarted()){
            throw new GameLauncherException("Game is already started!");
        }
        String verID;
        Version ver;
        if (p.hasVersion()){
            verID = p.getVersionID();
        } else {
            verID = kernel.getLatestVersion();
        }
        ver = kernel.getVersion(verID);
        File workingDir = kernel.getWorkingDir();
        console.printInfo("Deleting old natives.");
        File nativesRoot = new File(workingDir + File.separator + "versions" + File.separator + ver.getID());
        if (nativesRoot.exists()){
            if (nativesRoot.isDirectory()){
                File[] files = nativesRoot.listFiles();
                for (File f : files){
                    if (f.isDirectory() && f.getName().contains("natives")){
                        Utils.deleteDirectory(f);
                    }
                }
            }
        }
        final File nativesDir = new File(workingDir + File.separator + "versions" + File.separator + ver.getID() + File.separator + ver.getID() + "-natives-" + System.nanoTime());
        if (!nativesDir.exists() || !nativesDir.isDirectory()){
            nativesDir.mkdirs();
        }
        console.printInfo("Launching Minecraft " + ver.getID() + " on " + workingDir.getAbsolutePath());
        console.printInfo("Using natives dir: " + nativesDir);
        console.printInfo("Exctracting natives.");
        List<String> gameArgs = new ArrayList();
        if (p.hasJavaDir()){
            gameArgs.add(p.getJavaDir().getAbsolutePath());
        } else {
            gameArgs.add(Utils.getJavaDir());
        }
        if (!p.hasJavaArgs()){
            if (Utils.getOSArch().equals(OSArch.OLD)){
                gameArgs.add("-Xmx512M");
            }else{
                gameArgs.add("-Xmx1G");
            }
            gameArgs.add("-XX:+UseConcMarkSweepGC");
            gameArgs.add("-XX:+CMSIncrementalMode");
            gameArgs.add("-XX:-UseAdaptiveSizePolicy");
            gameArgs.add("-Xmn128M");
        }else{
            String javaArgs = p.getJavaArgs();
            String[] args = javaArgs.split(" ");
            for (String arg : args){
                gameArgs.add(arg);
            }
        }
        gameArgs.add("-Djava.library.path=" + nativesDir.getAbsolutePath());
        gameArgs.add("-cp");
        String libraries = "\"";
        List<Library> libs = ver.getLibraries();
        String separator = System.getProperty("path.separator");
        try {
            File launchPath = new File(GameLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            libraries += launchPath.getAbsolutePath() + separator;
        } catch (URISyntaxException ex) {
            console.printError("Failed to load GameStarter.");
        }
        for (Library lib : libs){
            if (lib.isCompatible()){
                if (lib.isNative()){
                    try {
                        File completePath = new File(kernel.getWorkingDir() + File.separator + lib.getRelativeNativePath());
                        ZipFile zip = new ZipFile(completePath);
                        final Enumeration<? extends ZipEntry> entries = zip.entries();
                        while (entries.hasMoreElements()) {
                            final ZipEntry entry = (ZipEntry)entries.nextElement();
                            if (entry.isDirectory()) {
                                continue;
                            }
                            final File targetFile = new File(nativesDir, entry.getName());
                            List<String> exclude = lib.getExtractExclusions();
                            boolean excluded = false;
                            for (String e : exclude){
                                if (entry.getName().startsWith(e)){
                                    excluded = true;
                                }
                            }
                            if (excluded){
                                continue;
                            }
                            final BufferedInputStream inputStream = new BufferedInputStream(zip.getInputStream(entry));
                            final byte[] buffer = new byte[2048];
                            final FileOutputStream outputStream = new FileOutputStream(targetFile);
                            final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
                            int length;
                            while ((length = inputStream.read(buffer, 0, buffer.length)) != -1) {
                                bufferedOutputStream.write(buffer, 0, length);
                            }
                            bufferedOutputStream.close();
                            outputStream.close();
                            inputStream.close();
                        }
                        zip.close();
                    } catch (IOException ex) {
                        console.printError("Failed to extract native: " + lib.getName());
                    }
                } else {
                    File completePath = new File(kernel.getWorkingDir() + File.separator + lib.getRelativePath());
                    libraries += completePath.getAbsolutePath() + separator;
                }
            }
        }
        console.printInfo("Preparing game args.");
        File verPath = new File(kernel.getWorkingDir() + File.separator + ver.getRelativeJar());
        libraries += verPath.getAbsolutePath() + "\"";
        String assetsID = ver.getAssets();
        File assetsDir;
        File assetsRoot = new File(workingDir + File.separator + "assets");
        if (assetsID.equals("legacy")){
            assetsDir = new File(assetsRoot + File.separator + "virtual" + File.separator + "legacy");
            if (!assetsDir.exists() || !assetsDir.isDirectory()){
                assetsDir.mkdirs();
            }
            console.printInfo("Building virtual asset folder.");
            File indexJSON = new File(assetsRoot + File.separator + "indexes" + File.separator + assetsID + ".json");
            try {
                JSONObject o = new JSONObject(new String(Files.readAllBytes(indexJSON.toPath()), "ISO-8859-1"));
                JSONObject objects = o.getJSONObject("objects");
                Set s = objects.keySet();
                Iterator it = s.iterator();
                while (it.hasNext()){
                    String name = it.next().toString();
                    File assetFile = new File(assetsDir + File.separator + name);
                    JSONObject asset = objects.getJSONObject(name);
                    long size = asset.getLong("size");
                    String sha = asset.getString("hash");
                    boolean valid = false;
                    if (assetFile.exists()){
                        if (assetFile.length() == size && Utils.verifyChecksum(assetFile, sha)){
                            valid = true;
                        }
                    }
                    if (!valid){
                        File objectFile = new File(assetsRoot + File.separator + "objects" + File.separator + sha.substring(0,2) + File.separator + sha);
                        if (assetFile.getParentFile() != null){
                            assetFile.getParentFile().mkdirs();
                        }
                        Files.copy(objectFile.toPath(), assetFile.toPath());
                    }
                }
            } catch (Exception ex) {
                console.printError("Failed to create virtual asset folder.");
            }
        }else{
            assetsDir = assetsRoot;
        }
        gameArgs.add(libraries);
        gameArgs.add("kml.GameStarter");
        gameArgs.add(ver.getMainClass());
        console.printInfo("Full game launcher parameters: ");
        for (String arg : gameArgs){
            console.printInfo(arg);
        }
        Authentication a = kernel.getAuthentication();
        User u = a.getSelectedUser();
        String versionArgs = ver.getMinecraftArguments();
        versionArgs = versionArgs.replace("${auth_player_name}", u.getDisplayName());
        versionArgs = versionArgs.replace("${version_name}", ver.getID());
        if (p.hasGameDir()){
            File gameDir = p.getGameDir();
            if (!gameDir.exists() || !gameDir.isDirectory()){
                gameDir.mkdirs();
            }
            versionArgs = versionArgs.replace("${game_directory}", "\"" + gameDir.getAbsolutePath() + "\"");
        }else{
            versionArgs = versionArgs.replace("${game_directory}", "\"" + workingDir.getAbsolutePath() + "\"");
        }
        versionArgs = versionArgs.replace("${assets_root}", "\"" + assetsDir.getAbsolutePath() + "\"");
        versionArgs = versionArgs.replace("${game_assets}", "\"" + assetsDir.getAbsolutePath() + "\"");
        versionArgs = versionArgs.replace("${assets_index_name}", assetsID);
        versionArgs = versionArgs.replace("${auth_uuid}", u.getProfileID().toString().replaceAll("-", ""));
        versionArgs = versionArgs.replace("${auth_access_token}", u.getAccessToken());
        versionArgs = versionArgs.replace("${version_type}", ver.getType().name());
        if (u.hasProperties()){
            Map<String, String> properties = u.getProperties();
            Set set = properties.keySet();
            Iterator it = set.iterator();
            JSONObject props = new JSONObject();
            while (it.hasNext()){
                String name = it.next().toString();
                String value = properties.get(name);
                props.put(name, value);
            }
            versionArgs = versionArgs.replace("${user_properties}", props.toString());
        }else{
            versionArgs = versionArgs.replace("${user_properties}", "{}");
        }
        versionArgs = versionArgs.replace("${user_type}", "mojang");
        versionArgs = versionArgs.replace("${auth_session}", "token:" + u.getAccessToken() + ":" + u.getProfileID().toString().replaceAll("-", ""));
        String[] argsSplit = versionArgs.split(" ");
        for (String arg : argsSplit){
            gameArgs.add(arg);
        }
        if (p.hasResolution()){
            gameArgs.add("--width");
            gameArgs.add(String.valueOf(p.getResolutionWidth()));
            gameArgs.add("--height");
            gameArgs.add(String.valueOf(p.getResolutionHeight()));
        }
        ProcessBuilder pb = new ProcessBuilder(gameArgs);
        pb.directory(workingDir);
        try{
            this.process = pb.start();
            Thread log_info = new Thread(){
                @Override
                public void run(){
                    InputStreamReader isr = new InputStreamReader(getInputStream());
                    BufferedReader br = new BufferedReader(isr);
                    String lineRead;
                    try{
                        while (isStarted()){
                            if ((lineRead = br.readLine()) != null){
                                console.printInfo(lineRead);
                            }
                        }
                    } catch (Exception ex){
                        console.printError("Game stopped unexpectedly.");
                    }
                    console.printInfo("Deleteting natives dir.");
                    Utils.deleteDirectory(nativesDir);
                }
            };
            log_info.start();
            Thread log_error = new Thread(){
                @Override
                public void run(){
                    InputStreamReader isr = new InputStreamReader(getErrorStream());
                    BufferedReader br = new BufferedReader(isr);
                    String lineRead;
                    try{
                        while (isStarted()){
                            if ((lineRead = br.readLine()) != null){
                                console.printInfo(lineRead);
                            }
                        }
                    } catch (Exception ex){}
                }
            };
            log_error.start();
        }catch (Exception ex){
            console.printError("Game returned an error code.");
        }
    }
    public boolean isStarted(){
        try {
            if (this.process != null){
                this.process.exitValue();
                this.process = null;
            }
            return false;
        } catch (Exception ex){
            return true;
        }
    }
    public InputStream getInputStream(){return this.process.getInputStream();}
    public InputStream getErrorStream(){return this.process.getErrorStream();}
}
