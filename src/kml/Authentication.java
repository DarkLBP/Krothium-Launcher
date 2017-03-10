package kml;

import kml.exceptions.AuthenticationException;
import kml.objects.User;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * @author DarkLBP
 * website https://krothium.com
 */

public class Authentication {
    private boolean authenticated;
    private final Console console;
    private final Map<String, User> userDatabase = new HashMap<>();
    private String selectedAccount, selectedProfile, clientToken = UUID.randomUUID().toString();
    private final Kernel kernel;
    
    public Authentication(Kernel k){
        this.kernel = k;
        this.console = k.getConsole();
    }
    private void addUser(String userID, User u){
        console.printInfo("User " + u.getDisplayName() + ((this.userDatabase.containsKey(userID)) ? " updated." : " loaded."));
        this.userDatabase.put(userID, u);
    }
    private boolean removeUser(String userID){
        if (this.userDatabase.containsKey(userID)){
            console.printInfo("User " + this.userDatabase.get(userID).getDisplayName() + " deleted.");
            this.userDatabase.remove(userID);
            if (userID.equals(this.selectedAccount)){
                this.selectedAccount = null;
                this.selectedProfile = null;
            }
            return true;
        }else{
            console.printError("userID " + userID + " is not registered.");
            return false;
        }
    }
    public User getUser(String userID) {
        if (this.userDatabase.containsKey(userID)){
            return this.userDatabase.get(userID);
        }else{
            console.printError("userID " + userID + " is not registered.");
            return null;
        }
    }
    public User getSelectedUser(){
        return this.userDatabase.get(this.selectedAccount);
    }
    public boolean hasSelectedUser(){
        return this.selectedAccount != null && this.userDatabase.containsKey(this.selectedAccount);
    }
    public void setSelectedUser(String user) {
        if (user != null) {
            if (this.userDatabase.containsKey(user)) {
                this.selectedAccount = user;
                this.selectedProfile = this.userDatabase.get(user).getProfileID();
            } else {
                this.selectedAccount = null;
                this.selectedProfile = null;
            }
        } else {
            this.selectedAccount = null;
            this.selectedProfile = null;
        }
    }
    public void logOut(String user){
        this.removeUser(user);
    }
    public void authenticate(final String username, final String password) throws AuthenticationException{
        JSONObject request = new JSONObject();
        JSONObject agent = new JSONObject();
        agent.put("name", "Minecraft");
        agent.put("version", 1);
        request.put("agent", agent);
        request.put("username", username);
        request.put("password", password);
        if (this.clientToken != null){
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
        if (response.isEmpty()){
            throw new AuthenticationException("Authentication server does not respond.");
        }
        JSONObject r = new JSONObject(response);
        if (!r.has("error")){
            this.clientToken = r.getString("clientToken");
            String accessToken = (r.has("accessToken")) ? r.getString(("accessToken")) : null;
            String profileID = (r.has("selectedProfile")) ? r.getJSONObject("selectedProfile").getString("id") : null;
            String profileName = (r.has("selectedProfile")) ? r.getJSONObject("selectedProfile").getString("name") : null;
            String userID = (r.has("user")) ? r.getJSONObject("user").getString("id") : null;
            User u = new User(profileName, accessToken, userID, username, profileID);
            this.addUser(userID, u);
            this.selectedAccount = userID;
            this.selectedProfile = profileID;
            this.authenticated = true;
            kernel.getProfiles().updateSessionProfiles();
            kernel.getGUI().showLoginPrompt(false);
        }else{
            this.authenticated = false;
            if (r.has("errorMessage")){
                throw new AuthenticationException(r.getString("errorMessage"));
            }else if (r.has("cause")){
                throw new AuthenticationException(r.getString("error") + " caused by " + r.getString("cause"));
            }else{
                throw new AuthenticationException(r.getString("error"));
            }
        }
    }
    public void refresh() throws AuthenticationException{
        if (this.selectedAccount == null){
            throw new AuthenticationException("No user is selected.");
        }
        JSONObject request = new JSONObject();
        JSONObject agent = new JSONObject();
        User u = this.getUser(this.selectedAccount);
        agent.put("name", "Minecraft");
        agent.put("version", 1);
        request.put("accessToken", u != null ? u.getAccessToken() : null);
        request.put("clientToken", this.clientToken);
        request.put("requestUser", true);
        Map<String, String> postParams = new HashMap<>();
        postParams.put("Content-Type", "application/json; charset=utf-8");
        postParams.put("Content-Length", String.valueOf(request.toString().length()));
        String response;
        try {
            response = Utils.sendPost(Constants.REFRESH_URL, request.toString().getBytes(Charset.forName("UTF-8")), postParams);
        } catch (IOException ex) {
            if (Constants.USE_LOCAL){
                this.authenticated = true;
                kernel.getGUI().showLoginPrompt(false);
            }
            throw new AuthenticationException("Failed to send request to authentication server: " + ex);
        }
        if (response.isEmpty()){
            throw new AuthenticationException("Authentication server does not respond.");
        }
        JSONObject r = new JSONObject(response);
        if (!r.has("error")){
            this.clientToken = (r.has("clientToken")) ? r.getString("clientToken") : this.clientToken;
            if (r.has("accessToken")){
                u.updateAccessToken(r.getString("accessToken"));
            }
            this.authenticated = true;
            kernel.getProfiles().updateSessionProfiles();
            kernel.getGUI().showLoginPrompt(false);
        }else{
            this.authenticated = false;
            this.removeUser(this.selectedAccount);
            if (r.has("errorMessage")){
                throw new AuthenticationException(r.getString("errorMessage"));
            }else if (r.has("cause")){
                throw new AuthenticationException(r.getString("error") + " caused by " + r.getString("cause"));
            }else{
                throw new AuthenticationException(r.getString("error"));
            }
        }
    }
    public void validate() throws AuthenticationException{
        if (this.selectedAccount == null){
            throw new AuthenticationException("No user is selected.");
        }
        JSONObject request = new JSONObject();
        JSONObject agent = new JSONObject();
        User u = this.getUser(this.selectedAccount);
        agent.put("name", "Minecraft");
        agent.put("version", 1);
        request.put("accessToken", u != null ? u.getAccessToken() : null);
        request.put("clientToken", this.clientToken);
        Map<String, String> postParams = new HashMap<>();
        postParams.put("Content-Type", "application/json; charset=utf-8");
        postParams.put("Content-Length", String.valueOf(request.toString().length()));
        String response;
        try {
            response = Utils.sendPost(Constants.VALIDATE_URL, request.toString().getBytes(Charset.forName("UTF-8")), postParams);
        } catch (IOException ex) {
            if (Constants.USE_LOCAL){
                this.authenticated = true;
                kernel.getGUI().showLoginPrompt(false);
            }
            throw new AuthenticationException("Failed to send request to authentication server: " + ex);
        }
        if (response.isEmpty()){
            this.authenticated = true;
            kernel.getProfiles().updateSessionProfiles();
            kernel.getGUI().showLoginPrompt(false);
        }else{
            this.authenticated = false;
            JSONObject o = new JSONObject(response);
            if (o.has("error")){
                this.removeUser(this.selectedAccount);
                if (o.has("errorMessage")){
                    throw new AuthenticationException(o.getString("errorMessage"));
                }else if (o.has("cause")){
                    throw new AuthenticationException(o.getString("error") + " caused by " + o.getString("cause"));
                }else{
                    throw new AuthenticationException(o.getString("error"));
                }
            }
        }
    }
    public boolean isAuthenticated() { return this.authenticated; }
    public String getClientToken() { return this.clientToken; }
    public void fetchUsers() {
        console.printInfo("Loading user data.");
        File launcherProfiles = kernel.getConfigFile();
        if (launcherProfiles.exists()) {
            try {
                JSONObject root = new JSONObject(Utils.readURL(launcherProfiles.toURI().toURL()));
                if (root.has("clientToken")) {
                    this.clientToken = root.getString("clientToken");
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
                                        this.addUser(userID, u);
                                    }
                                }
                            }

                        }
                    }
                }
                if (root.has("selectedUser") && root.getJSONObject("selectedUser").has("account") && root.getJSONObject("selectedUser").has("profile")){
                    if (this.userDatabase.size() > 0){
                        this.setSelectedUser(root.getJSONObject("selectedUser").getString("account"));
                    }
                }
            }catch (Exception ex){
                ex.printStackTrace();
                console.printError("Failed to load user list.");
            }
        }else{
            console.printError("Launcher profiles file not found. Using defaults.");
        }
    }
    public void setClientToken(String clientToken){this.clientToken = clientToken;}
    public Map<String, User> getUsers() {
        return this.userDatabase;
    }
    public JSONObject toJSON(){
        JSONObject o = new JSONObject();
        o.put("clientToken", this.clientToken);
        if (this.userDatabase.size() > 0){
            JSONObject db = new JSONObject();
            Set s = this.userDatabase.keySet();
            for (Object value : s) {
                String key = value.toString();
                JSONObject user = new JSONObject();
                User u = this.userDatabase.get(key);
                user.put("accessToken", u.getAccessToken());
                user.put("username", u.getUsername());
                JSONObject profile = new JSONObject();
                JSONObject profileInfo = new JSONObject();
                profileInfo.put("displayName", u.getDisplayName());
                profile.put(u.getProfileID(), profileInfo);
                user.put("profiles", profile);
                db.put(key, user);
            }
            o.put("authenticationDatabase", db);
            JSONObject selectedUser = new JSONObject();
            selectedUser.put("account", this.selectedAccount);
            selectedUser.put("profile", this.selectedProfile);
            o.put("selectedUser", selectedUser);
        }
        return o;
    }
}
