package kmlk;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @website http://krotium.com
 * @author DarkLBP
 */

public final class Version {
    private final String id;
    private final VersionType type;
    private final VersionOrigin origin;
    private final Map<VersionOrigin, URL> url = new HashMap();
    private final File path;
    private JSONObject versionMeta;
    private Version inheritedVersion;
    private final Map<String, Library> libraries = new HashMap();
    private final Map<String, Native> natives = new HashMap();
    private final Console console;
    private boolean prepared = false;
    private AssetIndex assetIndex;
    private String assets;
    private Version rootVer;
    public String minecraftArguments = null;
    public String mainClass = null;
    
    public Version(String id, VersionType type, VersionOrigin or, URL url)
    {
        this.id = id;
        this.type = type;
        this.url.put(or, url);
        this.origin = or;
        this.console = Kernel.getKernel().getConsole();
        this.path = new File("versions" + File.separator + id + File.separator + id + ".jar");
    }
    public boolean prepare()
    {
        try
        {
            this.versionMeta = new JSONObject(Utils.readURL(this.getURL((this.getOrigin() != VersionOrigin.REMOTE) ? VersionOrigin.LOCAL : VersionOrigin.REMOTE)));
            if (this.getOrigin() == VersionOrigin.LOCAL)
            {
                if (this.versionMeta.has("inheritsFrom"))
                {
                    this.inheritedVersion = Kernel.getKernel().getVersions().getVersionByName(this.versionMeta.getString("inheritsFrom"));
                    if (this.inheritedVersion != null)
                    {
                        if (!this.inheritedVersion.isPrepared())
                        {
                            this.inheritedVersion.prepare();
                        }
                        console.printInfo("Version " + this.getID() + " inherits from version " + this.inheritedVersion.getID());
                    }
                }
                else
                {
                    this.inheritedVersion = null;
                }
            }
            else
            {
                this.inheritedVersion = null;
            }
            console.printInfo("Fetching required versions libraries.");
            JSONArray array = this.versionMeta.getJSONArray("libraries");
            for (int i = 0; i < array.length(); i++)
            {
                OS currentOS = Utils.getPlatform();
                JSONObject lib = array.getJSONObject(i);
                String name = lib.getString("name");
                URL url = null;
                String sha1 = null;
                long size = -1;
                JSONArray rules = null;
                Map<OS, LibraryRule> ruls = new HashMap();
                boolean skip = false;
                String native_class = null;
                List<String> exclude = new ArrayList();
                if (lib.has("rules"))
                {
                    rules = lib.getJSONArray("rules");
                    List<LibraryRule> used = new ArrayList();
                    OS[] oses = OS.values();
                    for (int j = 0; j < rules.length(); j++)
                    {
                        JSONObject rule = rules.getJSONObject(j);
                        LibraryRule lr = LibraryRule.valueOf(rule.getString("action").toUpperCase());
                        if (rule.has("os"))
                        {
                            JSONObject os = rule.getJSONObject("os");
                            OS o = OS.valueOf(os.getString("name").toUpperCase());
                            console.printInfo((ruls.containsKey(o) ? "Updated" : "Added") + " " + o.name() + " rule (" + lr.name() + ") in library " + name);
                            ruls.put(o, lr);
                        }
                        else
                        {
                            for (OS o : oses)
                            {
                                if (!ruls.containsKey(o))
                                {
                                    console.printInfo("Added " + o.name() + " rule (" + lr.name() + ") in library " + name);
                                    ruls.put(o, lr);
                                }
                            }
                        }
                        if (!used.contains(lr))
                        {
                            used.add(lr);
                        }
                    }
                    if (used.contains(LibraryRule.ALLOW) && !used.contains(LibraryRule.DISALLOW))
                    {
                        for (OS o : oses)
                        {
                            if (!ruls.containsKey(o))
                            {
                                ruls.put(o, LibraryRule.DISALLOW);
                                console.printInfo("Added " + o.name() + " rule (" + LibraryRule.DISALLOW.name() + ") in library " + name);
                            }
                        }
                    }
                    else if (!used.contains(LibraryRule.ALLOW) && used.contains(LibraryRule.DISALLOW))
                    {
                        for (OS o : oses)
                        {
                            if (!ruls.containsKey(o))
                            {
                                ruls.put(o, LibraryRule.ALLOW);
                                console.printInfo("Added " + o.name() + " rule (" + LibraryRule.ALLOW.name() + ") in library " + name);
                            }
                        }
                    }
                    if (currentOS != OS.UNKNOWN)
                    {
                        if (ruls.get(currentOS) != LibraryRule.ALLOW)
                        {
                            skip = true;
                        }
                    }
                }
                if (!skip)
                {
                    if (lib.has("natives"))
                    {
                        JSONObject natives = lib.getJSONObject("natives");
                        String osArch = (Utils.getOSArch().equals(OSArch.NEW) ? "64" : "32");
                        native_class = (natives.has(currentOS.name().toLowerCase()) ? natives.getString(currentOS.name().toLowerCase()).replace("${arch}", osArch) : null);
                    }
                    if (lib.has("extract"))
                    {
                        JSONObject extract = lib.getJSONObject("extract");
                        if (extract.has("exclude"))
                        {
                            JSONArray exc = extract.getJSONArray("exclude");
                            for (int k = 0; k < exc.length(); k++)
                            {
                                String value = exc.getString(k);
                                if (!exclude.contains(value))
                                {
                                    exclude.add(value);
                                    console.printInfo("Exclusion added for path " + value + " in " + name);
                                }
                            }
                        }
                    }
                    boolean legacy = false;
                    if (lib.has("downloads"))
                    {
                        JSONObject downloads = lib.getJSONObject("downloads");
                        if (downloads.has("artifact"))
                        {
                            JSONObject artifact = downloads.getJSONObject("artifact");
                            if (artifact.has("url"))
                            {
                                url = Utils.stringToURL(artifact.getString("url"));
                            }
                            if (artifact.has("sha1"))
                            {
                                sha1 = artifact.getString("sha1");
                            }
                            if (artifact.has("size"))
                            {
                                size = artifact.getLong("size");
                            }
                        }
                        if (native_class != null)
                        {
                            if (downloads.has("classifiers"))
                            {
                                JSONObject classifiers = downloads.getJSONObject("classifiers");
                                if (classifiers.has(native_class))
                                {
                                    JSONObject clas = classifiers.getJSONObject(native_class);
                                    long nSize = (clas.has("size") ? clas.getLong("size") : -1);
                                    String nSha = (clas.has("sha1") ? clas.getString("sha1") : null);
                                    URL nURL = (clas.has("url") ? Utils.stringToURL(clas.getString("url")) : null);
                                    Native n = new Native(name, nURL, nSha, nSize, exclude, native_class);
                                    console.printInfo("Native " + name + (this.natives.containsKey(name) ? " updated" : " added"));
                                    this.natives.put(name, n);
                                }
                            }
                        }
                    }
                    else
                    {
                        if (lib.has("url"))
                        {
                            url = Utils.stringToURL(lib.getString("url") + Utils.getArtifactPath(name, "jar"));
                        }
                        else
                        {
                            url = Utils.stringToURL("https://libraries.minecraft.net/" + Utils.getArtifactPath(name, "jar"));
                        }
                        legacy = true;
                    }
                    Library l;
                    if (!legacy)
                    {
                        l = new Library(name, url, sha1, size, ruls);
                    }
                    else
                    {
                        l = new Library(name, url, ruls);
                    }
                    if (!libraries.containsKey(name))
                    {
                        libraries.put(name, l);
                        console.printInfo("Library " + name + " loaded.");
                    }
                    else
                    {
                        libraries.put(name, l);
                        console.printInfo("Library " + name + " updated!.");
                    }
                }
                else
                {
                    console.printInfo("Library " + name + " skipped because does not support the current OS (" + Utils.getPlatform().name() + ").");
                }
            }
            if (this.versionMeta.has("assets"))
            {
                this.assets = this.versionMeta.getString("assets");
                console.printInfo("Found assets dependency from version " + this.assets);
            }
            if (this.versionMeta.has("assetIndex"))
            {
                JSONObject index = this.versionMeta.getJSONObject("assetIndex");
                AssetIndex aInd = new AssetIndex(index.getString("id"), index.getLong("size"), index.getLong("totalSize"), Utils.stringToURL(index.getString("url")), index.getString("sha1"));
                this.assetIndex = aInd;
                console.printInfo("Found assets download for version " + this.assetIndex);
            }
            else
            {
                this.assetIndex = null;
            }
            Version tmp = this;
            while (tmp != null)
            {
                if (tmp.hasInheritedVersion())
                {
                    tmp = tmp.getInheritedVersion();
                }
                else
                {
                    if (tmp != this)
                    {
                        this.rootVer = tmp;
                        console.printInfo("Found root version " + tmp.getID() + " in version " + this.getID());
                    }
                    tmp = null;
                }
            }
            JSONObject meta = this.getMeta();
            if (meta.has("minecraftArguments"))
            {
                this.minecraftArguments = meta.getString("minecraftArguments");
            }
            if (meta.has("mainClass"))
            {
                this.mainClass = meta.getString("mainClass");
            }
            this.prepared = true;
            return true;
        }
        catch (Exception ex)
        {
            console.printError("Failed to prepare version " + this.getID());
            this.prepared = false;
            return false;
        }
    }
    public boolean isPrepared()
    {
        return this.prepared;
    }
    public File getPath()
    {
        return this.path;
    }
    public Version getRoot()
    {
        return this.rootVer;
    }
    public void putURL(URL u, VersionOrigin o)
    {
        this.url.put(o, u);
    }
    public String getID()
    {
        return this.id;
    }
    public VersionType getType()
    {
        return this.type;
    }
    public VersionOrigin getOrigin()
    {
        return this.origin;
    }
    public URL getURL(VersionOrigin o)
    {
        if (this.url.containsKey(o))
        {
            return this.url.get(o);
        }
        else
        {
            return null;
        }
    }
    public JSONObject getMeta()
    {
        return this.versionMeta;
    }
    public boolean hasMinecraftArguments()
    {
        return (this.minecraftArguments != null);
    }
    public boolean hasMainClass()
    {
        return (this.mainClass != null);
    }
    public String getMinecraftArguments()
    {
        return this.minecraftArguments;
    }
    public String getMainClass()
    {
        return this.mainClass;
    }
    public Version getInheritedVersion()
    {
        return this.inheritedVersion;
    }
    public boolean hasInheritedVersion()
    {
        return (this.inheritedVersion != null);
    }
    public boolean hasLibraries()
    {
        return (this.libraries.size() > 0);
    }
    public Map<String, Library> getLibraries()
    {
        return this.libraries;
    }
    public boolean hasNatives()
    {
        return (this.natives.size() > 0);
    }
    public Map<String, Native> getNatives()
    {
        return this.natives;
    }
    public boolean hasAssetIndex()
    {
        return (this.assetIndex != null);
    }
    public boolean hasAssets()
    {
        return (this.assets != null);
    }
    public String getAssets()
    {
        return this.assets;
    }
    public AssetIndex getAssetIndex()
    {
        return this.assetIndex;
    }
}