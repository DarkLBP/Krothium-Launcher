package kml.auth.user;

public class UserProfile {
    private final String id;
    private final String displayName;

    public UserProfile(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }
}
