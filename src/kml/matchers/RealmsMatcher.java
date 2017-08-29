package kml.matchers;

import kml.Constants;
import kml.Utils;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public class RealmsMatcher implements URLMatcher {
    private static final String REALMS_COMPATIBLE = "https://pc.realms.minecraft.net/mco/client/compatible";
    private static final String REALMS_INVITES = "https://pc.realms.minecraft.net/invites/count/pending";
    private static final String REALMS_TRIAL = "https://pc.realms.minecraft.net/trial";
    private static final String REALMS_NEWS = "https://pc.realms.minecraft.net/mco/v1/news";
    private static final String REALMS_AVAILABLE = "https://pc.realms.minecraft.net/mco/available";
    private static final String REALMS_LIVEPLAYERLIST = "https://pc.realms.minecraft.net/activities/liveplayerlist";
    private static final String REALMS_WORLDS = "https://pc.realms.minecraft.net/worlds";
    private static final Pattern REALMS_WORLD_JOIN = Pattern.compile("https://pc.realms.minecraft.net/worlds/v1/([0-9]+)/join/pc");
    private static final Pattern REALMS_EDIT_JOIN = Pattern.compile("https://pc.realms.minecraft.net/worlds/([0-9]+)");
    private static final Pattern REALMS_UNINVITE = Pattern.compile("https://pc.realms.minecraft.net/invites/([0-9]+)");
    private static final String REALMS_PENDING = "https://pc.realms.minecraft.net/invites/pending";
    private static final String REALMS_REGIONS = "https://pc.realms.minecraft.net/regions/ping/stat";

    @Override
    public final boolean match(URL url) {
        return url.toString().equalsIgnoreCase(REALMS_COMPATIBLE) || url.toString().equalsIgnoreCase(REALMS_INVITES) || url.toString().equalsIgnoreCase(REALMS_TRIAL) || url.toString().equalsIgnoreCase(REALMS_NEWS) || url.toString().equalsIgnoreCase(REALMS_AVAILABLE) || url.toString().equalsIgnoreCase(REALMS_LIVEPLAYERLIST) || url.toString().equalsIgnoreCase(REALMS_WORLDS) || REALMS_WORLD_JOIN.matcher(url.toString()).matches() || REALMS_UNINVITE.matcher(url.toString()).matches() || url.toString().equalsIgnoreCase(REALMS_PENDING) || REALMS_EDIT_JOIN.matcher(url.toString()).matches() || url.toString().equalsIgnoreCase(REALMS_REGIONS);
    }

    @Override
    public final URL handle(URL url) {
        URL remoteURL = null;
        if (url.toString().equalsIgnoreCase(REALMS_COMPATIBLE)) {
            remoteURL = Constants.REALMS_COMPATIBLE_URL;
        } else if (url.toString().equalsIgnoreCase(REALMS_AVAILABLE)) {
            remoteURL = Constants.REALMS_AVAILABLE_URL;
        } else if (url.toString().equalsIgnoreCase(REALMS_TRIAL)) {
            remoteURL = Constants.REALMS_TRIAL_URL;
        } else if (url.toString().equalsIgnoreCase(REALMS_WORLDS)) {
            remoteURL = Constants.REALMS_WORLDS_URL;
        } else if (url.toString().equalsIgnoreCase(REALMS_INVITES)) {
            remoteURL = Constants.REALMS_INVITES_PENDING_URL;
        } else if (url.toString().equalsIgnoreCase(REALMS_NEWS)) {
            remoteURL = Constants.REALMS_NEWS_URL;
        } else if (url.toString().equalsIgnoreCase(REALMS_LIVEPLAYERLIST)) {
            remoteURL = Constants.REALMS_PLAYER_LIST_URL;
        } else if (REALMS_WORLD_JOIN.matcher(url.toString()).matches()) {
            Matcher m = REALMS_WORLD_JOIN.matcher(url.toString());
            if (m.matches()) {
                String worldID = m.group(1);
                remoteURL = Utils.stringToURL(Constants.REALMS_JOIN_WORLD_URL.toString() + '/' + worldID);
            }
        } else if (REALMS_UNINVITE.matcher(url.toString()).matches()) {
            remoteURL = Constants.REALMS_UNINVITE_URL;
        } else if (url.toString().equalsIgnoreCase(REALMS_PENDING)) {
            remoteURL = Constants.REALMS_INVITESLIST_URL;
        } else if (REALMS_EDIT_JOIN.matcher(url.toString()).matches()) {
            remoteURL = Constants.REALMS_EDITWORLD_URL;
        } else if (url.toString().equalsIgnoreCase(REALMS_REGIONS)) {
            remoteURL = Constants.REALMS_REGIONS_URL;
        }
        return remoteURL;
    }
}