package kml;

import kml.exceptions.AuthenticationException;
import kml.exceptions.DownloaderException;
import kml.exceptions.GameLauncherException;
import kml.gui.Main;
import kml.objects.Profile;
import kml.objects.User;
import kml.objects.Version;
import kml.objects.VersionMeta;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.util.*;


/**
 * @website https://krothium.com
 * @author DarkLBP
 */

public final class Kernel {
    private final File workingDir;
    private final Console console;
    private final Profiles profiles;
    private final Versions versions;
    private final Settings settings;
    private final Downloader downloader;
    private final Authentication authentication;
    private final GameLauncher gameLauncher;
    private final Main mainForm;
    public Kernel(){
        this(Utils.getWorkingDirectory());
    }
    private Kernel(File workDir){
        this.workingDir = workDir;
        if (!this.workingDir.exists()){
            this.workingDir.mkdirs();
        }
        this.console = new Console(this);
        this.console.printInfo("KMLK v" + Constants.KERNEL_BUILD_NAME + " by DarkLBP (https://krothium.com)");
        this.console.printInfo("OS: " + System.getProperty("os.name"));
        this.console.printInfo("OS Version: " + System.getProperty("os.version"));
        this.console.printInfo("OS Architecture: " + System.getProperty("os.arch"));
        this.console.printInfo("Java Version: " + System.getProperty("java.version"));
        this.console.printInfo("Java Vendor: " + System.getProperty("java.vendor"));
        this.console.printInfo("Java Architecture: " + System.getProperty("sun.arch.data.model"));
        this.profiles = new Profiles(this);
        this.versions = new Versions(this);
        this.settings = new Settings(this);
        this.downloader = new Downloader(this);
        this.authentication = new Authentication(this);
        this.gameLauncher = new GameLauncher(this);
        this.loadVersions();
        this.loadProfiles();
        this.loadUsers();
        this.loadSettings();
        this.mainForm = new Main(this);
    }
    public Console getConsole(){return this.console;}
    public File getWorkingDir(){return this.workingDir;}
    public User getSelectedUser(){return this.authentication.getSelectedUser();}
    public String getSelectedProfile(){return this.profiles.getSelectedProfile();}
    public boolean setSelectedProfile(String p){return this.profiles.setSelectedProfile(p);}
    public boolean addProfile(Profile p){return this.profiles.addProfile(p);}
    public boolean renameProfile(String oldName, String newName){return this.profiles.renameProfile(oldName, newName);}
    public void launchGame() throws GameLauncherException{this.gameLauncher.launch();}
    public void authenticate(String user, String pass) throws AuthenticationException {this.authentication.authenticate(user, pass);}
    public boolean logOut(){return this.authentication.logOut();}
    public int getDownloadProgress(){return this.downloader.getProgress();}
    public boolean isGameStarted(){return this.gameLauncher.isStarted();}
    public boolean hasGameCrashed(){return this.gameLauncher.hasError();}
    public InputStream getGameInputStream(){return this.gameLauncher.getInputStream();}
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
        output.put("settings", this.settings.toJSON());
        return Utils.writeToFile(output.toString(), this.getConfigFile());
    }
    public void loadProfiles(){profiles.fetchProfiles();}
    public void loadVersions(){versions.fetchVersions();}
    public void loadUsers(){authentication.fetchUsers();}
    public void loadSettings(){settings.loadSettings();}
    public Versions getVersions(){return this.versions;}
    public Profiles getProfiles(){return this.profiles;}
    public Settings getSettings(){return this.settings;}
    public Downloader getDownloader(){return this.downloader;}
    public Authentication getAuthentication(){return this.authentication;}
    public File getConfigFile(){
        return new File(this.workingDir + File.separator + "launcher_profiles.json");}
    public GameLauncher getGameLauncher(){return this.gameLauncher;}
    public void exitSafely(){
        this.saveProfiles();
        this.console.printInfo("Shutting down launcher...");
        this.console.close();
        System.exit(0);
    }
    public boolean checkForUpdates(){
        if (!Constants.UPDATE_CHECKED){
            Constants.UPDATE_CHECKED = true;
            try{
                URL url = Constants.GETLATEST_URL;
                if (!Constants.USE_HTTPS){
                    url = Utils.stringToURL(url.toString().replace("https", "http"));
                }
                String r = Utils.sendPost(url, new byte[0], new HashMap<>());
                String[] data = r.split(":");
                int version = Integer.parseInt(Utils.fromBase64(data[0]));
                if (version > Constants.KERNEL_BUILD){
                    return true;
                } else {
                    return false;
                }
            } catch (Exception ex){
                return false;
            }
        }
        return false;
    }
    public Main getGUI(){return this.mainForm;}
}
