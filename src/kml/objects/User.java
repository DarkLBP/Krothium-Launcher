package kml.objects;

import java.util.Map;
import java.util.UUID;
/**
 * @website https://krothium.com
 * @author DarkLBP
 */

public class User {
    private final String displayName;
    private String accessToken;
    private final String userID;
    private final String userName;
    private final String profileUUID;
    public User(String name, String at, String ui, String un, String pi){
        this.displayName = name;
        this.accessToken = at;
        this.userID = ui;
        this.userName = un;
        this.profileUUID = pi;
    }
    public String getDisplayName(){return this.displayName;}
    public String getAccessToken(){return this.accessToken;}
    public String getUserID() {return this.userID;}
    public String getProfileID(){return this.profileUUID;}
    public void updateAccessToken(String accessToken){this.accessToken = accessToken;}
    public String getUsername(){return this.userName;}
}
