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

    public String getDisplayName() {
        return this.displayName;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public String getUserID() {
        return this.userID;
    }

    public String getProfileID() {
        return this.profileUUID;
    }

    public void updateAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getUsername() {
        return this.userName;
    }

    @Override
    public String toString() {
        return this.displayName;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof User && userID.equalsIgnoreCase(((User) o).userID);
    }

    @Override
    public int hashCode() {
        return userID.hashCode();
    }
}
