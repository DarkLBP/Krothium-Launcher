package kml.objects;

import kml.Kernel;
import kml.Utils;
import kml.enums.ProfileIcon;
import kml.enums.ProfileType;

import javax.swing.*;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.io.File;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @website https://krothium.com
 *  @author DarkLBP
 */

public class Profile {
    private String id;
    private String name;
    private ProfileType type;
    private String lastVersionId = null;
    private File gameDir = null;
    private File javaDir = null;
    private String javaArgs = null;
    private Instant created = null;
    private Instant lastUsed = null;
    private Map<String, Integer> resolution = new HashMap<>();
    private JMenuItem menuItem;
    private final Kernel kernel;
    private final Font bold = new Font("Minecraftia", Font.BOLD,16);
    private final Font plain = new Font("Minecraftia", Font.PLAIN,16);
    
    public Profile(ProfileType type, Kernel k){
        this.id = UUID.randomUUID().toString().replaceAll("-", "");
        this.type = type;
        this.lastUsed = Instant.EPOCH;
        this.kernel = k;
    }
    public Profile(String id, String name, String type, String created, String lastUsed, String lastVersionId, String gameDir, String javaDir, String javaArgs, Map<String, Integer> resolution, Kernel k){
        if (id == null){
            this.id = UUID.randomUUID().toString().replaceAll("-", "");
        } else {
            this.id = id;
        }
        this.name = name;
        this.lastVersionId = lastVersionId;
        if (gameDir != null){
            this.gameDir = new File(gameDir);
            if (!this.gameDir.exists() || !this.gameDir.isDirectory()) {
                this.gameDir = null;
            }
        }
        if (javaDir != null){
            this.javaDir = new File(javaDir);
            if (!this.javaDir.exists() || !this.javaDir.isFile()){
                this.javaDir = null;
            }
        }
        this.javaArgs = javaArgs;
        this.resolution = resolution;
        if (lastUsed == null) {
            this.lastUsed = Instant.EPOCH;
        } else {
            try {
                this.lastUsed = Instant.parse(lastUsed);
            } catch (DateTimeParseException ex){
                this.lastUsed = Instant.EPOCH;
            }
        }
        type = type.toLowerCase();
        switch (type){
            case "latest-release":
                this.type = ProfileType.RELEASE;
                break;
            case "latest-snapshot":
                this.type = ProfileType.SNAPSHOT;
                break;
            default:
                this.type = ProfileType.CUSTOM;
        }
        if (this.type == ProfileType.CUSTOM) {
            if (created == null) {
                this.created = Instant.EPOCH;
            } else {
                try {
                    this.created = Instant.parse(created);
                } catch (DateTimeParseException ex){
                    this.created = Instant.EPOCH;
                }
            }
        }
        this.kernel = k;
    }
    public String getID(){return this.id;}
    public void setName(String newName){this.name = newName;}
    public void setVersionID(String ver){this.lastVersionId = ver;}
    public String getName(){return this.name;}
    public boolean hasName(){return this.name != null;}
    public void setType(ProfileType type){this.type = type;}
    public ProfileType getType(){return this.type;}
    public String getVersionID(){
        if (this.getType() == ProfileType.CUSTOM){
            return this.lastVersionId;
        } else if (this.getType() == ProfileType.SNAPSHOT){
            return kernel.getVersions().getLatestSnapshot();
        }
        return kernel.getVersions().getLatestRelease();
    }
    public boolean hasVersion(){
        if (this.getType() == ProfileType.CUSTOM){
            return this.lastVersionId != null;
        }
        return true;
    }
    public File getGameDir(){return this.gameDir;}
    public boolean hasGameDir(){return (this.gameDir != null);}
    public void setGameDir(File dir){this.gameDir = dir;}
    public void setJavaDir(File dir){this.javaDir = dir;}
    public void setJavaArgs(String args){this.javaArgs = args;}
    public File getJavaDir(){return this.javaDir;}
    public boolean hasJavaDir(){return this.javaDir != null;}
    public String getJavaArgs(){return this.javaArgs;}
    public boolean hasJavaArgs(){return this.javaArgs != null;}
    public Instant getLastUsed(){return lastUsed;}
    public void setLastUsed(Instant used){this.lastUsed = used;}
    public boolean hasCreated(){return this.created != null;}
    public Instant getCreated(){return this.created;}
    public void setCreated(Instant created){this.created = created;}
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
                resolution = new HashMap<>();
            }
            resolution.put("width", w);
            resolution.put("height", h);
        }
    }
    public JMenuItem getMenuItem(){
        if (this.menuItem == null){
            if (this.hasName()){
                this.menuItem = new JMenuItem(this.getName());
            } else {
                if (this.getType() == ProfileType.RELEASE){
                    this.menuItem = new JMenuItem("Latest Release");
                } else if (this.getType() == ProfileType.SNAPSHOT){
                    this.menuItem = new JMenuItem("Latest Snapshot");
                } else {
                    this.menuItem = new JMenuItem("Unnamed Profile");
                }
            }
            this.menuItem.addActionListener(e -> kernel.getProfiles().setSelectedProfile(getID()));
            this.menuItem.setIcon(Utils.getProfileIcon(ProfileIcon.GRASS));
        } else {
            if (this.hasName()){
                if (!this.menuItem.getText().equals(this.getName())){
                    this.menuItem.setText(this.getName());
                }
            } else {
                String fakeName;
                if (this.getType() == ProfileType.RELEASE){
                    fakeName = "Latest Release";
                } else if (this.getType() == ProfileType.SNAPSHOT){
                    fakeName = "Latest Snapshot";
                } else {
                    fakeName = "Unnamed Profile";
                }
                if (!this.menuItem.getText().equals(fakeName)){
                    this.menuItem.setText(fakeName);
                }
            }

        }
        if (kernel.getProfiles().getSelectedProfile().equals(this.getID())){
            this.menuItem.setFont(bold);
            this.menuItem.setText(this.menuItem.getText() + " (Selected)");
        } else {
            this.menuItem.setFont(plain);
        }
        return this.menuItem;
    }
}
