package kml.objects;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kml.enums.LauncherVisibility;
import kml.enums.VersionType;

/**
 * @website https://krothium.com
 *  @author DarkLBP
 */

public class Profile {
    private String name;
    private String versionID = null;
    private File gameDir = null;
    private File javaDir = null;
    private String javaArgs = null;
    private Map<String, Integer> resolution = new HashMap();
    private LauncherVisibility visibility = null;
    private final List<VersionType> allowedVersionTypes;
    
    public Profile(String name){
        this.name = name;
        this.allowedVersionTypes = new ArrayList();
        this.allowedVersionTypes.add(VersionType.RELEASE);
    }
    public Profile(String name, String lastVersionId, File gameDir, File javaDir, String javaArgs, Map<String, Integer> resolution, LauncherVisibility v, List<VersionType> types){
        this.name = name;
        this.versionID = lastVersionId;
        this.gameDir = gameDir;
        this.javaDir = javaDir;
        this.javaArgs = javaArgs;
        this.resolution = resolution;
        this.visibility = v;
        this.allowedVersionTypes = types;
    }
    public void setName(String newName){this.name = newName;}
    public void setVersionID(String ver){this.versionID = ver;}
    public String getName(){
        return this.name;
    }
    public String getVersionID(){return this.versionID;}
    public boolean hasVersion(){return (this.versionID != null);}
    public File getGameDir(){return this.gameDir;}
    public boolean hasGameDir(){return (this.gameDir != null);}
    public void setGameDir(File dir){this.gameDir = dir;}
    public void setJavaDir(File dir){this.javaDir = dir;}
    public void setJavaArgs(String args){this.javaArgs = args;}
    public File getJavaDir(){return this.javaDir;}
    public boolean hasJavaDir(){return (this.javaDir != null);}
    public String getJavaArgs(){return this.javaArgs;}
    public boolean hasJavaArgs(){return (this.javaArgs != null);}
    public boolean hasResolution(){
        if (this.resolution != null){
            return (this.resolution.size() == 2);
        }
        return false;
    }
    public int getResolutionHeight(){
        if (resolution.containsKey("height")){
            return resolution.get("height");
        }
        return 0;
    }
    public int getResolutionWidth(){
        if (resolution.containsKey("width")){
            return resolution.get("width");
        }
        return 0;
    }
    public void setResolution(int w, int h){
        if (w < 0 || h < 0){
            resolution = null;
        } else {
            if (resolution == null){
                resolution = new HashMap();
            }
            resolution.put("width", w);
            resolution.put("height", h);
        }
    }
    public void allowVersionType(VersionType t){
        if (!this.allowedVersionTypes.contains(t)){
            this.allowedVersionTypes.add(t);
        }
    }
    public void removeVersionType(VersionType t){
        if (this.allowedVersionTypes.contains(t)){
            this.allowedVersionTypes.remove(t);
        }
    }
    public boolean hasVisibility(){return (this.visibility != null);}
    public LauncherVisibility getVisibility(){return this.visibility;}
    public List<VersionType> getAllowedVersionTypes(){return this.allowedVersionTypes;}
    public boolean isAllowedVersionType(VersionType t){return (this.allowedVersionTypes.contains(t));}
}
