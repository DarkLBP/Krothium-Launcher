package kml;

import kml.enums.ProfileIcon;
import kml.enums.ProfileType;
import kml.enums.VersionType;
import kml.objects.Profile;
import kml.objects.VersionMeta;
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
        this.kernel = k;
        this.console = k.getConsole();
    }

    /**
     * Returns the profile database
     * @return The profile database
     */
    public TreeSet<Profile> getProfiles() {
        return this.profiles;
    }

    /**
     * Adds a profile to the database
     * @param p The profile to be added
     * @return A boolean that indicates if the profile has been added
     */
    public boolean addProfile(Profile p) {
        if (!profiles.contains(p)) {
            profiles.add(p);
            console.printInfo("Profile " + p + " added");
            return true;
        }
        console.printError("Profile " + p + " already exists!");
        return false;
    }

    /**
     * Deletes a profile from the database
     * @param p The profile to be deleted
     * @return A boolean that indicates if the profile has been deleted
     */
    public boolean deleteProfile(Profile p) {
        if (profiles.contains(p)) {
            if (this.selected != null && this.selected.equals(p)) {
                console.printInfo("Profile " + p + " is selected and is going to be removed.");
                profiles.remove(p);
                console.printInfo("Profile " + p + " deleted.");
                if (profiles.size() > 0) {
                    this.setSelectedProfile(profiles.first());
                } else {
                    this.selected = null;
                }
            } else {
                profiles.remove(p);
                console.printInfo("Profile " + p + " deleted.");
            }
            return true;
        }
        console.printError("Profile " + p + " doesn't exist.");
        return false;
    }

    /**
     * Loads the profiles from the launcher_profiles.json
     */
    public void fetchProfiles() {
        console.printInfo("Fetching profiles.");
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
                            String uuid = key.replaceAll("(.{8})(.{4})(.{4})(.{4})(.+)", "$1-$2-$3-$4-$5");;
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
                    ProfileIcon icon;
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
                        icon = o.has("icon") ? ProfileIcon.valueOf(o.getString("icon").toUpperCase()) : null;
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
                            console.printError("Profile " + name != null ? name : "UNKNOWN" + " has an invalid resolution.");
                        }
                    }
                    Profile p = new Profile(key, name, type, created, lastUsed, version, gameDir, javaDir, javaArgs, resolution, icon, latestRelease, latestSnapshot);
                    if (first == null) {
                        first = p;
                    }
                    if (!profiles.contains(p)) {
                        this.addProfile(p);
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
                    addProfile(releaseProfile);
                }
                if (snapshotProfile == null) {
                    snapshotProfile = new Profile(ProfileType.SNAPSHOT);
                    addProfile(snapshotProfile);
                }
                if (profiles.size() > 0) {
                    if (latestUsedID != null) {
                        Settings settings = kernel.getSettings();
                        if (latestUsedID.getType() == ProfileType.SNAPSHOT && !settings.getEnableSnapshots()) {
                            setSelectedProfile(this.releaseProfile);
                        } else if (latestUsedID.getType() == ProfileType.CUSTOM) {
                            VersionType type = latestUsedID.getVersionID().getType();
                            if (type == VersionType.SNAPSHOT && !settings.getEnableSnapshots()) {
                                setSelectedProfile(this.releaseProfile);
                            } else if (type == VersionType.OLD_ALPHA && !settings.getEnableHistorical()) {
                                setSelectedProfile(this.releaseProfile);
                            } else if (type == VersionType.OLD_BETA && !settings.getEnableHistorical()) {
                                setSelectedProfile(this.releaseProfile);
                            } else {
                                setSelectedProfile(latestUsedID);
                            }
                        } else {
                            setSelectedProfile(latestUsedID);
                        }
                    } else {
                        console.printInfo("No profile is selected! Using first loaded (" + first + ")");
                        this.setSelectedProfile(first);
                    }
                }
            } catch (Exception ex) {
                console.printError("Failed to fetch profiles.");
            }
        } else {
            console.printError("No profiles to be loaded. Generating defaults.");
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
    public Profile getProfile(String id) {
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
    public Profile getSelectedProfile() {
        return this.selected;
    }

    /**
     * Returns the release profile
     * @return The release profile
     */
    public Profile getReleaseProfile() {
        return releaseProfile;
    }

    /**
     * Sets the current selected profile
     * @param p Profile to be selected
     * @return A boolean that indicates if the profile has been selected
     */
    public boolean setSelectedProfile(Profile p) {
        if (profiles.contains(p)) {
            p.setLastUsed(new Timestamp(System.currentTimeMillis()));
            console.printInfo("Profile " + p + " has been selected.");
            this.selected = p;
            return true;
        }
        return false;
    }

    /**
     * Converts the profile database to JSON
     * @return The json conversion of the profile database
     */
    public JSONObject toJSON() {
        JSONObject o = new JSONObject();
        JSONObject profiles = new JSONObject();
        for (Profile p : this.profiles) {
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
                    prof.put("icon", p.getIcon().name().toLowerCase());
                }
            }
            if (p.hasCreated()) {
                prof.put("created", p.getCreated().toString().replace(" ", "T") + "Z");
            }
            prof.put("lastUsed", p.getLastUsed().toString().replace(" ", "T") + "Z");
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
            profiles.put(p.getID(), prof);
        }
        o.put("profiles", profiles);
        return o;
    }
}
