package kmlk.objects;

import java.util.UUID;
import java.util.Map;
/**
 * @website https://krothium.com
 * @author DarkLBP
 */

public class User {
    private final String displayName;
    private String accessToken;
    private final String userID;
    private final String userName;
    private final UUID profileUUID;
    private final Map<String, String> properties;
    public User(String name, String at, String ui, String un, UUID pi, Map<String, String> prop){
        this.displayName = name;
        this.accessToken = at;
        this.userID = ui;
        this.userName = un;
        this.profileUUID = pi;
        this.properties = prop;
    }
    public String getDisplayName(){return this.displayName;}
    public String getAccessToken(){return this.accessToken;}
    public String getUserID() {return this.userID;}
    public UUID getProfileID(){return this.profileUUID;}
    public void updateAccessToken(String accessToken){this.accessToken = accessToken;}
    public boolean hasProperties(){return (this.properties.size() > 0);}
    public Map<String, String> getProperties(){return this.properties;}
    public String getUsername(){return this.userName;}
}
