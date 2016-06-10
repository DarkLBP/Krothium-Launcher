package kmlk;

import java.io.File;


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
    public Kernel()
    {
        this.workingDir = Utils.getWorkingDirectory();
        this.console = new Console(this);
        this.profiles = new Profiles(this);
        this.versions = new Versions(this);
        this.downloader = new Downloader(this);
        this.authentication = new Authentication(this);
        this.gameLauncher = new GameLauncher(this);
        System.out.println("KMLK r" + String.valueOf(Constants.kernelRevision) + " by DarkLBP (http://krotium.com)");
    }
    public Console getConsole()
    {
        return this.console;
    }
    public void setWorkingDir(File dir, boolean autoCreate)
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
}