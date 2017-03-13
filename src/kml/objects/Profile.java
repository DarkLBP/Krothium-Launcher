package kml.objects;

import kml.Language;
import kml.Utils;
import kml.enums.ProfileIcon;
import kml.enums.ProfileType;

import javax.swing.*;
import java.io.File;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author DarkLBP
 * website https://krothium.com
 */

public class Profile {
    private final String id;
    private String name, javaArgs, lastVersionId;
    private ProfileType type;
    private File gameDir, javaDir;
    private Timestamp created, lastUsed;
    private Map<String, Integer> resolution = new HashMap<>();
    private JLabel listItem;
    private JMenuItem menuItem;

    public Profile(ProfileType type){
        this.id = UUID.randomUUID().toString().replaceAll("-", "");
        this.type = type;
        this.lastUsed = new Timestamp(0);
    }
    public Profile(String id, String name, String type, String created, String lastUsed, String lastVersionId, String gameDir, String javaDir, String javaArgs, Map<String, Integer> resolution){
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
            this.lastUsed = new Timestamp(0);
        } else {
            try {
                this.lastUsed = Timestamp.valueOf(lastUsed.replace("T", " ").replace("Z", ""));
            } catch (Exception ex){
                this.lastUsed = new Timestamp(0);
            }
        }
        if (type == null) {
            this.type = ProfileType.CUSTOM;
        } else {
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
        }

        if (this.type == ProfileType.CUSTOM) {
            if (created == null) {
                this.created = new Timestamp(0);
            } else {
                try {
                    this.created = Timestamp.valueOf(created.replace("T", " ").replace("Z", ""));
                } catch (Exception ex){
                    this.created = new Timestamp(0);
                }
            }
        }
    }
    public String getID(){return this.id;}
    public void setName(String newName){this.name = newName;}
    public void setVersionID(String ver){this.lastVersionId = ver;}
    public String getName(){return this.name;}
    public boolean hasName(){return this.name != null;}
    public void setType(ProfileType type){this.type = type;}
    public ProfileType getType(){return this.type;}
    public String getVersionID(){return this.lastVersionId;}
    public boolean hasVersion(){return this.lastVersionId != null;}
    public File getGameDir(){return this.gameDir;}
    public boolean hasGameDir(){return (this.gameDir != null);}
    public void setGameDir(File dir){this.gameDir = dir;}
    public void setJavaDir(File dir){this.javaDir = dir;}
    public void setJavaArgs(String args){this.javaArgs = args;}
    public File getJavaDir(){return this.javaDir;}
    public boolean hasJavaDir(){return this.javaDir != null;}
    public String getJavaArgs(){return this.javaArgs;}
    public boolean hasJavaArgs(){return this.javaArgs != null;}
    public Timestamp getLastUsed(){return lastUsed;}
    public void setLastUsed(Timestamp used){this.lastUsed = used;}
    public boolean hasCreated(){return this.created != null;}
    public Timestamp getCreated(){return this.created;}
    public void setCreated(Timestamp created){this.created = created;}
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
    public JMenuItem getMenuItem()
    {
        if (this.menuItem == null){
            if (this.hasName()){
                this.menuItem = new JMenuItem(this.getName());
            } else if (this.getType() == ProfileType.RELEASE) {
                this.menuItem = new JMenuItem(Language.get(59));
            } else if (this.getType() == ProfileType.SNAPSHOT){
                this.menuItem = new JMenuItem(Language.get(60));
            } else {
                this.menuItem = new JMenuItem(Language.get(70));
            }
            this.menuItem.setIcon(Utils.getProfileIcon(ProfileIcon.GRASS));
        } else {
            if (this.hasName() && !this.getName().equals(this.listItem.getText())){
                this.menuItem.setText(this.getName());
            } else if (this.getType() == ProfileType.RELEASE && !listItem.getText().equals(Language.get(59))){
                this.menuItem.setText(Language.get(59));
            } else if (this.getType() == ProfileType.SNAPSHOT && !listItem.getText().equals(Language.get(60))){
                this.menuItem.setText(Language.get(60));
            } else if (!this.hasName() && this.getType() == ProfileType.CUSTOM) {
                this.menuItem.setText(Language.get(70));
            }
        }
        this.menuItem.setIconTextGap(25);
        return this.menuItem;
    }
    public JLabel getListItem(){
        if (this.listItem == null){
            if (this.hasName()){
                this.listItem = new JLabel(this.getName());
            } else if (this.getType() == ProfileType.RELEASE) {
                this.listItem = new JLabel(Language.get(59));
            } else if (this.getType() == ProfileType.SNAPSHOT){
                this.listItem = new JLabel(Language.get(60));
            } else {
                this.listItem = new JLabel(Language.get(70));
            }
            this.listItem.setIcon(Utils.getProfileIcon(ProfileIcon.GRASS));
        } else {
            if (this.hasName() && !this.getName().equals(this.listItem.getText())){
                this.listItem.setText(this.getName());
            } else if (this.getType() == ProfileType.RELEASE && !listItem.getText().equals(Language.get(59))){
                this.listItem.setText(Language.get(59));
            } else if (this.getType() == ProfileType.SNAPSHOT && !listItem.getText().equals(Language.get(60))){
                this.listItem.setText(Language.get(60));
            } else if (!this.hasName() && this.getType() == ProfileType.CUSTOM) {
                this.listItem.setText(Language.get(70));
            }
        }
        return this.listItem;
    }

    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("[Name: ");
        b.append(getName());
        b.append(" | UUID: ");
        b.append(getID());
        b.append("]");
        return b.toString();
    }
}
