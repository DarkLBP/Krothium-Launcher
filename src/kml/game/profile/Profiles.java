package kml.game.profile;

import kml.Console;
import kml.Kernel;
import kml.Settings;
import kml.game.version.VersionMeta;
import kml.game.version.VersionType;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.*;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */

public class Profiles {
    private final TreeSet<Profile> profiles = new TreeSet<>();
    private final Console console;
    private final Kernel kernel;
    private Profile selected, releaseProfile, snapshotProfile;

    public Profiles(Kernel k) {
        kernel = k;
        console = k.getConsole();
    }

    /**
     * Returns the profile database
     * @return The profile database
     */
    public final Iterable<Profile> getProfiles() {
        return profiles;
    }

    /**
     * Adds a profile to the database
     * @param p The profile to be added
     */
    public void addProfile(Profile p) {
        if (!profiles.contains(p)) {
            profiles.add(p);
            console.print("Profile " + p + " added");
            return;
        }
        console.print("Profile " + p + " already exists!");
    }

    /**
     * Deletes a profile from the database
     * @param p The profile to be deleted
     * @return A boolean that indicates if the profile has been deleted
     */
    public final boolean deleteProfile(Profile p) {
        if (profiles.contains(p)) {
            if (selected != null && selected.equals(p)) {
                console.print("Profile " + p + " is selected and is going to be removed.");
                profiles.remove(p);
                console.print("Profile " + p + " deleted.");
                if (!profiles.isEmpty()) {
                    setSelectedProfile(profiles.first());
                } else {
                    selected = null;
                }
            } else {
                profiles.remove(p);
                console.print("Profile " + p + " deleted.");
            }
            return true;
        }
        console.print("Profile " + p + " doesn't exist.");
        return false;
    }

    /**
     * Loads the profiles from the launcher_profiles.json
     */
    public final void fetchProfiles() {
        console.print("Fetching profiles.");
        Timestamp latestUsedMillis = new Timestamp(-1);
        JSONObject root = kernel.getLauncherProfiles();
        if (root != null) {
            try {
                JSONObject ples = root.getJSONObject("profiles");
                Set<String> keys = ples.keySet();
                Iterator<String> it = keys.iterator();
                Profile first = null, latestUsedID = null;
                while (it.hasNext()) {
                    String key = it.next();
                    JSONObject o = ples.getJSONObject(key);
                    try {
                        if (key.length() != 32) {
                            key = UUID.randomUUID().toString().replace("-", "");
                        } else {
                            String uuid = key.replaceAll("(.{8})(.{4})(.{4})(.{4})(.+)", "$1-$2-$3-$4-$5");
                            UUID.fromString(uuid);
                        }
                    } catch (IllegalArgumentException ex) {
                        key = UUID.randomUUID().toString().replace("-", "");
                    }
                    ProfileType type;
                    String typeString = o.has("type") ? o.getString("type") : null;
                    if (typeString != null) {
                        switch (typeString) {
                            case "latest-release":
                                type = ProfileType.RELEASE;
                                break;
                            case "latest-snapshot":
                                type = ProfileType.SNAPSHOT;
                                break;
                            default:
                                type = ProfileType.CUSTOM;
                        }
                    } else {
                        type = ProfileType.CUSTOM;
                    }
                    String name;
                    VersionMeta version;
                    String icon;
                    boolean latestRelease = false, latestSnapshot = false;
                    if (type == ProfileType.CUSTOM) {
                        name = o.has("name") ? o.getString("name") : null;
                        String ver = o.has("lastVersionId") ? o.getString("lastVersionId") : null;
                        if (ver != null) {
                            switch (ver) {
                                case "latest-release":
                                    latestRelease = true;
                                    break;
                                case "latest-snapshot":
                                    latestSnapshot = true;
                                    break;
                            }
                        }
                        version = kernel.getVersions().getVersionMeta(ver);
                        try {
                            icon = o.has("icon") ? o.getString("icon") : null;
                        } catch (IllegalArgumentException ex) {
                            icon = null;
                            console.print("Invalid profile icon for profile " + key);
                        }
                    } else {
                        name = null;
                        version = null;
                        icon = null;
                    }
                    String created = o.has("created") ? o.getString("created") : null;
                    String lastUsed = o.has("lastUsed") ? o.getString("lastUsed") : null;
                    String gameDir = o.has("gameDir") ? o.getString("gameDir") : null;
                    String javaDir = o.has("javaDir") ? o.getString("javaDir") : null;
                    String javaArgs = o.has("javaArgs") ? o.getString("javaArgs") : null;
                    Map<String, Integer> resolution = new HashMap<>();
                    if (o.has("resolution")) {
                        JSONObject res = o.getJSONObject("resolution");
                        if (res.has("width") && res.has("height")) {
                            resolution.put("width", res.getInt("width"));
                            resolution.put("height", res.getInt("height"));
                        } else {
                            console.print("Profile " + name != null ? name : "UNKNOWN" + " has an invalid resolution.");
                        }
                    }
                    Profile p = new Profile(key, name, type, created, lastUsed, version, gameDir, javaDir, javaArgs, resolution, icon, latestRelease, latestSnapshot);
                    if (first == null) {
                        first = p;
                    }
                    if (!profiles.contains(p)) {
                        addProfile(p);
                        if (p.getLastUsed().compareTo(latestUsedMillis) > 0) {
                            latestUsedMillis = p.getLastUsed();
                            latestUsedID = p;
                        }
                        if (type == ProfileType.RELEASE && releaseProfile == null) {
                            releaseProfile = p;
                        } else if (type == ProfileType.SNAPSHOT && snapshotProfile == null) {
                            snapshotProfile = p;
                        }
                    }
                }
                if (releaseProfile == null) {
                    releaseProfile = new Profile(ProfileType.RELEASE);
                    if (first == null) {
                        first = releaseProfile;
                    }
                    addProfile(releaseProfile);
                }
                if (snapshotProfile == null) {
                    snapshotProfile = new Profile(ProfileType.SNAPSHOT);
                    addProfile(snapshotProfile);
                }
                if (latestUsedID != null) {
                    Settings settings = kernel.getSettings();
                    if (latestUsedID.getType() == ProfileType.SNAPSHOT && !settings.getEnableSnapshots()) {
                        setSelectedProfile(releaseProfile);
                    } else if (latestUsedID.getType() == ProfileType.CUSTOM) {
                        VersionType type = latestUsedID.getVersionID().getType();
                        if (type == VersionType.SNAPSHOT && !settings.getEnableSnapshots()) {
                            setSelectedProfile(releaseProfile);
                        } else if (type == VersionType.OLD_ALPHA && !settings.getEnableHistorical()) {
                            setSelectedProfile(releaseProfile);
                        } else if (type == VersionType.OLD_BETA && !settings.getEnableHistorical()) {
                            setSelectedProfile(releaseProfile);
                        } else {
                            setSelectedProfile(latestUsedID);
                        }
                    } else {
                        setSelectedProfile(latestUsedID);
                    }
                } else {
                    console.print("No profile is selected! Using first loaded (" + first + ')');
                    setSelectedProfile(first);
                }
            } catch (JSONException ex) {
                console.print("Failed to fetch profiles.");
                ex.printStackTrace(console.getWriter());
            }
        } else {
            console.print("No profiles to be loaded. Generating defaults.");
            if (releaseProfile == null) {
                releaseProfile = new Profile(ProfileType.RELEASE);
                addProfile(releaseProfile);
            }
            if (snapshotProfile == null) {
                snapshotProfile = new Profile(ProfileType.SNAPSHOT);
                addProfile(snapshotProfile);
            }
        }
        if (selected == null) {
            setSelectedProfile(releaseProfile);
        }
    }

    /**
     * Gets a profile by id
     * @param id The id to search
     * @return The profile that matches the id or null
     */
    public final Profile getProfile(String id) {
        for (Profile p : profiles) {
            if (p.getID().equalsIgnoreCase(id)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Returns the selected profile
     * @return The selected profile
     */
    public final Profile getSelectedProfile() {
        return selected;
    }

    /**
     * Returns the release profile
     * @return The release profile
     */
    public final Profile getReleaseProfile() {
        return releaseProfile;
    }

    /**
     * Sets the current selected profile
     * @param p Profile to be selected
     */
    public void setSelectedProfile(Profile p) {
        p.setLastUsed(new Timestamp(System.currentTimeMillis()));
        selected = p;
        console.print("Profile " + p + " has been selected.");
    }

    /**
     * Converts the profile database to JSON
     * @return The json conversion of the profile database
     */
    public final JSONObject toJSON() {
        JSONObject o = new JSONObject();
        JSONObject profilesJSON = new JSONObject();
        for (Profile p : profiles) {
            JSONObject prof = new JSONObject();
            switch (p.getType()) {
                case RELEASE:
                    prof.put("type", "latest-release");
                    break;
                case SNAPSHOT:
                    prof.put("type", "latest-snapshot");
                    break;
                default:
                    prof.put("type", "custom");
            }
            if (p.getType() == ProfileType.CUSTOM) {
                if (p.hasName()) {
                    prof.put("name", p.getName());
                }
                if (p.hasVersion()) {
                    if (p.isLatestRelease()) {
                        prof.put("lastVersionId", "latest-release");
                    } else if (p.isLatestSnapshot()) {
                        prof.put("lastVersionId", "latest-snapshot");
                    } else {
                        prof.put("lastVersionId", p.getVersionID());
                    }
                }
                if (p.hasIcon()) {
                    prof.put("icon", p.getIcon());
                }
            }
            if (p.hasCreated()) {
                prof.put("created", p.getCreated().toString().replace(" ", "T") + 'Z');
            }
            prof.put("lastUsed", p.getLastUsed().toString().replace(" ", "T") + 'Z');
            if (p.hasGameDir()) {
                prof.put("gameDir", p.getGameDir().toString());
            }
            if (p.hasJavaDir()) {
                prof.put("javaDir", p.getJavaDir().toString());
            }
            if (p.hasJavaArgs()) {
                prof.put("javaArgs", p.getJavaArgs());
            }
            if (p.hasResolution()) {
                JSONObject res = new JSONObject();
                res.put("width", p.getResolutionWidth());
                res.put("height", p.getResolutionHeight());
                prof.put("resolution", res);
            }
            profilesJSON.put(p.getID(), prof);
        }
        o.put("profiles", profilesJSON);
        return o;
    }
}
