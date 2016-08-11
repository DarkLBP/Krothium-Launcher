package kmlk;

import kmlk.objects.Profile;
import kmlk.objects.User;
import java.io.File;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import kmlk.exceptions.AuthenticationException;
import kmlk.exceptions.DownloaderException;
import kmlk.exceptions.GameLauncherException;
import kmlk.objects.Version;
import kmlk.objects.VersionMeta;
import org.json.JSONObject;


/**
 * @website https://krothium.com
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
    public Kernel(){
        this.workingDir = Utils.getWorkingDirectory();
        this.console = new Console();
        this.profiles = new Profiles(this);
        this.versions = new Versions(this);
        this.downloader = new Downloader(this);
        this.authentication = new Authentication(this);
        this.gameLauncher = new GameLauncher(this);
        System.out.println("KMLK v" + Constants.KERNEL_BUILD_NAME + " by DarkLBP (http://krotium.com)");
    }
    public Console getConsole(){return this.console;}
    public void setWorkingDir(File dir){
        if (!dir.exists() || !dir.isDirectory()){
            dir.mkdirs();
        }
        this.workingDir = dir;
    }
    public File getWorkingDir(){return this.workingDir;}
    public User getSelectedUser(){return this.authentication.getSelectedUser();}
    public String getSelectedProfile(){return this.profiles.getSelectedProfile();}
    public boolean setSelectedProfile(String p){return this.profiles.setSelectedProfile(p);}
    public boolean updateProfile(Profile p){return this.profiles.updateProfile(p);}
    public boolean addProfile(Profile p){return this.profiles.addProfile(p);}
    public boolean renameProfile(String oldName, String newName){return this.profiles.renameProfile(oldName, newName);}
    public void launchGame() throws GameLauncherException{this.gameLauncher.launch();}
    public void authenticate(String user, String pass) throws AuthenticationException {this.authentication.authenticate(user, pass);};
    public boolean logOut(){return this.authentication.logOut();}
    public int getDownloadProgress(){return this.downloader.getProgress();}
    public boolean isGameStarted(){return this.gameLauncher.isStarted();};
    public InputStream getGameInputStream(){return this.gameLauncher.getInputStream();};
    public void download() throws DownloaderException{this.downloader.download();}
    public boolean existsProfile(String p){return (this.profiles.getProfileByName(p) != null);}
    public boolean existsVersion(String v){return (this.versions.getVersion(v) != null);}
    public Profile getProfile(String p){return this.profiles.getProfileByName(p);}
    public Version getVersion(String v){return this.versions.getVersion(v);}
    public VersionMeta getVersionMeta(String v){return this.versions.getVersionMeta(v);}
    public Map<String, Profile> getProfileDB(){return this.profiles.getProfiles();}
    public LinkedHashSet<String> getVersionDB(){return this.versions.getVersions();}
    public boolean isDownloading(){return this.downloader.isDownloading();}
    public boolean deleteProfile(String name){return this.profiles.deleteProfile(name);}
    public String getLatestVersion(){return this.versions.getLatestRelease();}
    public boolean isAuthenticated(){return this.authentication.isAuthenticated();}
    public String getClientToken(){return this.authentication.getClientToken();}
    public boolean saveProfiles(){
        JSONObject output = new JSONObject();
        JSONObject profiles = this.profiles.toJSON();
        JSONObject authdata = this.authentication.toJSON();
        Set pset = profiles.keySet();
        Iterator pit = pset.iterator();
        while (pit.hasNext()){
            String name = pit.next().toString();
            output.put(name, profiles.get(name));
        }
        Set aset = authdata.keySet();
        Iterator ait = aset.iterator();
        while (ait.hasNext()){
            String name = ait.next().toString();
            output.put(name, authdata.get(name));
        }
        return Utils.writeToFile(output.toString(), this.getConfigFile());
    }
    public void loadProfiles(){profiles.fetchProfiles();}
    public void loadVersions(){versions.fetchVersions();}
    public void loadUsers(){authentication.fetchUsers();}
    public Versions getVersions(){return this.versions;}
    public Profiles getProfiles(){return this.profiles;}
    public Downloader getDownloader(){return this.downloader;}
    public Authentication getAuthentication(){return this.authentication;}
    public File getConfigFile(){return new File(this.getWorkingDir() + File.separator + "launcher_profiles.json");}
    public GameLauncher getGameLauncher(){return this.gameLauncher;}
}
