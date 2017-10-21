package kml.auth.user;

import java.util.ArrayList;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */

public class User {
    private final String id, username;
    private String accessToken;
    private final UserType type;
    private final ArrayList<UserProfile> profiles;
    private String selectedProfile;

    public User(String ui, String at, String un, UserType type, ArrayList<UserProfile> userProfiles, String selectedProfile) {
        this.accessToken = at;
        this.id = ui;
        this.username = un;
        this.type = type;
        this.profiles = userProfiles;
        this.selectedProfile = selectedProfile;
    }

    public final String getDisplayName() {
        for (UserProfile up : profiles) {
            if (up.getId().equalsIgnoreCase(selectedProfile)) {
                return up.getDisplayName();
            }
        }
        if (!profiles.isEmpty()) {
            return profiles.get(0).getDisplayName();
        }
        return null;
    }

    public final String getAccessToken() {
        return this.accessToken;
    }

    public final String getUserID() {
        return this.id;
    }

    public ArrayList<UserProfile> getProfiles() {
        return this.profiles;
    }

    public String getSelectedProfile() {
        return this.selectedProfile;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public final String getUsername() {
        return this.username;
    }

    public final UserType getType() {
        return this.type;
    }

    public void setSelectedProfile(String selectedProfile) {
        this.selectedProfile = selectedProfile;
    }

    @Override
    public final String toString() {
        return this.getDisplayName();
    }

    @Override
    public final boolean equals(Object o) {
        return o instanceof User && this.id.equalsIgnoreCase(((User) o).id);
    }

    @Override
    public final int hashCode() {
        return this.id.hashCode();
    }
}
