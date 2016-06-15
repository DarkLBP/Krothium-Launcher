package kmlk;

import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @website http://krotium.com
 * @author DarkLBP
 */

public class Authentication {
    private boolean authenticated = false;
    private final Console console;
    protected String username;
    protected String password;
    protected String clientToken;
    protected Map<String, User> userDatabase = new HashMap();
    private String selectedProfile;

    public Authentication()
    {
        this.console = Kernel.getKernel().getConsole();
    }
    public void setCredentials(String user, String pass)
    {
        this.username = user;
        this.password = pass;
    }
    public void clearCredentials()
    {
        this.username = null;
        this.password = null;
    }
    public void addToDatabase(String profileID, User u)
    {
        console.printInfo("User " + u.getDisplayName() + ((this.userDatabase.containsKey(profileID)) ? " updated." : " loaded."));
        this.userDatabase.put(profileID, u);   
    }
    public void removeFromDatabase(String profileID)
    {
        if (this.userDatabase.containsKey(profileID))
        {
            console.printInfo("User " + this.userDatabase.get(profileID).getDisplayName() + " deleted.");
            this.userDatabase.remove(profileID);
        }
        else
        {
            console.printError("Profile id " + profileID + " is not registered.");
        }
    }
    public User getFromDatabase(String profileID)
    {
        if (this.userDatabase.containsKey(profileID))
        {
            return this.userDatabase.get(profileID);
        }
        else
        {
            console.printError("Profile id " + profileID + " is not registered.");
            return null;
        }
    }
    public User getSelectedUser()
    {
        return this.userDatabase.get(this.selectedProfile);
    }
    public void authenticate()
    {
        JSONObject request = new JSONObject();
        JSONObject agent = new JSONObject();
        agent.put("name", "Minecraft");
        agent.put("version", 1);
        request.put("agent", agent);
        request.put("username", this.username);
        request.put("password", this.password);
        if (this.clientToken != null)
        {
            request.put("clientToken", this.clientToken);
        }
        request.put("requestUser", true);
        try {
            JSONObject response = new JSONObject(Utils.sendJSONPost(Constants.authAuthenticate, request.toString()));
            if (!response.has("error"))
            {
                this.clientToken = response.getString("clientToken");
                String accessToken = (response.has("accessToken")) ? response.getString(("accessToken")) : null;
                String profileID = (response.has("selectedProfile")) ? response.getJSONObject("selectedProfile").getString("id") : null;
                String profileName = (response.has("selectedProfile")) ? response.getJSONObject("selectedProfile").getString("name") : null;
                String userID = (response.has("user")) ? response.getJSONObject("user").getString("id") : null;
                JSONObject user = response.getJSONObject("user");
                Map<String, String> properties = new HashMap();
                if (user.has("userProperties"))
                {
                    JSONArray props = user.getJSONArray("userProperties");
                    if (props.length() > 0)
                    {
                        for (int i = 0; i < props.length(); i++)
                        {
                            JSONObject p = props.getJSONObject(i);
                            if (p.has("name") && p.has("value"))
                            {
                                properties.put(p.getString("name"), p.getString("value"));
                            }
                        }
                    }
                }
                User u = new User(profileName, accessToken, userID, this.username, Utils.stringToUUID(profileID), properties);
                this.addToDatabase(profileID, u);
                this.selectedProfile = profileID;
                this.authenticated = true;
            }
            else
            {
                this.authenticated = false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            console.printError("Failed to authenticate.");
            this.authenticated = false;
        }
    }
    public void refresh()
    {
        JSONObject request = new JSONObject();
        JSONObject agent = new JSONObject();
        User u = this.getFromDatabase(this.selectedProfile);
        agent.put("name", "Minecraft");
        agent.put("version", 1);
        request.put("accessToken", u.getAccessToken());
        request.put("clientToken", this.clientToken);
        request.put("requestUser", true);
        try {
            JSONObject response = new JSONObject(Utils.sendJSONPost(Constants.authRefresh, request.toString()));
            if (!response.has("error"))
            {
                this.clientToken = (response.has("clientToken")) ? response.getString("clientToken") : this.clientToken;
                if (response.has("accessToken"))
                {
                    u.updateAccessToken(response.getString("accessToken"));
                }
                this.authenticated = true;
            }
            else
            {
                this.authenticated = false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            console.printError("Failed to authenticate.");
            this.authenticated = false;
        }
    }
    public void validate()
    {
        JSONObject request = new JSONObject();
        JSONObject agent = new JSONObject();
        User u = this.getFromDatabase(this.selectedProfile);
        agent.put("name", "Minecraft");
        agent.put("version", 1);
        request.put("accessToken", u.getAccessToken());
        request.put("clientToken", this.clientToken);
        try {
            String response = Utils.sendJSONPost(Constants.authValidate, request.toString());
            if (response.length() == 0)
            {
                this.authenticated = true;
            }
            else
            {
                this.authenticated = false;
                JSONObject o = new JSONObject(response);
                if (o.has("error"))
                {
                    //Classify error
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            console.printError("Failed to authenticate.");
            this.authenticated = false;
        }
    }
    public boolean isAuthenticated()
    {
        return this.authenticated;
    }
    public String getClientToken()
    {
        return this.clientToken;
    }
    public void fetchUsers()
    {
        console.printInfo("Loading user data.");
        File launcherProfiles = Kernel.getKernel().getConfigFile();
        if (launcherProfiles.exists())
        {
            try
            {
                JSONObject root = new JSONObject(Utils.readURL(launcherProfiles.toURI().toURL()));
                if (root.has("clientToken"))
                {
                    this.clientToken = root.getString("clientToken");
                }
                if (root.has("authenticationDatabase"))
                {
                    JSONObject users = root.getJSONObject("authenticationDatabase");
                    if (users.length() > 0)
                    {
                        Set s = users.keySet();
                        Iterator it = s.iterator();
                        while (it.hasNext())
                        {
                            String profile = it.next().toString();
                            JSONObject user = users.getJSONObject(profile); 
                            if (user.has("displayName") && user.has("accessToken") && user.has("userid") && user.has("uuid") && user.has("username"))
                            {
                                String displayName = user.getString("displayName");
                                String accessToken = user.getString("accessToken");
                                String userID = user.getString("userid");
                                UUID uuid = UUID.fromString(user.getString("uuid"));
                                String username = user.getString("username");
                                Map<String, String> properties = new HashMap();
                                if (user.has("userProperties"))
                                {
                                    JSONArray props = user.getJSONArray("userProperties");
                                    if (props.length() > 0)
                                    {
                                        for (int i = 0; i < props.length(); i++)
                                        {
                                            JSONObject p = props.getJSONObject(i);
                                            if (p.has("name") && p.has("value"))
                                            {
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
                if (root.has("selectedUser"))
                {
                    this.selectedProfile = null;
                    if (this.userDatabase.size() > 0)
                    {
                        if (this.userDatabase.containsKey(root.getString("selectedUser")))
                        {
                            this.selectedProfile = root.getString("selectedUser");
                        }
                        else
                        {
                            Set s = this.userDatabase.keySet();
                            Iterator i = s.iterator();
                            while (this.selectedProfile == null)
                            {
                                this.selectedProfile = i.next().toString();
                            }
                        }
                    }
                }
                else
                {
                    this.selectedProfile = null;
                    if (this.userDatabase.size() > 0)
                    {
                        Set s = this.userDatabase.keySet();
                        Iterator i = s.iterator();
                        while (this.selectedProfile == null)
                        {
                            this.selectedProfile = i.next().toString();
                        }
                    }
                }
            }
            catch (Exception ex)
            {
                console.printError("Failed to load user list.");
            }
        }
        else
        {
            console.printError("Launcher profiles file not found. Using defaults.");
            this.clientToken = UUID.randomUUID().toString();
        }
    }
    public void setClientToken(String clientToken)
    {
        this.clientToken = clientToken;
    }
    public JSONObject toJSON()
    {
        JSONObject o = new JSONObject();
        o.put("clientToken", this.clientToken);
        if (this.userDatabase.size() > 0)
        {
            JSONObject db = new JSONObject();
            Set s = this.userDatabase.keySet();
            Iterator it = s.iterator();
            while (it.hasNext())
            {
                String key = it.next().toString();
                JSONObject user = new JSONObject();
                User u = this.userDatabase.get(key);
                user.put("displayName", u.getDisplayName());
                if (u.hasProperties())
                {
                    JSONArray props = new JSONArray();
                    Map<String, String> p = u.getProperties();
                    Set s1 = p.keySet();
                    Iterator it1 = s1.iterator();
                    while(it1.hasNext())
                    {
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
