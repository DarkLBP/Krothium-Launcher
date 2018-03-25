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

    public User(String ui, String at, String un, UserType t, ArrayList<UserProfile> userProfiles, String selProf) {
        accessToken = at;
        id = ui;
        username = un;
        type = t;
        profiles = userProfiles;
        selectedProfile = selProf;
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
        return accessToken;
    }

    public final String getUserID() {
        return id;
    }

    public ArrayList<UserProfile> getProfiles() {
        return profiles;
    }

    public String getSelectedProfile() {
        return selectedProfile;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public final String getUsername() {
        return username;
    }

    public final UserType getType() {
        return type;
    }

    public void setSelectedProfile(String selectedProfile) {
        this.selectedProfile = selectedProfile;
    }

    @Override
    public final String toString() {
        return getDisplayName();
    }

    @Override
    public final boolean equals(Object o) {
        return o instanceof User && id.equalsIgnoreCase(((User) o).id);
    }

    @Override
    public final int hashCode() {
        return id.hashCode();
    }
}
