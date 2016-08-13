package kml;

import kml.exceptions.AuthenticationException;
import kml.objects.User;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @website https://krothium.com
 * @author DarkLBP
 */

public class Authentication {
    private boolean authenticated = false;
    private final Console console;
    protected String clientToken = UUID.randomUUID().toString();
    protected Map<String, User> userDatabase = new HashMap();
    private String selectedProfile;
    private final Kernel kernel;
    
    public Authentication(Kernel k){
        this.kernel = k;
        this.console = k.getConsole();
    }
    public void addToDatabase(String profileID, User u){
        console.printInfo("User " + u.getDisplayName() + ((this.userDatabase.containsKey(profileID)) ? " updated." : " loaded."));
        this.userDatabase.put(profileID, u);   
    }
    public void removeFromDatabase(String profileID){
        if (this.userDatabase.containsKey(profileID)){
            console.printInfo("User " + this.userDatabase.get(profileID).getDisplayName() + " deleted.");
            this.userDatabase.remove(profileID);
        }else{
            console.printError("Profile id " + profileID + " is not registered.");
        }
    }
    public User getFromDatabase(String profileID) {
        if (this.userDatabase.containsKey(profileID)){
            return this.userDatabase.get(profileID);
        }else{
            console.printError("Profile id " + profileID + " is not registered.");
            return null;
        }
    }
    public User getSelectedUser(){return this.userDatabase.get(this.selectedProfile);}
    public boolean hasSelectedUser(){return (this.selectedProfile != null);}
    public boolean logOut(){
        if (this.hasSelectedUser()){
            this.authenticated = false;
            return this.removeUser(this.selectedProfile);
        }
        return false;
    }
    public boolean removeUser(String u){
        if (this.userDatabase.containsKey(u)){
            this.userDatabase.remove(u);
            return true;
        } else {
            return false;
        }
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
        Map<String, String> postParams = new HashMap();
        postParams.put("Content-Type", "application/json; charset=utf-8");
        postParams.put("Content-Length", "" + request.toString().length());
        String response = null;
        try {
            response = Utils.sendPost(Constants.AUTHENTICATE_URL, request.toString().getBytes(Charset.forName("UTF-8")), postParams);
        } catch (Exception ex) {
            throw new AuthenticationException("Failed to send request to authentication server.");
        }
        if (response == null || response.isEmpty()){
            throw new AuthenticationException("Authentication server does not respond.");
        }
        JSONObject r = new JSONObject(response);
        if (!r.has("error")){
            this.clientToken = r.getString("clientToken");
            String accessToken = (r.has("accessToken")) ? r.getString(("accessToken")) : null;
            String profileID = (r.has("selectedProfile")) ? r.getJSONObject("selectedProfile").getString("id") : null;
            String profileName = (r.has("selectedProfile")) ? r.getJSONObject("selectedProfile").getString("name") : null;
            String userID = (r.has("user")) ? r.getJSONObject("user").getString("id") : null;
            JSONObject user = r.getJSONObject("user");
            Map<String, String> properties = new HashMap();
            if (user.has("userProperties")){
                JSONArray props = user.getJSONArray("userProperties");
                if (props.length() > 0){
                    for (int i = 0; i < props.length(); i++){
                        JSONObject p = props.getJSONObject(i);
                        if (p.has("name") && p.has("value")){
                            properties.put(p.getString("name"), p.getString("value"));
                        }
                    }
                }
            }
            User u = new User(profileName, accessToken, userID, username, Utils.stringToUUID(profileID), properties);
            this.addToDatabase(profileID, u);
            this.selectedProfile = profileID;
            if (kernel.getSelectedProfile().equals("(Default)")){
                kernel.renameProfile("(Default)", profileName);
            }
            this.authenticated = true;
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
        JSONObject request = new JSONObject();
        JSONObject agent = new JSONObject();
        User u = this.getFromDatabase(this.selectedProfile);
        agent.put("name", "Minecraft");
        agent.put("version", 1);
        request.put("accessToken", u.getAccessToken());
        request.put("clientToken", this.clientToken);
        request.put("requestUser", true);
        Map<String, String> postParams = new HashMap();
        postParams.put("Content-Type", "application/json; charset=utf-8");
        postParams.put("Content-Length", "" + request.toString().length());
        String response = null;
        try {
            response = Utils.sendPost(Constants.REFRESH_URL, request.toString().getBytes(Charset.forName("UTF-8")), postParams);
        } catch (Exception ex) {
            throw new AuthenticationException("Failed to send request to authentication server.");
        }
        if (response == null || response.isEmpty()){
            throw new AuthenticationException("Authentication server does not respond.");
        }
        JSONObject r = new JSONObject(response);
        if (!r.has("error")){
            this.clientToken = (r.has("clientToken")) ? r.getString("clientToken") : this.clientToken;
            if (r.has("accessToken")){
                u.updateAccessToken(r.getString("accessToken"));
            }
            this.authenticated = true;
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
    public void validate() throws AuthenticationException{
        JSONObject request = new JSONObject();
        JSONObject agent = new JSONObject();
        User u = this.getFromDatabase(this.selectedProfile);
        agent.put("name", "Minecraft");
        agent.put("version", 1);
        request.put("accessToken", u.getAccessToken());
        request.put("clientToken", this.clientToken);
        Map<String, String> postParams = new HashMap();
        postParams.put("Content-Type", "application/json; charset=utf-8");
        postParams.put("Content-Length", "" + request.toString().length());
        String response = null;
        try {
            response = Utils.sendPost(Constants.VALIDATE_URL, request.toString().getBytes(Charset.forName("UTF-8")), postParams);
        } catch (Exception ex) {
            throw new AuthenticationException("Failed to send request to authentication server.");
        }
        if (response == null){
            throw new AuthenticationException("Authentication server does not respond.");
        }
        if (response.isEmpty()){
            this.authenticated = true;
        }else{
            this.authenticated = false;
            JSONObject o = new JSONObject(response);
            if (o.has("error")){
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
                        Iterator it = s.iterator();
                        while (it.hasNext()) {
                            String profile = it.next().toString();
                            JSONObject user = users.getJSONObject(profile); 
                            if (user.has("displayName") && user.has("accessToken") && user.has("userid") && user.has("uuid") && user.has("username")) {
                                String displayName = user.getString("displayName");
                                String accessToken = user.getString("accessToken");
                                String userID = user.getString("userid");
                                UUID uuid = UUID.fromString(user.getString("uuid"));
                                String username = user.getString("username");
                                Map<String, String> properties = new HashMap();
                                if (user.has("userProperties")) {
                                    JSONArray props = user.getJSONArray("userProperties");
                                    if (props.length() > 0) {
                                        for (int i = 0; i < props.length(); i++) {
                                            JSONObject p = props.getJSONObject(i);
                                            if (p.has("name") && p.has("value")){
                                                properties.put(p.getString("name"), p.getString("value"));
                                            }
                                        }
                                    }
                                }
                                User u = new User(displayName, accessToken, userID, username, uuid, properties);
                                this.addToDatabase(profile, u);
                            }
                        }
                    }
                }
                if (root.has("selectedUser")){
                    this.selectedProfile = null;
                    if (this.userDatabase.size() > 0){
                        if (this.userDatabase.containsKey(root.getString("selectedUser"))){
                            this.selectedProfile = root.getString("selectedUser");
                        }else{
                            Set s = this.userDatabase.keySet();
                            Iterator i = s.iterator();
                            while (this.selectedProfile == null){
                                this.selectedProfile = i.next().toString();
                            }
                        }
                    }
                }else{
                    this.selectedProfile = null;
                    if (this.userDatabase.size() > 0){
                        Set s = this.userDatabase.keySet();
                        Iterator i = s.iterator();
                        while (this.selectedProfile == null){
                            this.selectedProfile = i.next().toString();
                        }
                    }
                }
            }catch (Exception ex){
                console.printError("Failed to load user list.");
            }
        }else{
            console.printError("Launcher profiles file not found. Using defaults.");
        }
    }
    public void setClientToken(String clientToken){this.clientToken = clientToken;}
    public JSONObject toJSON(){
        JSONObject o = new JSONObject();
        o.put("clientToken", this.clientToken);
        if (this.userDatabase.size() > 0){
            JSONObject db = new JSONObject();
            Set s = this.userDatabase.keySet();
            Iterator it = s.iterator();
            while (it.hasNext()){
                String key = it.next().toString();
                JSONObject user = new JSONObject();
                User u = this.userDatabase.get(key);
                user.put("displayName", u.getDisplayName());
                if (u.hasProperties()){
                    JSONArray props = new JSONArray();
                    Map<String, String> p = u.getProperties();
                    Set s1 = p.keySet();
                    Iterator it1 = s1.iterator();
                    while(it1.hasNext()){
                        String k = it1.next().toString();
                        JSONObject jo = new JSONObject();
                        jo.put("name", k);
                        jo.put("value", p.get(k));
                        props.put(jo);
                    }
                    user.put("userProperties", props);
                }
                user.put("accessToken", u.getAccessToken());
                user.put("userid", u.getUserID());
                user.put("uuid", u.getProfileID().toString());
                user.put("username", u.getUsername());
                db.put(key, user);
            }
            o.put("authenticationDatabase", db);
        }
        o.put("selectedUser", this.selectedProfile);
        return o;
    }
}
