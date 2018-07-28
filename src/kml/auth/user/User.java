package kml.auth.user;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author DarkLBP
 * website https://krothium.com
 */

public class User {
    @Expose
    private String accessToken;
    @Expose
    private String username;
    @Expose
    private HashMap<String, UserProfile> profiles;

    public String getAccessToken() {
        return accessToken;
    }

    public String getUsername() {
        return username;
    }

    public HashMap<String, UserProfile> getProfiles() {
        return profiles;
    }
}
