package kml;

import kml.enums.LauncherVisibility;
import kml.enums.VersionType;
import kml.objects.Profile;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.*;

/**
 * @website https://krothium.com
 * @author DarkLBP
 */

public class Profiles {
    private final Map<String, Profile> profiles = new HashMap<>();
    private final Console console;
    private String selected;
    private final Kernel kernel;
    
    public Profiles(Kernel k){
        this.kernel = k;
        this.console = k.getConsole();
    }
    public Map<String, Profile> getProfiles(){return this.profiles;}
    public boolean addProfile(Profile p){
        if (!this.existsProfile(p)){
            profiles.put(p.getName(), p);
            console.printInfo("Profile " + p.getName() + " added");
            return true;
        }
        console.printError("Profile " + p.getName() + " already exists!");
        return false;
    }
    public boolean deleteProfile(String p){
        if (this.existsProfile(p)){
            if (this.selected.equals(p)){
                console.printInfo("Profile " + p + " is selected and is going to be removed.");
                profiles.remove(p);
                console.printInfo("Profile " + p + " deleted.");
                if (this.profileCount() > 0){
                    Set keySet = this.profiles.keySet();
                    this.setSelectedProfile(keySet.toArray()[0].toString());
                } else {
                    this.createDefaultProfile();
                }
            } else {
                profiles.remove(p);
                console.printInfo("Profile " + p + " deleted.");
            }
            return true;
        }
        console.printError("Profile " + p + " doesn't exist.");
        return false;
    }
    private boolean duplicateProfile(Profile p) {
        if (this.existsProfile(p)){
            StringBuilder modifiedName = new StringBuilder();
            modifiedName.append(p.getName()).append("_");
            p.setName(modifiedName.toString());
            while (this.existsProfile(p)){
                modifiedName.append("_");
                p.setName(modifiedName.toString());
            }
            if (this.addProfile(p)){
                console.printError("Profile " + p.getName() + " duplicated with this name " + modifiedName);
                return true;
            }
        }
        console.printError("Profile " + p.getName() + " doesn't exist.");
        return false;
    }
    public boolean renameProfile(String oldName, String newName){
        if (this.existsProfile(oldName)){
            boolean sel = oldName.equals(this.selected);
            Profile tmp = this.getProfileByName(oldName);
            tmp.setName(newName);
            this.addProfile(tmp);
            if (sel){
                this.setSelectedProfile(newName);
            }
            this.deleteProfile(oldName);
            return true;
        }
        return false;
    }
    private boolean existsProfile(Profile p){return this.profiles.containsKey(p.getName());}
    private boolean existsProfile(String p){return this.profiles.containsKey(p);}
    public void fetchProfiles(){ 
        console.printInfo("Fetching profiles.");
        File launcherProfiles = kernel.getConfigFile();
        if (launcherProfiles.exists()){
            try{
                JSONObject root = new JSONObject(Utils.readURL(launcherProfiles.toURI().toURL()));
                JSONObject ples = root.getJSONObject("profiles");
                Set keys = ples.keySet();
                Iterator it = keys.iterator();
                String first = null;
                while (it.hasNext()){
                    String key = it.next().toString();
                    JSONObject o = ples.getJSONObject(key);
                    String name = null;
                    String ver = null;
                    File gameDir = null;
                    File javaDir = null;
                    String javaArgs = null;
                    Map<String, Integer> resolution = new HashMap<>();
                    LauncherVisibility visibility = null;
                    List<VersionType> types = new ArrayList<>();
                    if (o.has("name")){
                        name = o.getString("name");
                    }
                    if (o.has("allowedReleaseTypes")){
                        JSONArray a = o.getJSONArray("allowedReleaseTypes");
                        for (int i = 0; i < a.length(); i++){
                            try{
                                types.add(VersionType.valueOf(a.getString(i).toUpperCase()));
                                console.printInfo("Added version type " + VersionType.valueOf(a.getString(i).toUpperCase()));
                            }catch (Exception ex){
                                console.printError(a.get(i) + " version type does not exist.");
                            }
                        }
                    }
                    if (!types.contains(VersionType.RELEASE)){
                        types.add(VersionType.RELEASE);
                    }
                    if (o.has("lastVersionId")){
                        ver = o.getString("lastVersionId");
                    }
                    if (o.has("gameDir")){
                        gameDir = new File(o.getString("gameDir"));
                    }
                    if (o.has("javaDir")){
                        javaDir = new File(o.getString("javaDir"));
                    }
                    if (o.has("javaArgs")){
                        javaArgs = o.getString("javaArgs");
                    }
                    if (o.has("resolution")){
                        JSONObject res = o.getJSONObject("resolution");
                        if (res.has("width") && res.has("height")){
                            resolution.put("width", res.getInt("width"));
                            resolution.put("height", res.getInt("height"));
                        }else{
                            console.printError("Profile " + ((name != null) ? name : "UNKNOWN") + " has an invalid resolution.");
                        }
                    }
                    if (o.has("launcherVisibilityOnGameClose")){
                        String vis = o.getString("launcherVisibilityOnGameClose");
                        if (vis != null){
                            if (vis.length() >= 4){
                                if (vis.startsWith("close")){
                                    visibility = LauncherVisibility.CLOSE;
                                }else if (vis.startsWith("hide")){
                                    visibility = LauncherVisibility.HIDE;
                                }else if (vis.startsWith("keep")){
                                    visibility = LauncherVisibility.KEEP;
                                }
                            }
                        }
                    }
                    if (name != null){
                        Profile p = new Profile(name, ver, gameDir, javaDir, javaArgs, resolution, visibility, types);
                        if (first == null){
                            first = name;
                        }
                        if (!this.existsProfile(p)){
                            this.addProfile(p);
                        }else{
                            this.duplicateProfile(p);
                        }
                    }else{
                        console.printError("Invalid profile found: " + name);
                    }
                }
                if (this.profileCount() > 0){
                    if (root.has("selectedProfile")){
                        String selProfile = root.getString("selectedProfile");
                        if (this.profiles.containsKey(selProfile)){
                            console.printInfo("Profile " + selProfile + " marked as selected.");
                            if (!this.setSelectedProfile(selProfile)){
                                this.createDefaultProfile();
                            }
                        }else{
                            console.printError("Invalid profile selected! Using first loaded (" + first + ")");
                            if (this.setSelectedProfile(first)){
                                this.createDefaultProfile();
                            }
                        }
                    } else {
                        console.printInfo("No profile is selected! Using first loaded (" + first + ")");
                        if (this.setSelectedProfile(first)){
                            this.createDefaultProfile();
                        }
                    }
                }else{
                    this.createDefaultProfile();
                }
            }catch (Exception ex){
                ex.printStackTrace();
                console.printError("Failed to fetch profiles.");
            }
        }else{
            console.printError("Launcher profiles file not found. Using defaults.");
            this.createDefaultProfile();
        }
    }
    private boolean createDefaultProfile()
    {
        Profile p = new Profile("(Default)");
        if (this.existsProfile(p)){
            console.printError("Default profile already exists.");
            return false;
        }
        this.addProfile(p);
        this.setSelectedProfile("(Default)");
        return true;   
    }
    public Profile getProfileByName(String pName){
        if (profiles.containsKey(pName)){
            return profiles.get(pName);
        }
        return null;
    }
    private int profileCount(){return profiles.size();}
    public String getSelectedProfile(){return this.selected;}
    public boolean setSelectedProfile(String p){
        if (this.existsProfile(p)){
            console.printInfo("Profile " + p + " has been selected.");
            this.selected = p;
            return true;
        }
        return false;
    }
    public JSONObject toJSON(){
        JSONObject o = new JSONObject();
        JSONObject profiles = new JSONObject();
        Set s = this.profiles.keySet();
        Iterator it = s.iterator();
        while (it.hasNext()){
            String key = it.next().toString();
            Profile p = this.profiles.get(key);
            JSONObject prof = new JSONObject();
            prof.put("name", p.getName());
            if (p.hasGameDir()){
                prof.put("gameDir", p.getGameDir().toString());
            }
            if (p.hasVersion()){
                prof.put("lastVersionId", p.getVersionID());
            }
            if (p.hasJavaDir()){
                prof.put("javaDir", p.getJavaDir().toString());
            }
            if (p.hasJavaArgs()){
                prof.put("javaArgs", p.getJavaArgs());
            }
            if (p.hasResolution()){
                JSONObject res = new JSONObject();
                res.put("width", p.getResolutionWidth());
                res.put("height", p.getResolutionHeight());
                prof.put("resolution", res);
            }
            List<VersionType> allowed = p.getAllowedVersionTypes();
            if (allowed.size() > 0){
                JSONArray at = new JSONArray();
                for (VersionType t : allowed){
                    at.put(t.name().toLowerCase());
                }
                prof.put("allowedReleaseTypes", at);
            }
            if (p.hasVisibility()){
                String output = null;
                switch (p.getVisibility()){
                    case CLOSE:
                       output = "close launcher when game starts";
                       break;
                    case HIDE:
                        output = "hide launcher and re-open when game closes";
                        break;
                    case KEEP:
                        output = "keep the launcher open";
                        break;
                }
                prof.put("launcherVisibilityOnGameClose", output);
            }
            profiles.put(p.getName(), prof);
        }
        o.put("profiles", profiles);
        o.put("selectedProfile", this.selected);
        return o;
    }
}
