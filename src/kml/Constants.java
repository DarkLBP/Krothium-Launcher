package kml;

import javafx.scene.image.Image;
import kml.matchers.*;

import java.io.File;
import java.net.URL;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */

public class Constants {
    public static final int KERNEL_BUILD = 35;
    public static final String KERNEL_BUILD_NAME = "2.3.2";
    public static final URL AUTHENTICATE_URL = Utils.stringToURL("https://mc.krothium.com/authenticate");
    public static final URL REFRESH_URL = Utils.stringToURL("https://mc.krothium.com/refresh");
    public static final URL CHANGESKIN_URL = Utils.stringToURL("https://mc.krothium.com/changeskin");
    public static final URL CHANGECAPE_URL = Utils.stringToURL("https://mc.krothium.com/changecape");
    public static final URL GETLATEST_URL = Utils.stringToURL("https://mc.krothium.com/latestversion");
    public static final URL GET_PROFILESID = Utils.stringToURL("https://mc.krothium.com/api/profiles/minecraft");
    public static final URL BLOCKED_SERVERS = Utils.stringToURL("https://mc.krothium.com/server/blockedservers");
    public static final URL JOINSERVER = Utils.stringToURL("https://mc.krothium.com/server/join");
    public static final URL PROTECTION_URL = Utils.stringToURL("https://mc.krothium.com/server/protection");
    public static final URL HANDSHAKE_URL = Utils.stringToURL("https://mc.krothium.com/hello");
    public static final URL GET_PROFILESID_MOJANG = Utils.stringToURL("https://api.mojang.com/profiles/minecraft");
    public static final URL VERSION_MANIFEST_FILE = Utils.stringToURL("https://launchermeta.mojang.com/mc/game/version_manifest.json");
    public static final URL REALMS_COMPATIBLE_URL = Utils.stringToURL("https://mc.krothium.com/realms/compatible");
    public static final URL REALMS_AVAILABLE_URL = Utils.stringToURL("https://mc.krothium.com/realms/available");
    public static final URL REALMS_TRIAL_URL = Utils.stringToURL("https://mc.krothium.com/realms/trial");
    public static final URL REALMS_WORLDS_URL = Utils.stringToURL("https://mc.krothium.com/realms/worlds");
    public static final URL REALMS_INVITES_PENDING_URL = Utils.stringToURL("https://mc.krothium.com/realms/pending");
    public static final URL REALMS_NEWS_URL = Utils.stringToURL("https://mc.krothium.com/realms/news");
    public static final URL REALMS_PLAYER_LIST_URL = Utils.stringToURL("https://mc.krothium.com/realms/liveplayerlist");
    public static final URL REALMS_JOIN_WORLD_URL = Utils.stringToURL("https://mc.krothium.com/realms/join");
    public static final URL REALMS_UNINVITE_URL = Utils.stringToURL("https://mc.krothium.com/realms/leave");
    public static final URL REALMS_INVITESLIST_URL = Utils.stringToURL("https://mc.krothium.com/realms/invites");
    public static final URL REALMS_EDITWORLD_URL = Utils.stringToURL("https://mc.krothium.com/realms/editworld");
    public static final URL REALMS_REGIONS_URL = Utils.stringToURL("https://mc.krothium.com/realms/regions");
    public static final URL NEWS_URL = Utils.stringToURL("https://launchermeta.mojang.com/mc/news.json");
    public static final URL RUNTIME_URL = Utils.stringToURL("https://launchermeta.mojang.com/mc/launcher.json");
    public static final String RESOURCES_URL = "http://resources.download.minecraft.net/";
    public static Image PROFILE_ICONS;
    public static Image APPLICATION_ICON;
    public static File APPLICATION_WORKING_DIR = Utils.getWorkingDirectory();
    public static File APPLICATION_CONFIG = new File(APPLICATION_WORKING_DIR, "launcher_profiles.json");
    public static File APPLICATION_CACHE = new File(APPLICATION_WORKING_DIR, "cache");
    public static final URLMatcher[] HTTP_MATCHERS = new URLMatcher[]{new SkinMatcher(), new CapeMatcher(), new JoinServerMatcher(), new CheckServerMatcher()};
    public static final URLMatcher[] HTTPS_MATCHERS = new URLMatcher[]{new ProfileMatcher(), new JoinMatcher(), new HasJoinedMatcher(), new BlockedServersMatcher(), new RealmsMatcher()};
    public static final int DOWNLOAD_TRIES = 5;
    public static final int KEEP_OLD_LOGS = 4;
    public static boolean USE_LOCAL = false;
    static {
        try {
            PROFILE_ICONS = new Image("/kml/gui/textures/profile_icons.png");
            APPLICATION_ICON = new Image("/kml/gui/textures/icon.png");
        } catch (Exception ex) {

        }
    }
}
