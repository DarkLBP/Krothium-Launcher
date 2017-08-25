package kml.matchers;

import kml.Constants;
import kml.Utils;

import java.net.URL;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public class RealmsMatcher implements URLMatcher {
    private final String realms_compatible = "https://pc.realms.minecraft.net/mco/client/compatible";
    private final String realms_invites = "https://pc.realms.minecraft.net/invites/count/pending";
    private final String realms_trial = "https://pc.realms.minecraft.net/trial";
    private final String realms_news = "https://pc.realms.minecraft.net/mco/v1/news";
    private final String realms_available = "https://pc.realms.minecraft.net/mco/available";
    private final String realms_liveplayerlist = "https://pc.realms.minecraft.net/activities/liveplayerlist";
    private final String realms_worlds = "https://pc.realms.minecraft.net/worlds";
    private final Pattern realms_world_join = Pattern.compile("https://pc.realms.minecraft.net/worlds/v1/([0-9]+)/join/pc");
    private final Pattern realms_edit_join = Pattern.compile("https://pc.realms.minecraft.net/worlds/([0-9]+)");
    private final Pattern realms_uninvite = Pattern.compile("https://pc.realms.minecraft.net/invites/([0-9]+)");
    private final String realms_pending = "https://pc.realms.minecraft.net/invites/pending";
    private final String realms_regions = "https://pc.realms.minecraft.net/regions/ping/stat";

    @Override
    public boolean match(URL url) {
        return url.toString().equalsIgnoreCase(realms_compatible) || url.toString().equalsIgnoreCase(realms_invites) || url.toString().equalsIgnoreCase(realms_trial) || url.toString().equalsIgnoreCase(realms_news) || url.toString().equalsIgnoreCase(realms_available) || url.toString().equalsIgnoreCase(realms_liveplayerlist) || url.toString().equalsIgnoreCase(realms_worlds) || realms_world_join.matcher(url.toString()).matches() || realms_uninvite.matcher(url.toString()).matches() || url.toString().equalsIgnoreCase(realms_pending) || realms_edit_join.matcher(url.toString()).matches() || url.toString().equalsIgnoreCase(realms_regions);
    }

    @Override
    public URL handle(URL url) {
        URL remoteURL = null;
        if (url.toString().equalsIgnoreCase(realms_compatible)) {
            remoteURL = Constants.REALMS_COMPATIBLE_URL;
        } else if (url.toString().equalsIgnoreCase(realms_available)) {
            remoteURL = Constants.REALMS_AVAILABLE_URL;
        } else if (url.toString().equalsIgnoreCase(realms_trial)) {
            remoteURL = Constants.REALMS_TRIAL_URL;
        } else if (url.toString().equalsIgnoreCase(realms_worlds)) {
            remoteURL = Constants.REALMS_WORLDS_URL;
        } else if (url.toString().equalsIgnoreCase(realms_invites)) {
            remoteURL = Constants.REALMS_INVITES_PENDING_URL;
        } else if (url.toString().equalsIgnoreCase(realms_news)) {
            remoteURL = Constants.REALMS_NEWS_URL;
        } else if (url.toString().equalsIgnoreCase(realms_liveplayerlist)) {
            remoteURL = Constants.REALMS_PLAYER_LIST_URL;
        } else if (realms_world_join.matcher(url.toString()).matches()) {
            Matcher m = realms_world_join.matcher(url.toString());
            if (m.matches()) {
                String worldID = m.group(1);
                remoteURL = Utils.stringToURL(Constants.REALMS_JOIN_WORLD_URL.toString() + "/" + worldID);
            }
        } else if (realms_uninvite.matcher(url.toString()).matches()) {
            remoteURL = Constants.REALMS_UNINVITE_URL;
        } else if (url.toString().equalsIgnoreCase(realms_pending)) {
            remoteURL = Constants.REALMS_INVITESLIST_URL;
        } else if (realms_edit_join.matcher(url.toString()).matches()) {
            remoteURL = Constants.REALMS_EDITWORLD_URL;
        } else if (url.toString().equalsIgnoreCase(realms_regions)) {
            remoteURL = Constants.REALMS_REGIONS_URL;
        }
        return remoteURL != null ? remoteURL : null;
    }
}