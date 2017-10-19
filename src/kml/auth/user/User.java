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

    public User(String ui, String at, String un, UserType type, ArrayList<UserProfile> userProfiles) {
        this.accessToken = at;
        this.id = ui;
        this.username = un;
        this.type = type;
        this.profiles = userProfiles;
    }

    public final String getDisplayName() {
        return this.displayName;
    }

    public final String getAccessToken() {
        return this.accessToken;
    }

    public final String getUserID() {
        return this.id;
    }

    public final String getProfileID() {
        return this.profileUUID;
    }

    public final void updateAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public final String getUsername() {
        return this.username;
    }

    public final UserType getType() {
        return this.type;
    }

    @Override
    public final String toString() {
        return this.displayName;
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
