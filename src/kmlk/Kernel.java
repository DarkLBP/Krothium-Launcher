package kmlk;

import java.io.File;
import java.util.Iterator;
import java.util.Set;
import org.json.JSONObject;


/**
 * @website http://krotium.com
 * @author DarkLBP
 */

public final class Kernel {
    public File workingDir;
    public final Console console;
    public final Profiles profiles;
    public final Versions versions;
    public final Downloader downloader;
    public final Authentication authentication;
    public final GameLauncher gameLauncher;
    public static Kernel kernel;
    public Kernel()
    {
        this.kernel = this;
        this.workingDir = Utils.getWorkingDirectory();
        this.console = new Console();
        this.profiles = new Profiles();
        this.versions = new Versions();
        this.downloader = new Downloader();
        this.authentication = new Authentication();
        this.gameLauncher = new GameLauncher();
        System.out.println("KMLK r" + String.valueOf(Constants.kernelRevision) + " by DarkLBP (http://krotium.com)");
    }
    public Console getConsole()
    {
        return this.console;
    }
    public void setWorkingDir(File dir)
    {
        if (!dir.exists() || !dir.isDirectory())
        {
            dir.mkdirs();
        }
        this.workingDir = dir;
    }
    public File getWorkingDir()
    {
        return this.workingDir;
    }
    public User getSelectedUser()
    {
        return this.authentication.getSelectedUser();
    }
    public Profile getSelectedProfile()
    {
        return this.profiles.getSelectedProfile();
    }
    public void setSelectedProfile(Profile p)
    {
        this.profiles.setSelectedProfile(p);
    }
    public boolean updateProfile(Profile p)
    {
        return this.profiles.updateProfile(p);
    }
    public boolean addProfile(Profile p)
    {
        return this.profiles.addProfile(p);
    }
    public boolean saveProfiles()
    {
        JSONObject output = new JSONObject();
        JSONObject profiles = this.profiles.toJSON();
        JSONObject authdata = this.authentication.toJSON();
        Set pset = profiles.keySet();
        Iterator pit = pset.iterator();
        while (pit.hasNext())
        {
            String name = pit.next().toString();
            output.put(name, profiles.get(name));
        }
        Set aset = authdata.keySet();
        Iterator ait = aset.iterator();
        while (ait.hasNext())
        {
            String name = ait.next().toString();
            output.put(name, authdata.get(name));
        }
        return Utils.writeToFile(output.toString(), this.getConfigFile());
    }
    public void loadProfiles()
    {
        profiles.fetchProfiles();
    }
    public void loadVersions()
    {
        versions.fetchVersions();
    }
    public void loadUsers()
    {
        authentication.fetchUsers();
    }
    public Versions getVersions()
    {
        return this.versions;
    }
    public Profiles getProfiles()
    {
        return this.profiles;
    }
    public Downloader getDownloader()
    {
        return this.downloader;
    }
    public Authentication getAuthentication()
    {
        return this.authentication;
    }
    public File getConfigFile()
    {
        return new File(this.getWorkingDir() + File.separator + "launcher_profiles.json");
    }
    public GameLauncher getGameLauncher()
    {
        return this.gameLauncher;
    }
    public static Kernel getKernel()
    {
        return kernel;
    }
}
