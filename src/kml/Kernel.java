package kml;

import kml.gui.Main;
import org.json.JSONObject;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Set;


/**
 * @author DarkLBP
 * website https://krothium.com
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
        this.console.printInfo("KML v" + Constants.KERNEL_BUILD_NAME + " by DarkLBP (https://krothium.com)");
        this.console.printInfo("Kernel build: " + Constants.KERNEL_BUILD);
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
        this.loadSettings();
        this.loadVersions();
        this.loadProfiles();
        this.loadUsers();
        this.mainForm = new Main(this);
    }
    public Console getConsole(){return this.console;}
    public File getWorkingDir(){return this.workingDir;}
    public void saveProfiles(){
        JSONObject output = new JSONObject();
        JSONObject profiles = this.profiles.toJSON();
        JSONObject authdata = this.authentication.toJSON();
        Set pset = profiles.keySet();
        for (Object aPset : pset) {
            String name = aPset.toString();
            output.put(name, profiles.get(name));
        }
        Set aset = authdata.keySet();
        for (Object anAset : aset) {
            String name = anAset.toString();
            output.put(name, authdata.get(name));
        }
        output.put("settings", this.settings.toJSON());
        if (!Utils.writeToFile(output.toString(), this.getConfigFile())){
            console.printError("Failed to save the profiles file!");
        }
    }
    private void loadProfiles(){profiles.fetchProfiles();}
    private void loadVersions(){versions.fetchVersions();}
    private void loadUsers(){authentication.fetchUsers();}
    private void loadSettings(){settings.loadSettings();}
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
    public String checkForUpdates(){
        try{
            URL url = Constants.GETLATEST_URL;
            String r = Utils.sendPost(url, new byte[0], new HashMap<String, String>());
            String[] data = r.split(":");
            int version = Integer.parseInt(Utils.fromBase64(data[0]));
            if (version > Constants.KERNEL_BUILD){
                console.printInfo("New kernel build available: " + version);
                return data[1];
            }
        } catch (Exception ex){
            console.printError("Failed to check for updates.");
            return null;
        }
        return null;
    }
    public Main getGUI(){return this.mainForm;}
}
