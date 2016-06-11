package kmlk;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.json.JSONObject;

/**
 * @website http://krotium.com
 * @author DarkLBP
 */

public class GameLauncher {
    
    private final Kernel kernel;
    private final Console console;
    public GameLauncher(Kernel k)
    {
        this.kernel = k;
        this.console = k.getConsole();
    }
    public void launch(Profile p)
    {
        Version ver = p.getVersion();
        File workingDir = this.kernel.getWorkingDir();
        File nativesDir = new File(workingDir + File.separator + "versions" + File.separator + ver.getID() + File.separator + ver.getID() + "-natives-" + System.nanoTime());
        if (!nativesDir.exists() || !nativesDir.isDirectory())
        {
            nativesDir.mkdirs();
        }
        console.printInfo("Launching Minecraft " + ver.getID() + " on " + workingDir.getAbsolutePath());
        console.printInfo("Using natives dir: " + nativesDir);
        console.printInfo("Exctracting natives.");
        List<String> gameArgs = new ArrayList();
        gameArgs.add(Utils.getJavaDir());
        gameArgs.add("-Djava.library.path=" + nativesDir.getAbsolutePath());
        gameArgs.add("-cp");
        String libraries = "";
        Version v = ver;
        while (v != null)
        {
            if (v.hasNatives())
            {
                Map<String, Native> nats = v.getNatives();
                Set set = nats.keySet();
                Iterator it = set.iterator();
                while (it.hasNext())
                {
                    String nat_name = it.next().toString();
                    Native nat = nats.get(nat_name);
                    File completePath = new File(kernel.getWorkingDir() + File.separator + nat.getPath());
                    try {
                        ZipFile zip = new ZipFile(completePath);
                        final Enumeration<? extends ZipEntry> entries = zip.entries();
                        while (entries.hasMoreElements()) {
                            final ZipEntry entry = (ZipEntry)entries.nextElement();
                            if (entry.isDirectory()) {
                                continue;
                            }
                            final File targetFile = new File(nativesDir, entry.getName());
                            List<String> exclude = nat.getExclusions();
                            boolean excluded = false;
                            for (String e : exclude)
                            {
                                if (entry.getName().startsWith(e))
                                {
                                    excluded = true;
                                }
                            }
                            if (excluded)
                            {
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
                    } catch (IOException ex) {
                        console.printError("Failed to extract native: " + nat_name);
                    }
                }
            }
            if (v.hasInheritedVersion())
            {
                v = v.getInheritedVersion();
            }
            else
            {
                v = null;
            }
        }
        console.printInfo("Preparing game args.");
        Version v2 = ver;
        while (v2 != null)
        {
            if (v2.hasLibraries())
            {
                Map<String, Library> libs = v2.getLibraries();
                Set set = libs.keySet();
                Iterator it = set.iterator();
                while (it.hasNext())
                {
                    String lib_name = it.next().toString();
                    Library lib = libs.get(lib_name);
                    File completePath = new File(kernel.getWorkingDir() + File.separator + lib.getPath());
                    libraries += completePath.getAbsolutePath() + ";";
                }
            }
            if (v2.hasInheritedVersion())
            {
                v2 = v2.getInheritedVersion();
            }
            else
            {
                v2 = null;
            }
        }
        File verPath = new File(kernel.getWorkingDir() + File.separator + ver.getPath());
        libraries += verPath.getAbsolutePath();
        String assetsID = ver.getAssetID();
        File assetsDir;
        File assetsRoot = new File(workingDir + File.separator + "assets");
        if (assetsID.equals("legacy"))
        {
            assetsDir = new File(assetsRoot + File.separator + "virtual" + File.separator + "legacy");
            if (!assetsDir.exists() || !assetsDir.isDirectory())
            {
                assetsDir.mkdirs();
            }
            console.printInfo("Building virtual asset folder.");
            File indexJSON = new File(assetsRoot + File.separator + "indexes" + File.separator + assetsID + ".json");
            try {
                JSONObject o = new JSONObject(new String(Files.readAllBytes(indexJSON.toPath()), "ISO-8859-1"));
                JSONObject objects = o.getJSONObject("objects");
                Set s = objects.keySet();
                Iterator it = s.iterator();
                while (it.hasNext())
                {
                    String name = it.next().toString();
                    File assetFile = new File(assetsDir + File.separator + name);
                    JSONObject asset = objects.getJSONObject(name);
                    long size = asset.getLong("size");
                    String sha = asset.getString("hash");
                    boolean valid = false;
                    if (assetFile.exists())
                    {
                        if (assetFile.length() == size && Utils.verifyChecksum(assetFile, sha))
                        {
                            valid = true;
                        }
                    }
                    if (!valid)
                    {
                        File objectFile = new File(assetsRoot + File.separator + "objects" + File.separator + sha.substring(0,2) + File.separator + sha);
                        if (assetFile.getParentFile() != null)
                        {
                            assetFile.getParentFile().mkdirs();
                        }
                        Files.copy(objectFile.toPath(), assetFile.toPath());
                    }
                }
            } catch (Exception ex) {
                console.printError("Failed to create virtual asset folder.");
            }
        }
        else
        {
            assetsDir = assetsRoot;
        }
        gameArgs.add(libraries);
        gameArgs.add(ver.getMainClass());
        Authentication a = kernel.getAuthentication();
        User u = a.getSelectedUser();
        String versionArgs = ver.getArguments();
        versionArgs = versionArgs.replace("${auth_player_name}", u.getDisplayName());
        versionArgs = versionArgs.replace("${version_name}", ver.getID());
        versionArgs = versionArgs.replace("${game_directory}", workingDir.getAbsolutePath());
        versionArgs = versionArgs.replace("${assets_root}", assetsDir.getAbsolutePath());
        versionArgs = versionArgs.replace("${game_assets}", assetsDir.getAbsolutePath());
        versionArgs = versionArgs.replace("${assets_index_name}", assetsID);
        versionArgs = versionArgs.replace("${auth_uuid}", u.getProfileID().toString());
        versionArgs = versionArgs.replace("${auth_access_token}", u.getAccessToken());
        versionArgs = versionArgs.replace("${version_type}", ver.getType().name());
        if (u.hasProperties())
        {
            Map<String, String> properties = u.getProperties();
            Set set = properties.keySet();
            Iterator it = set.iterator();
            JSONObject props = new JSONObject();
            while (it.hasNext())
            {
                String name = it.next().toString();
                String value = properties.get(name);
                props.put(name, value);
            }
            versionArgs = versionArgs.replace("${user_properties}", props.toString());
        }
        else
        {
            versionArgs = versionArgs.replace("${user_properties}", "{}");
        }
        versionArgs = versionArgs.replace("${user_type}", "mojang");
        versionArgs = versionArgs.replace("${auth_session}", "token:" + u.getAccessToken() + ":" + u.getProfileID().toString().replaceAll("-", ""));
        String[] argsSplit = versionArgs.split(" ");
        for (int i = 0; i < argsSplit.length; i++)
        {
            gameArgs.add(versionArgs);
        }
        ProcessBuilder pb = new ProcessBuilder(gameArgs);
        try
        {
            Process process = pb.start();
            process.waitFor();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
