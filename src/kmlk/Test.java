package kmlk;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.json.JSONObject;

/**
 * @website http://krotium.com
 * @author DarkLBP
 */

public class Test {
    public static void main(String[] args)
    {
        Kernel k = new Kernel();
        k.getConsole().setEnabled(true);
        k.getConsole().includeTimestamps(true);
        k.setWorkingDir(new File("C:\\Minecraft"), true);
        k.loadVersions();
        k.loadProfiles();
        k.loadUsers();
        Profile p = k.getProfiles().getSelected();
        Downloader d = k.getDownloader();
        d.downloadAssets(p.getVersion());
        d.downloadVersion(p.getVersion());
        d.downloadLibraries(p.getVersion());
        d.downloadNatives(p.getVersion());
        GameLauncher l = k.getGameLauncher();
        Authentication a = k.getAuthentication();
        User u = a.getSelectedUser();
        l.launch(p);
        //a.validate();
        /*Version ver = k.getProfiles().getSelected().getVersion();
        Downloader d = k.getDownloader();
        d.downloadAssets(ver);
        d.downloadVersion(ver);
        d.downloadLibraries(ver);*/
        /*User u = a.getSelectedUser();*/
    }
}
