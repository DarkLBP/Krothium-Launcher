package kml.auth.user;

import com.google.gson.annotations.Expose;

public class UserProfile {
    @Expose
    private String displayName;

    public String getDisplayName() {
        return displayName;
    }
}
