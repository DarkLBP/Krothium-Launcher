package kml.auth;

import kml.Console;
import kml.Kernel;
import kml.utils.Utils;
import kml.auth.user.User;
import kml.auth.user.UserProfile;
import kml.auth.user.UserType;
import kml.exceptions.AuthenticationException;
import org.json.JSONArray;
import org.json.JSONException;
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
    private String clientToken = UUID.randomUUID().toString();
    private User selectedAccount;
    private final String authenticatePath ,refreshPath;
    private final String mojangDomain = "authserver.mojang.com";
    private final String krothiumDomain = "mc.krothium.com";

    public Authentication(Kernel k) {
        kernel = k;
        console = k.getConsole();
        authenticatePath = "/authenticate";
        refreshPath = "/refresh";
    }

    /**
     * Adds a user to the database
     * @param u The user to be added
     */
    private void addUser(User u) {
        if (userDatabase.contains(u)) {
            userDatabase.remove(u);
            userDatabase.add(u);
            console.print("User " + u.getUserID() + " updated.");
        } else {
            userDatabase.add(u);
            console.print("User " + u.getUserID() + " loaded.");
        }

    }

    /**
     * Removes a user for the database
     * @param u The User to be removed
     */
    public void removeUser(User u) {
        if (userDatabase.contains(u)) {
            console.print("User " + u.getUserID() + " deleted.");
            userDatabase.remove(u);
            if (u.equals(selectedAccount)) {
                setSelectedUser(null);
            }
        } else {
            console.print("userID " + u.getUserID() + " is not registered.");
        }
    }

    /**
     * Returns the selected user. Might be null
     * @return The selected user or null if no user is selected.
     */
    public final User getSelectedUser() {
        return selectedAccount;
    }

    /**
     * Sets the selected user
     * @param user The user to be selected
     */
    public void setSelectedUser(User user) {
        if (user != null) {
            console.print("User " + user.getUserID() + " is now selected.");
        } else if (selectedAccount != null) {
            console.print(selectedAccount.getUserID() + " is not longer selected.");
        }
        selectedAccount = user;
    }

    /**
     * Performs an authenticate request to the server
     * @param username The username
     * @param password The password
     * @throws AuthenticationException If authentication failed
     */
    public final void authenticate(String username, String password) throws AuthenticationException {
        JSONObject request = new JSONObject();
        JSONObject agent = new JSONObject();
        UserType type;
        agent.put("name", "Minecraft");
        agent.put("version", 1);
        request.put("agent", agent);
        String tmpUser;
        if (username.startsWith("krothium://")) {
            type = UserType.KROTHIUM;
            tmpUser = username.replace("krothium://", "");
        } else {
            type = UserType.MOJANG;
            tmpUser = username;
        }
        request.put("username", tmpUser);
        request.put("password", password);
        if (clientToken != null) {
            request.put("clientToken", clientToken);
        }
        request.put("requestUser", true);
        Map<String, String> postParams = new HashMap<>();
        postParams.put("Content-Type", "application/json; charset=utf-8");
        postParams.put("Content-Length", String.valueOf(request.toString().length()));
        String response;
        String authURL;
        if (type == UserType.MOJANG) {
            authURL = "https://" + mojangDomain + authenticatePath;
        } else {
            authURL = "https://" + krothiumDomain + authenticatePath;
        }
        try {
            response = Utils.sendPost(authURL, request.toString().getBytes(Charset.forName("UTF-8")), postParams);
        } catch (IOException ex) {
            console.print("Failed to send request to authentication server");
            ex.printStackTrace(console.getWriter());
            throw new AuthenticationException("Failed to send request to authentication server");
        }
        if (response.isEmpty()) {
            throw new AuthenticationException("Authentication server does not respond.");
        }
        JSONObject r;
        try {
            r = new JSONObject(response);
        } catch (JSONException ex) {
            throw new AuthenticationException("Failed to read authentication response.");
        }
        if (!r.has("error")) {
            try {
                String accessToken = r.getString("accessToken");
                String selectedProfile = r.getJSONObject("selectedProfile").getString("id");
                String userID = r.getJSONObject("user").getString("id");
                clientToken = r.getString("clientToken");
                ArrayList<UserProfile> userProfiles = new ArrayList<>();
                JSONArray uprofs = r.getJSONArray("availableProfiles");
                for (int i = 0; i < uprofs.length(); i++){
                    JSONObject prof = uprofs.getJSONObject(i);
                    UserProfile up = new UserProfile(prof.getString("id"), prof.getString("name"));
                    userProfiles.add(up);
                }
                User u = new User(userID, accessToken, username, type, userProfiles, selectedProfile);
                selectedAccount = u;
                authenticated = true;
                addUser(u);
            } catch (JSONException ex) {
                ex.printStackTrace(console.getWriter());
                throw new AuthenticationException("Authentication server replied wrongly.");
            }
        } else {
            authenticated = false;
            throwError(r);
        }
    }

    /**
     * Performs a refresh request to the server
     * @throws AuthenticationException If the refresh failed
     */
    public final void refresh() throws AuthenticationException, JSONException{
        if (selectedAccount == null) {
            throw new AuthenticationException("No user is selected.");
        }
        JSONObject request = new JSONObject();
        JSONObject agent = new JSONObject();
        User u = selectedAccount;
        agent.put("name", "Minecraft");
        agent.put("version", 1);
        request.put("agent", agent);
        request.put("accessToken", u.getAccessToken());
        request.put("clientToken", clientToken);
        request.put("requestUser", true);
        Map<String, String> postParams = new HashMap<>();
        postParams.put("Content-Type", "application/json; charset=utf-8");
        postParams.put("Content-Length", String.valueOf(request.toString().length()));
        String response;
        String refreshURL;
        if (u.getType() == UserType.MOJANG) {
            refreshURL = "https://" + mojangDomain + refreshPath;
        } else {
            refreshURL = "https://" + krothiumDomain + refreshPath;
        }
        try {
            response = Utils.sendPost(refreshURL, request.toString().getBytes(Charset.forName("UTF-8")), postParams);
        } catch (IOException ex) {
            if (Kernel.USE_LOCAL) {
                authenticated = true;
                console.print("Authenticated locally.");
                return;
            } else {
                console.print("Failed to send request to authentication server");
                ex.printStackTrace(console.getWriter());
                throw new AuthenticationException("Failed to send request to authentication server");
            }
        }
        if (response.isEmpty()) {
            throw new AuthenticationException("Authentication server does not respond.");
        }
        JSONObject r;
        try {
            r = new JSONObject(response);
        } catch (JSONException ex) {
            throw new AuthenticationException("Failed to read authentication response.");
        }
        if (!r.has("error")) {
            try {
                clientToken = r.getString("clientToken");
                u.setAccessToken(r.getString("accessToken"));
                String selectedProfile = r.getJSONObject("selectedProfile").getString("id");
                u.setSelectedProfile(selectedProfile);
                authenticated = true;
            } catch (JSONException ex) {
                ex.printStackTrace(console.getWriter());
                throw new AuthenticationException("Authentication server replied wrongly.");
            }
        } else {
            authenticated = false;
            removeUser(selectedAccount);
            throwError(r);
        }
    }

    private void throwError(JSONObject message) throws AuthenticationException{
        if (message.has("errorMessage")) {
            throw new AuthenticationException(message.getString("errorMessage"));
        } else if (message.has("cause")) {
            throw new AuthenticationException(message.getString("error") + " caused by " + message.getString("cause"));
        } else {
            throw new AuthenticationException(message.getString("error"));
        }
    }

    /**
     * Checks if someone is authenticated
     * @return A boolean that indicates if is authenticated
     */
    public final boolean isAuthenticated() {
        return authenticated;
    }

    /**
     * Returns the client token
     * @return The client token
     */
    public final String getClientToken() {
        return clientToken;
    }

    /**
     * Loads the users from launcher_profile.json
     */
    public final void fetchUsers() {
        console.print("Loading user data.");
        JSONObject root = kernel.getLauncherProfiles();
        if (root != null) {
            String selectedUser = null;
            String selectedProfile = null;
            if (root.has("clientToken")) {
                clientToken = root.getString("clientToken");
            }
            if (root.has("selectedUser")) {
                Object selected = root.get("selectedUser");
                if (selected instanceof JSONObject) {
                    JSONObject s = (JSONObject)selected;
                    if (s.has("account")) {
                        selectedUser = s.getString("account");
                    }
                    if (s.has("profile")) {
                        selectedProfile = s.getString("profile");
                    }
                } else {
                    console.print("Legacy launcher_profiles.json found");
                }
            }
            if (root.has("authenticationDatabase")) {
                JSONObject users = root.getJSONObject("authenticationDatabase");
                Set s = users.keySet();
                for (Object value : s) {
                    String userID = value.toString();
                    JSONObject user = users.getJSONObject(userID);
                    if (user.has("accessToken") && user.has("username") && user.has("profiles")) {
                        String username = user.getString("username");
                        UserType userType = username.startsWith("krothium://") ? UserType.KROTHIUM : UserType.MOJANG;
                        JSONObject profiles = user.getJSONObject("profiles");
                        Set profileSet = profiles.keySet();
                        if (profileSet.size() > 0) {
                            ArrayList<UserProfile> userProfiles = new ArrayList<>();
                            for (Object o : profileSet) {
                                String profileUUID = o.toString();
                                JSONObject profile = profiles.getJSONObject(profileUUID);
                                if (profile.has("displayName")) {
                                    UserProfile up = new UserProfile(profileUUID, profile.getString("displayName"));
                                    userProfiles.add(up);
                                }
                            }
                            User u;
                            if (userID.equalsIgnoreCase(selectedUser)) {
                                u = new User(userID, user.getString("accessToken"), username, userType, userProfiles, selectedProfile);
                                addUser(u);
                                setSelectedUser(u);
                            } else {
                                u = new User(userID, user.getString("accessToken"), username, userType, userProfiles, null);
                                addUser(u);
                            }
                        }
                    }
                }
            }
        } else {
            console.print("No users to be loaded.");
        }
    }

    /**
     * Returns the user database
     * @return The user database
     */
    public final Set<User> getUsers() {
        return userDatabase;
    }

    /**
     * Converts the user database to JSON
     * @return The user database in json format
     */
    public final JSONObject toJSON() {
        JSONObject o = new JSONObject();
        o.put("clientToken", clientToken);
        if (!userDatabase.isEmpty()) {
            JSONObject db = new JSONObject();
            for (User u : userDatabase) {
                JSONObject user = new JSONObject();
                user.put("accessToken", u.getAccessToken());
                user.put("username", u.getUsername());
                JSONObject profile = new JSONObject();
                for (UserProfile up : u.getProfiles()) {
                    JSONObject profileInfo = new JSONObject();
                    profileInfo.put("displayName", u.getDisplayName());
                    profile.put(up.getId(), profileInfo);
                }
                user.put("profiles", profile);
                db.put(u.getUserID(), user);
            }
            o.put("authenticationDatabase", db);
            JSONObject selectedUser = new JSONObject();
            if (selectedAccount != null) {
                selectedUser.put("account", selectedAccount.getUserID());
                selectedUser.put("profile", selectedAccount.getSelectedProfile());
            }
            o.put("selectedUser", selectedUser);
        }
        return o;
    }
}
