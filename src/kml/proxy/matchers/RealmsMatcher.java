package kml.proxy.matchers;

import kml.Utils;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public class RealmsMatcher implements URLMatcher {
    private final String REALMS_COMPATIBLE = "https://pc.realms.minecraft.net/mco/client/compatible";
    private final String REALMS_INVITES = "https://pc.realms.minecraft.net/invites/count/pending";
    private final String REALMS_TRIAL = "https://pc.realms.minecraft.net/trial";
    private final String REALMS_NEWS = "https://pc.realms.minecraft.net/mco/v1/news";
    private final String REALMS_AVAILABLE = "https://pc.realms.minecraft.net/mco/available";
    private final String REALMS_LIVEPLAYERLIST = "https://pc.realms.minecraft.net/activities/liveplayerlist";
    private final String REALMS_WORLDS = "https://pc.realms.minecraft.net/worlds";
    private final Pattern REALMS_WORLD_JOIN = Pattern.compile("https://pc.realms.minecraft.net/worlds/v1/([0-9]+)/join/pc");
    private final Pattern REALMS_EDIT_JOIN = Pattern.compile("https://pc.realms.minecraft.net/worlds/([0-9]+)");
    private final Pattern REALMS_UNINVITE = Pattern.compile("https://pc.realms.minecraft.net/invites/([0-9]+)");
    private final String REALMS_PENDING = "https://pc.realms.minecraft.net/invites/pending";
    private final String REALMS_REGIONS = "https://pc.realms.minecraft.net/regions/ping/stat";
    private final URL REALMS_COMPATIBLE_URL = Utils.stringToURL("https://mc.krothium.com/realms/compatible");
    private final URL REALMS_AVAILABLE_URL = Utils.stringToURL("https://mc.krothium.com/realms/available");
    private final URL REALMS_TRIAL_URL = Utils.stringToURL("https://mc.krothium.com/realms/trial");
    private final URL REALMS_WORLDS_URL = Utils.stringToURL("https://mc.krothium.com/realms/worlds");
    private final URL REALMS_INVITES_PENDING_URL = Utils.stringToURL("https://mc.krothium.com/realms/pending");
    private final URL REALMS_NEWS_URL = Utils.stringToURL("https://mc.krothium.com/realms/news");
    private final URL REALMS_PLAYER_LIST_URL = Utils.stringToURL("https://mc.krothium.com/realms/liveplayerlist");
    private final URL REALMS_JOIN_WORLD_URL = Utils.stringToURL("https://mc.krothium.com/realms/join");
    private final URL REALMS_UNINVITE_URL = Utils.stringToURL("https://mc.krothium.com/realms/leave");
    private final URL REALMS_INVITESLIST_URL = Utils.stringToURL("https://mc.krothium.com/realms/invites");
    private final URL REALMS_EDITWORLD_URL = Utils.stringToURL("https://mc.krothium.com/realms/editworld");
    private final URL REALMS_REGIONS_URL = Utils.stringToURL("https://mc.krothium.com/realms/regions");

    @Override
    public final boolean match(URL url) {
        return url.toString().equalsIgnoreCase(REALMS_COMPATIBLE) || url.toString().equalsIgnoreCase(REALMS_INVITES) || url.toString().equalsIgnoreCase(REALMS_TRIAL) || url.toString().equalsIgnoreCase(REALMS_NEWS) || url.toString().equalsIgnoreCase(REALMS_AVAILABLE) || url.toString().equalsIgnoreCase(REALMS_LIVEPLAYERLIST) || url.toString().equalsIgnoreCase(REALMS_WORLDS) || REALMS_WORLD_JOIN.matcher(url.toString()).matches() || REALMS_UNINVITE.matcher(url.toString()).matches() || url.toString().equalsIgnoreCase(REALMS_PENDING) || REALMS_EDIT_JOIN.matcher(url.toString()).matches() || url.toString().equalsIgnoreCase(REALMS_REGIONS);
    }

    @Override
    public final URL handle(URL url) {
        URL remoteURL = null;
        if (url.toString().equalsIgnoreCase(REALMS_COMPATIBLE)) {
            remoteURL = this.REALMS_COMPATIBLE_URL;
        } else if (url.toString().equalsIgnoreCase(REALMS_AVAILABLE)) {
            remoteURL = this.REALMS_AVAILABLE_URL;
        } else if (url.toString().equalsIgnoreCase(REALMS_TRIAL)) {
            remoteURL = this.REALMS_TRIAL_URL;
        } else if (url.toString().equalsIgnoreCase(REALMS_WORLDS)) {
            remoteURL = this.REALMS_WORLDS_URL;
        } else if (url.toString().equalsIgnoreCase(REALMS_INVITES)) {
            remoteURL = this.REALMS_INVITES_PENDING_URL;
        } else if (url.toString().equalsIgnoreCase(REALMS_NEWS)) {
            remoteURL = this.REALMS_NEWS_URL;
        } else if (url.toString().equalsIgnoreCase(REALMS_LIVEPLAYERLIST)) {
            remoteURL = this.REALMS_PLAYER_LIST_URL;
        } else if (REALMS_WORLD_JOIN.matcher(url.toString()).matches()) {
            Matcher m = REALMS_WORLD_JOIN.matcher(url.toString());
            if (m.matches()) {
                String worldID = m.group(1);
                remoteURL = Utils.stringToURL(this.REALMS_JOIN_WORLD_URL.toString() + '/' + worldID);
            }
        } else if (REALMS_UNINVITE.matcher(url.toString()).matches()) {
            remoteURL = this.REALMS_UNINVITE_URL;
        } else if (url.toString().equalsIgnoreCase(REALMS_PENDING)) {
            remoteURL = this.REALMS_INVITESLIST_URL;
        } else if (REALMS_EDIT_JOIN.matcher(url.toString()).matches()) {
            remoteURL = this.REALMS_EDITWORLD_URL;
        } else if (url.toString().equalsIgnoreCase(REALMS_REGIONS)) {
            remoteURL = this.REALMS_REGIONS_URL;
        }
        return remoteURL;
    }
}