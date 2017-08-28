package kml;

import kml.exceptions.AuthenticationException;
import kml.objects.User;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */

public class Authentication {
    private final Console console;
    private final Set<User> userDatabase = new HashSet<>();
    private final Kernel kernel;
    private boolean authenticated;
    private String selectedProfile, clientToken = UUID.randomUUID().toString();
    private User selectedAccount;

    public Authentication(Kernel k) {
        this.kernel = k;
        this.console = k.getConsole();
    }

    /**
     * Adds a user to the database
     * @param u The user to be added
     */
    private void addUser(User u) {
        if (this.userDatabase.contains(u)) {
            this.userDatabase.remove(u);
            this.userDatabase.add(u);
            console.printInfo("User " + u.getDisplayName() + " updated.");
        } else {
            this.userDatabase.add(u);
            console.printInfo("User " + u.getDisplayName() + " loaded.");
        }

    }

    /**
     * Removes a user for the database
     * @param u The User to be removed
     * @return A boolean that indicates if the user has been found and removed
     */
    public boolean removeUser(User u) {
        if (this.userDatabase.contains(u)) {
            console.printInfo("User " + u.getDisplayName() + " deleted.");
            userDatabase.remove(u);
            if (u.equals(this.selectedAccount)) {
                this.selectedAccount = null;
                this.selectedProfile = null;
            }
            return true;
        } else {
            console.printError("userID " + u.getUserID() + " is not registered.");
            return false;
        }
    }

    /**
     * Returns the selected user. Might be null
     * @return The selected user or null if no user is selected.
     */
    public User getSelectedUser() {
        return selectedAccount;
    }

    /**
     * Sets the selected user
     * @param user The user to be selected
     */
    public void setSelectedUser(User user) {
        if (user != null) {
            if (this.userDatabase.contains(user)) {
                this.selectedAccount = user;
                this.selectedProfile = user.getProfileID();
                return;
            }
        }
        this.selectedAccount = null;
        this.selectedProfile = null;
    }

    /**
     * Performs an authenticate request to the server
     * @param username The username
     * @param password The password
     * @throws AuthenticationException If authentication failed
     */
    public void authenticate(final String username, final String password) throws AuthenticationException {
        JSONObject request = new JSONObject();
        JSONObject agent = new JSONObject();
        agent.put("name", "Minecraft");
        agent.put("version", 1);
        request.put("agent", agent);
        request.put("username", username);
        request.put("password", password);
        if (this.clientToken != null) {
            request.put("clientToken", this.clientToken);
        }
        request.put("requestUser", true);
        Map<String, String> postParams = new HashMap<>();
        postParams.put("Content-Type", "application/json; charset=utf-8");
        postParams.put("Content-Length", String.valueOf(request.toString().length()));
        String response;
        try {
            response = Utils.sendPost(Constants.AUTHENTICATE_URL, request.toString().getBytes(Charset.forName("UTF-8")), postParams);
        } catch (Exception ex) {
            throw new AuthenticationException("Failed to send request to authentication server: " + ex);
        }
        if (response.isEmpty()) {
            throw new AuthenticationException("Authentication server does not respond.");
        }
        JSONObject r = new JSONObject(response);
        if (!r.has("error")) {
            this.clientToken = r.getString("clientToken");
            String accessToken = (r.has("accessToken")) ? r.getString(("accessToken")) : null;
            String profileID = (r.has("selectedProfile")) ? r.getJSONObject("selectedProfile").getString("id") : null;
            String profileName = (r.has("selectedProfile")) ? r.getJSONObject("selectedProfile").getString("name") : null;
            String userID = (r.has("user")) ? r.getJSONObject("user").getString("id") : null;
            User u = new User(profileName, accessToken, userID, username, profileID);
            this.addUser(u);
            this.selectedAccount = u;
            this.selectedProfile = profileID;
            this.authenticated = true;
        } else {
            this.authenticated = false;
            if (r.has("errorMessage")) {
                throw new AuthenticationException(r.getString("errorMessage"));
            } else if (r.has("cause")) {
                throw new AuthenticationException(r.getString("error") + " caused by " + r.getString("cause"));
            } else {
                throw new AuthenticationException(r.getString("error"));
            }
        }
    }

    /**
     * Performs a refresh request to the server
     * @throws AuthenticationException If the refresh failed
     */
    public void refresh() throws AuthenticationException {
        if (this.selectedAccount == null) {
            throw new AuthenticationException("No user is selected.");
        }
        JSONObject request = new JSONObject();
        JSONObject agent = new JSONObject();
        User u = this.selectedAccount;
        agent.put("name", "Minecraft");
        agent.put("version", 1);
        request.put("accessToken", u.getAccessToken());
        request.put("clientToken", this.clientToken);
        request.put("requestUser", true);
        Map<String, String> postParams = new HashMap<>();
        postParams.put("Content-Type", "application/json; charset=utf-8");
        postParams.put("Content-Length", String.valueOf(request.toString().length()));
        String response;
        try {
            response = Utils.sendPost(Constants.REFRESH_URL, request.toString().getBytes(Charset.forName("UTF-8")), postParams);
        } catch (IOException ex) {
            if (Constants.USE_LOCAL) {
                this.authenticated = true;
                console.printInfo("Authenticated locally.");
                return;
            } else {
                throw new AuthenticationException("Failed to send request to authentication server: " + ex);
            }
        }
        if (response.isEmpty()) {
            throw new AuthenticationException("Authentication server does not respond.");
        }
        JSONObject r = new JSONObject(response);
        if (!r.has("error")) {
            this.clientToken = (r.has("clientToken")) ? r.getString("clientToken") : this.clientToken;
            if (r.has("accessToken")) {
                u.updateAccessToken(r.getString("accessToken"));
            }
            this.authenticated = true;
        } else {
            this.authenticated = false;
            this.removeUser(this.selectedAccount);
            if (r.has("errorMessage")) {
                throw new AuthenticationException(r.getString("errorMessage"));
            } else if (r.has("cause")) {
                throw new AuthenticationException(r.getString("error") + " caused by " + r.getString("cause"));
            } else {
                throw new AuthenticationException(r.getString("error"));
            }
        }
    }

    /**
     * Checks if someone is authenticated
     * @return A boolean that indicates if is authenticated
     */
    public boolean isAuthenticated() {
        return this.authenticated;
    }

    /**
     * Returns the client token
     * @return The client token
     */
    public String getClientToken() {
        return this.clientToken;
    }

    /**
     * Loads the users from launcher_profile.json
     */
    public void fetchUsers() {
        console.printInfo("Loading user data.");
        JSONObject root = kernel.getLauncherProfiles();
        if (root != null) {
            try {
                String selectedUser = null;
                if (root.has("clientToken")) {
                    this.clientToken = root.getString("clientToken");
                }
                if (root.has("selectedUser") && root.getJSONObject("selectedUser").has("account") && root.getJSONObject("selectedUser").has("profile")) {
                    selectedUser = root.getJSONObject("selectedUser").getString("account");
                }
                if (root.has("authenticationDatabase")) {
                    JSONObject users = root.getJSONObject("authenticationDatabase");
                    if (users.length() > 0) {
                        Set s = users.keySet();
                        for (Object value : s) {
                            String userID = value.toString();
                            JSONObject user = users.getJSONObject(userID);
                            if (user.has("accessToken") && user.has("username") && user.has("profiles")) {
                                JSONObject profiles = user.getJSONObject("profiles");
                                if (profiles.keySet().size() == 1) {
                                    String uuid = profiles.keySet().toArray()[0].toString();
                                    JSONObject profile = profiles.getJSONObject(uuid);
                                    if (profile.has("displayName")) {
                                        User u = new User(profile.getString("displayName"), user.getString("accessToken"), userID, user.getString("username"), uuid);
                                        this.addUser(u);
                                        if (u.getUserID().equalsIgnoreCase(selectedUser)) {
                                            this.setSelectedUser(u);
                                        }
                                    }
                                }
                            }

                        }
                    }
                }
            } catch (Exception ex) {
                console.printError("Failed to load user list.");
            }
        } else {
            console.printError("No users to be loaded.");
        }
    }

    /**
     * Returns the user database
     * @return The user database
     */
    public Set<User> getUsers() {
        return this.userDatabase;
    }

    /**
     * Converts the user database to JSON
     * @return The user database in json format
     */
    public JSONObject toJSON() {
        JSONObject o = new JSONObject();
        o.put("clientToken", this.clientToken);
        if (this.userDatabase.size() > 0) {
            JSONObject db = new JSONObject();
            for (User u : userDatabase) {
                JSONObject user = new JSONObject();
                user.put("accessToken", u.getAccessToken());
                user.put("username", u.getUsername());
                JSONObject profile = new JSONObject();
                JSONObject profileInfo = new JSONObject();
                profileInfo.put("displayName", u.getDisplayName());
                profile.put(u.getProfileID(), profileInfo);
                user.put("profiles", profile);
                db.put(u.getUserID(), user);
            }
            o.put("authenticationDatabase", db);
            JSONObject selectedUser = new JSONObject();
            if (selectedAccount != null) {
                selectedUser.put("account", this.selectedAccount.getUserID());
            }
            selectedUser.put("profile", this.selectedProfile);
            o.put("selectedUser", selectedUser);
        }
        return o;
    }
}
