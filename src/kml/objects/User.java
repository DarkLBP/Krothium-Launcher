package kml.objects;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */

public class User {
    private final String displayName, userID, userName, profileUUID;
    private String accessToken;

    public User(String name, String at, String ui, String un, String pi) {
        this.displayName = name;
        this.accessToken = at;
        this.userID = ui;
        this.userName = un;
        this.profileUUID = pi;
    }

    public final String getDisplayName() {
        return this.displayName;
    }

    public final String getAccessToken() {
        return this.accessToken;
    }

    public final String getUserID() {
        return this.userID;
    }

    public final String getProfileID() {
        return this.profileUUID;
    }

    public final void updateAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public final String getUsername() {
        return this.userName;
    }

    @Override
    public final String toString() {
        return this.displayName;
    }

    @Override
    public final boolean equals(Object o) {
        return o instanceof User && this.userID.equalsIgnoreCase(((User) o).userID);
    }

    @Override
    public final int hashCode() {
        return this.userID.hashCode();
    }
}
