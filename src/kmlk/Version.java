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
    private final Kernel kernel;
    private final Map<String, Library> libraries = new HashMap();
    private final Map<String, Native> natives = new HashMap();
    private final Console console;
    private boolean prepared = false;
    private String assetID;
    private Version rootVer;
    public String minecraftArguments = null;
    public String mainClass = null;
    
    public Version(Kernel k, String id, VersionType type, VersionOrigin or, URL url)
    {
        this.id = id;
        this.type = type;
        this.url.put(or, url);
        this.origin = or;
        this.kernel = k;
        this.console = k.getConsole();
        if (or != VersionOrigin.REMOTE)
        {
            this.prepare();
        }
        this.path = new File("versions" + File.separator + id + File.separator + id + ".jar");
    }
    public void prepare()
    {
        this.fetchVersionMeta();
        this.fetchInheritedVersion();
        this.fetchLibraries();
        this.fetchAssetID();
        this.fetchRoot();
        this.fetchArguments();
        this.prepared = true;
    }
    public boolean isPrepared()
    {
        return this.prepared;
    }
    public File getPath()
    {
        return this.path;
    }
    public void fetchRoot()
    {
        Version tmp = this;
        while (tmp != null)
        {
            if (tmp.hasInheritedVersion())
            {
                tmp = tmp.getInheritedVersion();
            }
            else
            {
                this.rootVer = tmp;
                console.printInfo("Found root version " + tmp.getID() + " in version " + this.getID());
                tmp = null;
            }
        }
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
    public boolean fetchVersionMeta()
    {
        try
        {
            this.versionMeta = new JSONObject(Utils.readURL(this.getURL((this.getOrigin() != VersionOrigin.REMOTE) ? VersionOrigin.LOCAL : VersionOrigin.REMOTE)));
            return true;
        }
        catch (Exception ex)
        {
            return false;
        }
    }
    public JSONObject getMeta()
    {
        return this.versionMeta;
    }
    public void fetchArguments()
    {
        JSONObject meta = this.getMeta();
        if (meta.has("minecraftArguments"))
        {
            this.minecraftArguments = meta.getString("minecraftArguments");
        }
        if (meta.has("mainClass"))
        {
            this.mainClass = meta.getString("mainClass");
        }
    }
    public boolean hasArguments()
    {
        return (this.minecraftArguments != null);
    }
    public boolean hasMainClass()
    {
        return (this.mainClass != null);
    }
    public String getArguments()
    {
        return this.minecraftArguments;
    }
    public String getMainClass()
    {
        return this.mainClass;
    }
    public void fetchInheritedVersion()
    {
        if (this.getOrigin() == VersionOrigin.LOCAL)
        {
            JSONObject meta = this.getMeta();
            if (meta.has("inheritsFrom"))
            {
                this.inheritedVersion = kernel.getVersions().getVersionByName(meta.getString("inheritsFrom"));
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
        if (this.inheritedVersion != null)
        {
            console.printInfo("Version " + this.getID() + " inherits from version " + this.inheritedVersion.getID());
        }
    }
    public Version getInheritedVersion()
    {
        return this.inheritedVersion;
    }
    public boolean hasInheritedVersion()
    {
        return (this.inheritedVersion != null);
    }
    public void fetchLibraries()
    {
        console.printInfo("Fetching required versions libraries.");
        JSONObject root = this.getMeta();
        JSONArray array = root.getJSONArray("libraries");
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
                Library l = new Library(this.kernel, name, url, sha1, size, ruls);
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
    public boolean hasAssets()
    {
        return (this.assetID != null);
    }
    public void fetchAssetID()
    {
        JSONObject verMeta = this.getMeta();
        if (verMeta.has("assetIndex"))
        {
            this.assetID = verMeta.getJSONObject("assetIndex").getString("id");
            console.printInfo("Found asset dependency from version " + this.assetID);
        }
        else
        {
            this.assetID = null;
        }
    }
    public String getAssetID()
    {
        return this.assetID;
    }
}
