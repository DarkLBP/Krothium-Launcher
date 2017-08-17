package kml;

import kml.enums.ProfileIcon;
import kml.enums.ProfileType;
import kml.objects.Profile;
import org.json.JSONObject;

import java.io.File;
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

    public TreeSet<Profile> getProfiles() {
        return this.profiles;
    }

    public boolean addProfile(Profile p) {
        if (!profiles.contains(p)) {
            if (p.getType() != ProfileType.CUSTOM) {
                if (!hasReleaseProfile() && p.getType() == ProfileType.RELEASE) {
                    releaseProfile = p;
                    profiles.add(p);
                    console.printInfo("Profile " + p + " added");
                    return true;
                } else if (!hasSnapshotProfile() && p.getType() == ProfileType.SNAPSHOT) {
                    snapshotProfile = p;
                    profiles.add(p);
                    console.printInfo("Profile " + p + " added");
                    return true;
                } else {
                    console.printInfo("Profile " + p + " ignored.");
                    return false;
                }
            } else {
                profiles.add(p);
                console.printInfo("Profile " + p + " added");
                return true;
            }
        }
        console.printError("Profile " + p + " already exists!");
        return false;
    }

    public boolean deleteProfile(Profile p) {
        if (profiles.contains(p)) {
            if (Objects.nonNull(this.selected) && this.selected.equals(p)) {
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
            if (this.hasReleaseProfile() && this.getReleaseProfile().equals(p)) {
                this.releaseProfile = null;
            }
            if (this.hasSnapshotProfile() && this.getSnapshotProfile().equals(p)) {
                this.snapshotProfile = null;
            }
            return true;
        }
        console.printError("Profile " + p + " doesn't exist.");
        return false;
    }

    public void fetchProfiles() {
        console.printInfo("Fetching profiles.");
        File launcherProfiles = kernel.getConfigFile();
        String latestUsedID = null;
        Timestamp latestUsedMillis = new Timestamp(0);
        if (launcherProfiles.exists()) {
            try {
                JSONObject root = new JSONObject(Utils.readURL(launcherProfiles.toURI().toURL()));
                JSONObject ples = root.getJSONObject("profiles");
                Set keys = ples.keySet();
                Iterator it = keys.iterator();
                String first = null;
                while (it.hasNext()) {
                    String key = it.next().toString();
                    JSONObject o = ples.getJSONObject(key);
                    String type = null;
                    String name = null;
                    String ver = null;
                    String created = null;
                    String lastUsed = null;
                    String gameDir = null;
                    String javaDir = null;
                    String javaArgs = null;
                    ProfileIcon icon = null;
                    Map<String, Integer> resolution = new HashMap<>();
                    if (o.has("name")) {
                        name = o.getString("name");
                    }
                    if (o.has("type")) {
                        type = o.getString("type");
                    }
                    if (o.has("created")) {
                        created = o.getString("created");
                    }
                    if (o.has("lastUsed")) {
                        lastUsed = o.getString("lastUsed");
                    }
                    if (o.has("lastVersionId")) {
                        ver = o.getString("lastVersionId");
                    }
                    if (o.has("gameDir")) {
                        gameDir = o.getString("gameDir");
                    }
                    if (o.has("javaDir")) {
                        javaDir = o.getString("javaDir");
                    }
                    if (o.has("javaArgs")) {
                        javaArgs = o.getString("javaArgs");
                    }
                    if (o.has("resolution")) {
                        JSONObject res = o.getJSONObject("resolution");
                        if (res.has("width") && res.has("height")) {
                            resolution.put("width", res.getInt("width"));
                            resolution.put("height", res.getInt("height"));
                        } else {
                            console.printError("Profile " + ((Objects.nonNull(name)) ? name : "UNKNOWN") + " has an invalid resolution.");
                        }
                    }
                    if (o.has("icon")) {
                        icon = ProfileIcon.valueOf(o.getString("icon").toUpperCase());
                    }
                    Profile p = new Profile(key, name, type, created, lastUsed, ver, gameDir, javaDir, javaArgs, resolution, icon);
                    if (Objects.isNull(first)) {
                        first = key;
                    }
                    if (!profiles.contains(p)) {
                        this.addProfile(p);
                        if (p.getLastUsed().compareTo(latestUsedMillis) > 0) {
                            latestUsedMillis = p.getLastUsed();
                            latestUsedID = p.getID();
                        }
                    }
                }
                if (profiles.size() > 0) {
                    if (Objects.nonNull(latestUsedID)) {
                        this.setSelectedProfile(getProfile(latestUsedID));
                    } else {
                        console.printInfo("No profile is selected! Using first loaded (" + first + ")");
                        this.setSelectedProfile(getProfile(first));
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                console.printError("Failed to fetch profiles.");
            }
        } else {
            console.printError("Launcher profiles file not found. Using defaults.");
        }
    }

    //Get profile by ID
    public Profile getProfile(String id) {
        for (Profile p : profiles) {
            if (p.getID().equalsIgnoreCase(id)) {
                return p;
            }
        }
        return null;
    }

    public Profile getSelectedProfile() {
        return this.selected;
    }

    public boolean setSelectedProfile(Profile p) {
        if (profiles.contains(p)) {
            if (p.getType() == ProfileType.CUSTOM) {
                p.setLastUsed(new Timestamp(System.currentTimeMillis()));
            }
            console.printInfo("Profile " + p + " has been selected.");
            this.selected = p;
            return true;
        }
        return false;
    }

    private boolean hasReleaseProfile() {
        return Objects.nonNull(this.releaseProfile);
    }

    private boolean hasSnapshotProfile() {
        return Objects.nonNull(this.snapshotProfile);
    }

    private Profile getReleaseProfile() {
        return this.releaseProfile;
    }

    private Profile getSnapshotProfile() {
        return this.snapshotProfile;
    }

    public boolean updateSessionProfiles() {
        boolean change = false;
        if (kernel.getAuthentication().isAuthenticated()) {
            if (!hasReleaseProfile()) {
                Profile release = new Profile(ProfileType.RELEASE);
                addProfile(release);
                setSelectedProfile(release);
                change = true;
            }
            if (!hasSnapshotProfile() && kernel.getSettings().getEnableSnapshots()) {
                Profile snapshot = new Profile(ProfileType.SNAPSHOT);
                addProfile(snapshot);
                change = true;
            }
            if (hasSnapshotProfile() && !kernel.getSettings().getEnableSnapshots()) {
                deleteProfile(getSnapshotProfile());
                change = true;
            }
        } else {
            if (hasReleaseProfile()) {
                deleteProfile(getReleaseProfile());
                change = true;
            }
            if (hasSnapshotProfile()) {
                deleteProfile(getSnapshotProfile());
                change = true;
            }
        }
        return change;
    }

    public JSONObject toJSON() {
        JSONObject o = new JSONObject();
        JSONObject profiles = new JSONObject();
        for (Profile p : this.profiles) {
            JSONObject prof = new JSONObject();
            if (p.hasName()) {
                prof.put("name", p.getName());
            }
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
            if (p.hasCreated()) {
                prof.put("created", p.getCreated().toString().replace(" ", "T") + "Z");
            }
            prof.put("lastUsed", p.getLastUsed().toString().replace(" ", "T") + "Z");
            if (p.hasGameDir()) {
                prof.put("gameDir", p.getGameDir().toString());
            }
            if (p.hasVersion()) {
                prof.put("lastVersionId", p.getVersionID());
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
            if (p.hasIcon()) {
                prof.put("icon", p.getIcon().name().toLowerCase());
            }
            profiles.put(p.getID(), prof);
        }
        o.put("profiles", profiles);
        o.put("selectedProfile", this.selected);
        return o;
    }
}
